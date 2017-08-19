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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * PolicyFilters are used to layer finer grained behaviour on top of a more basic policy.
 * <p>
 * These are applied with the builder.
 */
public interface PolicyFilter {

    boolean applies(LocalDate today);

    /**
     * Create a filter for particular days of the week.
     *
     * @param days - varargs of days of the week from java.time.
     *
     * @return A filter which only allows the selected days of the week.
     */
    static PolicyFilter daysOfWeek(DayOfWeek... days) {
        final Set<DayOfWeek> fallingDays = Stream.of(days).collect(Collectors.toSet());
        return today -> fallingDays.contains(today.getDayOfWeek());
    }

    /**
     * Wraps another filter, negating it.
     * <p>
     * For instance, you could create a filter with daysOfWeek(SATURDAY, SUNDAY) and wrap
     * it in not(), to create a filter that only allows weekdays.
     *
     * @param other - the filter to negate
     *
     * @return the resulting negating filter.
     */
    static PolicyFilter not(PolicyFilter other) {
        return today -> ! other.applies(today);
    }
}
