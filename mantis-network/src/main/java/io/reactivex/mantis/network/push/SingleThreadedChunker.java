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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;


public class SingleThreadedChunker<T> implements Callable<Void> {

    final MonitoredQueue<T> inputQueue;
    final int TIME_PROBE_COUNT = 100000;
    private final int chunkSize;
    private final long maxChunkInterval;
    private final ConnectionManager<T> connectionManager;
    private final ChunkProcessor<T> processor;
    private final Object[] chunk;
    int iteration;
    private int index;

    public SingleThreadedChunker(ChunkProcessor<T> processor, MonitoredQueue<T> iQ, int chunkSize, long maxChunkInterval, ConnectionManager<T> connMgr) {
        this.inputQueue = iQ;
        this.chunkSize = chunkSize;
        this.maxChunkInterval = maxChunkInterval;
        this.processor = processor;
        this.connectionManager = connMgr;
        chunk = new Object[this.chunkSize];

    }

    private boolean stopCondition() {
        return Thread.currentThread().isInterrupted();
    }

    @Override
    public Void call() throws Exception {


        long chunkStartTime = System.currentTimeMillis();
        while (true) {
            iteration++;
            if (iteration == TIME_PROBE_COUNT) {

                long currTime = System.currentTimeMillis();
                if (currTime - maxChunkInterval > chunkStartTime) {
                    drain();
                }
                iteration = 0;
                if (stopCondition()) {
                    break;
                }
            }

            if (index < this.chunkSize) {
                T ele = inputQueue.poll();
                if (ele != null) {

                    chunk[index++] = ele;
                }
            } else {
                drain();
                chunkStartTime = System.currentTimeMillis();
                if (stopCondition()) {
                    break;
                }
            }

        }
        return null;
    }

    private void drain() {

        if (index > 0) {
            List<T> copy = new ArrayList<>(index);
            for (int i = 0; i < index; i++) {
                copy.add((T) chunk[i]);
            }

            processor.process(connectionManager, copy);
            index = 0;
        }
    }

}
