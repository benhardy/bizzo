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

import net.bhardy.bizzo.billing.impl.PolicyBuilderImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

/**
 * A billing policy helps you figure out billing cycles, i.e. when bills are due.
 * <p>
 * START HERE with a call to BillingPolicy.builder() to start creating a policy.
 */
public interface BillingPolicy {
    /**
     * Given a date, determine if this billing policy is falling due on that
     * date.
     * <p>
     * This takes filters into consideration.
     *
     * @param day - the date to check
     *
     * @return true if the bill is due on that date, false otherwise.
     */
    boolean isDueOn(LocalDate day);

    /**
     * Figure out which dates are upcoming due dates according to this billing
     * policy.
     * <p>
     * This takes filters into consideration, which leads to an interesting edge case:
     * If a filter that causes a date under examination to be moved to an date earlier
     * than the supplied date, it will not be returned. Which is what you'd want, since
     * it's not actually "upcoming".
     * <p>
     * This is inclusive. A bill could fall due on the first day you're checking.
     *
     * @param day - the first date to check
     *
     * @return an infinite stream of subsequent due dates, in chronological order.
     */
    Stream<LocalDate> upcomingDueDates(LocalDate day);

    /**
     * Get the rough billing cycle type for this policy, e.g. MONTHLY, WEEKLY etc.
     *
     * @return the kind of cycle.
     */
    CycleType getCycleType();

    /**
     * To create a new policy, call BillingPolicy.builder(). This will enable you to
     * fluently start building a policy.
     * <p>
     * e.g.:<br>
     * <code>
     *     BillingPolicy payDay = BillingPolicy.builder()
     *             .monthlyOnDay(15)
     *             .filter(not(daysOfWeek(SATURDAY, SUNDAY)))
     *             .build();
     * </code>
     *
     * @return the policy builder.
     */
    static PolicyBuilder builder() {
        return new PolicyBuilderImpl();
    }
}
