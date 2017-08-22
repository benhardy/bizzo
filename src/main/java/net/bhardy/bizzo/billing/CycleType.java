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

import java.time.Period;

/**
 * Billing cycles typically fall roughly into the following time cycles.
 * <p>
 * You can use PolicyFilters to make behaviour more specific.
 */
public enum CycleType {

    DAILY(Period.ofDays(1)),
    WEEKLY(Period.ofWeeks(1)),
    BIWEEKLY(Period.ofWeeks(2)),
    //SEMIMONTHLY(Period.ofDays(1)),
    MONTHLY(Period.ofMonths(1)),
    BIMONTHLY(Period.ofMonths(2)),
    QUARTERLY(Period.ofMonths(3)),
    SEMIANNUALLY(Period.ofMonths(6)),
    ANNUALLY(Period.ofYears(1));

    private final transient Period period;

    CycleType(Period period) {
        this.period = period;
    }

    public Period getPeriod() {
        return period;
    }
}
