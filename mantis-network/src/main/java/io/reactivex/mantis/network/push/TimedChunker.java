/*
 * Copyright 2019 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.reactivex.mantis.network.push;

import io.mantisrx.common.metrics.Counter;
import io.mantisrx.common.metrics.Metrics;
import io.mantisrx.common.metrics.MetricsRegistry;
import io.mantisrx.common.metrics.spectator.MetricGroupId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;


public class TimedChunker<T> implements Callable<Void> {

    private static final ThreadFactory namedFactory = new NamedThreadFactory("TimedChunkerGroup");
    private final MonitoredQueue<T> buffer;
    private final ChunkProcessor<T> processor;
    private final ScheduledExecutorService scheduledService = Executors.newSingleThreadScheduledExecutor(namedFactory);
    private final int maxBufferLength;
    private final int maxTimeMSec;
    private final ConnectionManager<T> connectionManager;
    private final List<T> internalBuffer;

    private final Counter interrupted;
    private final Counter numEventsDrained;
    private final Counter drainTriggeredByTimer;
    private final Counter drainTriggeredByBatch;

    public TimedChunker(MonitoredQueue<T> buffer, int maxBufferLength,
                        int maxTimeMSec, ChunkProcessor<T> processor,
                        ConnectionManager<T> connectionManager) {
        this.maxBufferLength = maxBufferLength;
        this.maxTimeMSec = maxTimeMSec;
        this.buffer = buffer;
        this.processor = processor;
        this.connectionManager = connectionManager;
        this.internalBuffer = new ArrayList<>(maxBufferLength);

        MetricGroupId metricsGroup = new MetricGroupId("TimedChunker");
        Metrics metrics = new Metrics.Builder()
            .id(metricsGroup)
            .addCounter("interrupted")
            .addCounter("numEventsDrained")
            .addCounter("drainTriggeredByTimer")
            .addCounter("drainTriggeredByBatch")
            .build();
        interrupted = metrics.getCounter("interrupted");
        numEventsDrained = metrics.getCounter("numEventsDrained");
        drainTriggeredByTimer = metrics.getCounter("drainTriggeredByTimer");
        drainTriggeredByBatch = metrics.getCounter("drainTriggeredByBatch");
        MetricsRegistry.getInstance().registerAndGet(metrics);
    }

    @Override
    public Void call() throws Exception {
        ScheduledFuture periodicDrain = scheduledService.scheduleAtFixedRate(() -> {
            drainTriggeredByTimer.increment();
            drain();
            }, maxTimeMSec, maxTimeMSec, TimeUnit.MILLISECONDS);
        while (!stopCondition()) {
            try {
                T data = buffer.get();
                synchronized (internalBuffer) {
                    internalBuffer.add(data);
                }
                if (internalBuffer.size() >= maxBufferLength) {
                    drainTriggeredByBatch.increment();
                    drain();
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                periodicDrain.cancel(true);
                interrupted.increment();
            }
        }
        drain();
        return null;
    }

    private boolean stopCondition() {
        return Thread.currentThread().isInterrupted();
    }

    private void drain() {
        if (!internalBuffer.isEmpty()) {
            List<T> copy = new ArrayList<>(internalBuffer.size());
            synchronized (internalBuffer) {
                // internalBuffer content may have changed since acquiring the lock.
                copy.addAll(internalBuffer);
                internalBuffer.clear();
            }
            if (!copy.isEmpty()) {
                processor.process(connectionManager, copy);
                numEventsDrained.increment(copy.size());
            }
        }
    }
}
