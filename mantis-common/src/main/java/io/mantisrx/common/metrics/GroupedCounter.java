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

package io.mantisrx.common.metrics;

public abstract class GroupedCounter {

    private final String familyName;

    public GroupedCounter(String familyName, String... events) {
        this.familyName = familyName;
    }

    public String familyName() {
        return familyName;
    }

    public abstract void increment(String event);

    public abstract long count(String event);

    public abstract long rateCount(String event);

    public abstract long rateTimeInMilliseconds();
}
