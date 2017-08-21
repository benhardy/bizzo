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
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

/**
 * Internal implementation of the PolicyBuilder.
 *
 * @see net.bhardy.bizzo.billing.PolicyBuilder
 */
public class PolicyBuilderImpl implements PolicyBuilder {
    @Override
    public FilterOption everyDay() {
        BillingPolicy base = new BillingPolicy() {

            @Override
            public boolean isDueOn(LocalDate day) {
                return true;
            }

            @Override
            public List<LocalDate> upcomingDueDates(LocalDate day, int howMany) {
                List<LocalDate> results = new ArrayList<>();
                for (int num = 0; num < howMany; num++) {
                    results.add(day.plusDays(num));
                }
                return results;
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
            public List<LocalDate> upcomingDueDates(LocalDate day, int howMany) {
                DayOfWeek todayIsA = day.getDayOfWeek();
                int daysTilNext = (onWhichDay.getValue() + DAYS_PER_WEEK - todayIsA.getValue()) % DAYS_PER_WEEK;
                LocalDate first = day.plusDays(daysTilNext);
                List<LocalDate> results = new ArrayList<>();
                for (int num = 0; num < howMany; num++) {
                    results.add(first.plusWeeks(num));
                }
                return results;
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
            public List<LocalDate> upcomingDueDates(LocalDate day, int howMany) {
                final int monthOffset = (day.getDayOfMonth() > dayOfMonth) ? 1 : 0;
                final LocalDate first = day.withDayOfMonth(dayOfMonth).plusMonths(monthOffset);
                return range(0, howMany).mapToObj(first::plusMonths).collect(toList());
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
