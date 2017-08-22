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
package net.bhardy.bizzo.billing.impl;

import net.bhardy.bizzo.billing.ActionChoice;
import net.bhardy.bizzo.billing.BillingPolicy;
import net.bhardy.bizzo.billing.CycleType;
import net.bhardy.bizzo.billing.FilterOption;
import net.bhardy.bizzo.billing.PolicyBuilder;
import net.bhardy.bizzo.billing.PolicyFilter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

/**
 * Internal implementation of the PolicyBuilder.
 *
 * @see net.bhardy.bizzo.billing.PolicyBuilder
 */
public class PolicyBuilderImpl implements PolicyBuilder {
    @Override
    public FilterOption daily() {
        BillingPolicy base = new BillingPolicy() {

            @Override
            public boolean isDueOn(LocalDate day) {
                return true;
            }

            @Override
            public Stream<LocalDate> upcomingDueDates(final LocalDate startingFrom) {
                final AtomicReference<LocalDate> current = new AtomicReference<>(startingFrom);
                return Stream.generate(() -> current.getAndUpdate(now -> now.plusDays(1)));
            }

            @Override
            public CycleType getCycleType() {
                return CycleType.DAILY;
            }
        };

        return buildFilterOption(base);
    }

    private static final int DAYS_PER_WEEK = 7;

    @Override
    public FilterOption weeklyOnDay(DayOfWeek onWhichDay) {
        BillingPolicy base = new BillingPolicy() {

            @Override
            public boolean isDueOn(LocalDate day) {
                return day.getDayOfWeek().equals(onWhichDay);
            }

            @Override
            public Stream<LocalDate> upcomingDueDates(final LocalDate fromDay) {
                final DayOfWeek todayIsA = fromDay.getDayOfWeek();
                final int daysTilNext = (onWhichDay.getValue() + DAYS_PER_WEEK - todayIsA.getValue()) % DAYS_PER_WEEK;
                final LocalDate first = fromDay.plusDays(daysTilNext);
                final AtomicReference<LocalDate> current = new AtomicReference<>(first);
                return Stream.generate(() -> current.getAndUpdate(now -> now.plusWeeks(1)));
            }

            @Override
            public CycleType getCycleType() {
                return CycleType.WEEKLY;
            }
        };

        return buildFilterOption(base);
    }

    @Override
    public FilterOption monthlyOnDay(int dayOfMonth) {
        BillingPolicy base = new BillingPolicy() {
            @Override
            public boolean isDueOn(LocalDate day) {
                return day.getDayOfMonth() == dayOfMonth;
            }

            @Override
            public Stream<LocalDate> upcomingDueDates(final LocalDate fromDay) {
                final int monthOffset = (fromDay.getDayOfMonth() > dayOfMonth) ? 1 : 0;
                final LocalDate first = fromDay.withDayOfMonth(dayOfMonth).plusMonths(monthOffset);
                final AtomicReference<LocalDate> current = new AtomicReference<>(first);
                return Stream.generate(() -> current.getAndUpdate(now -> now.plusMonths(1)));
            }

            @Override
            public CycleType getCycleType() {
                return CycleType.MONTHLY;
            }
        };

        return buildFilterOption(base);

    }

    FilterOption buildFilterOption(BillingPolicy underlyingPolicy) {
        return new FilterOption() {
            @Override
            public BillingPolicy build() {
                return underlyingPolicy;
            }

            @Override
            public ActionChoice filter(PolicyFilter filter) {
                return new ActionChoiceBuilder(PolicyBuilderImpl.this, underlyingPolicy, filter);
            }
        };
    }
}
