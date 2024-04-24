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

package io.reactivx.common.consistenthashing;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import io.mantisrx.common.network.ConsistentHash;
import io.mantisrx.common.network.Endpoint;
import io.mantisrx.common.network.EndpointConfiguration;
import io.mantisrx.common.network.HashFunctions;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;


public class ConsistentHashTest {

    @Test
    public void oneNodeTest() {
        Endpoint n1 = new Endpoint("host1", 7001);

        List<Endpoint> nodes = new ArrayList<>();
        nodes.add(n1);

        ConsistentHash<Endpoint> ch =
                new ConsistentHash<>(HashFunctions.ketama(), new EndpointConfiguration(), nodes);
        int hostHitCountNode1 = 0;
        int nonHitCount = 0;

        int msgCount = 100000;
        for (int i = 0; i < msgCount; i++) {

            Endpoint sn = ch.get(("msg:" + i).getBytes());
            if ("host1".equals(sn.getHost())) {
                hostHitCountNode1++;
            } else {
                nonHitCount++;
            }
        }
        assertTrue(nonHitCount == 0);
        assertEquals(msgCount, hostHitCountNode1);
    }

    @Test
    public void emptyNodeThrowsTest() {
        List<Endpoint> nodes = new ArrayList<>();
        try {

            ConsistentHash<Endpoint> ch =
                    new ConsistentHash<>(HashFunctions.ketama(), new EndpointConfiguration(), nodes);
            fail();

        } catch (Exception e) {

        }
    }

    @Test
    public void twoNodeTest() {
        Endpoint n1 = new Endpoint("host1", 7001);
        Endpoint n2 = new Endpoint("host2", 7001);
        List<Endpoint> nodes = new ArrayList<>();
        nodes.add(n1);
        nodes.add(n2);

        ConsistentHash<Endpoint> ch =
                new ConsistentHash<>(HashFunctions.ketama(), new EndpointConfiguration(), nodes);
        int hostHitCountNode1 = 0;
        int hostHitCountNode2 = 0;
        int nonHitCount = 0;

        int msgCount = 100000;
        for (int i = 0; i < msgCount; i++) {

            Endpoint sn = ch.get(("msg:" + i).getBytes());
            if ("host1".equals(sn.getHost())) {
                hostHitCountNode1++;
            } else if ("host2".equals(sn.getHost())) {
                hostHitCountNode2++;
            } else {
                nonHitCount++;
            }
        }

        double host1HitPercentage = (double) hostHitCountNode1 / (double) msgCount;
        System.out.println("host1 hit % " + host1HitPercentage);
        assertTrue(host1HitPercentage > 0.48 && host1HitPercentage < 0.52);

        assertTrue(nonHitCount == 0);
        assertEquals(msgCount, hostHitCountNode1 + hostHitCountNode2);
    }


}
