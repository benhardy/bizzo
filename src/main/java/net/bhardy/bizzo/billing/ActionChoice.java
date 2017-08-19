/*
 * Copyright 2017 Ben Hardy
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
package net.bhardy.bizzo.billing;

/**
 * When building a Billing Policy with filters for particular billing days,
 * an action must be chosen for what to do when that filter does not pass a
 * day in question.
 * <p>
 * ActionChoice is the stage in the Policy Builder DSL where you select which
 * kind of action you would like to take in the event of a filter not accepting
 * your condition.
 */
public interface ActionChoice {
    enum Kind {
        /**
         * Try seeing if we can skip to the previous day, to see if the filter passes.
         */
        PREVIOUS_DAY,

        /**
         * Try seeing if we can skip to the next day, to see if the filter passes.
         */
        NEXT_DAY,

        /**
         * Give up.
         */
        SKIP
    }

    FilterOption action(Kind actionKind);
}
