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

package io.mantisrx.runtime.computation;

import io.mantisrx.runtime.Context;
import rx.Observable;


public final class ScalarComputations {

    private ScalarComputations() {}

    public static <T> ScalarComputation<T, T> identity() {
        return new ScalarComputation<T, T>() {
            @Override
            public Observable<T> call(Context context, Observable<T> t1) {
                return t1;
            }
        };
    }
}
