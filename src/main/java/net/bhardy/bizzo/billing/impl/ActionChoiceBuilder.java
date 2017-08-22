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
import net.bhardy.bizzo.billing.PolicyFilter;

import java.time.LocalDate;
import java.time.Period;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

/**
 * The internal implementation of the ActionChoice.
 * <p>
 * It mostly just wraps a policy with a new one enacting the filter.
 */
final class ActionChoiceBuilder implements ActionChoice {
    private final PolicyBuilderImpl policyBuilder;
    private final BillingPolicy underlyingPolicy;
    private final PolicyFilter filter;

    ActionChoiceBuilder(PolicyBuilderImpl policyBuilder, BillingPolicy underlyingPolicy, PolicyFilter filter) {
        this.policyBuilder = policyBuilder;
        this.underlyingPolicy = underlyingPolicy;
        this.filter = filter;
    }

    @Override
    public FilterOption action(Kind actionKind) {
        BillingPolicy layered = new BillingPolicy() {
            private static final int RANGE = 12;

            @Override
            public boolean isDueOn(final LocalDate today) {
                final Period howFarBack = underlyingPolicy.getCycleType().getPeriod().multipliedBy(RANGE);
                final LocalDate jumpBack = today.minus(howFarBack);
                return upcomingDueDates(jumpBack).limit(RANGE * 2L).anyMatch(today::equals);
            }

            @Override
            public Stream<LocalDate> upcomingDueDates(LocalDate day) {
                return filterDates(day, actionKind);
            }

            @Override
            public CycleType getCycleType() {
                return underlyingPolicy.getCycleType();
            }
        };
        return policyBuilder.buildFilterOption(layered);
    }

    private Stream<LocalDate> filterDates(LocalDate startDay, final Kind actionKind) {
        final Stream<LocalDate> considering = underlyingPolicy.upcomingDueDates(startDay);
        if (actionKind == Kind.SKIP) {
            return considering.filter(filter::applies);
        }
        if (actionKind == Kind.NEXT_DAY) {
            final AtomicReference<LocalDate> latest = new AtomicReference<>(startDay);

            return considering.flatMap(day -> {
                if (latest.get().isAfter(day)) {
                    return Stream.empty();
                }
                LocalDate now = findNextFreeDate(day);
                latest.set(now.plusDays(1));
                return Stream.of(now);
            });
        }
        if (actionKind == Kind.PREVIOUS_DAY) {
            final AtomicReference<LocalDate> last = new AtomicReference<>(startDay.minusDays(1));

            return considering.flatMap(day -> {
                final LocalDate skipBack = findPreviousFreeDate(day, last.get());
                if (skipBack == null) {
                    return Stream.empty();
                }
                last.set(skipBack);
                return Stream.of(skipBack);
            });
        }
        throw new UnsupportedOperationException("unsupported ActionKind: " + actionKind);
    }

    private LocalDate findNextFreeDate(LocalDate startingAt) {
        LocalDate now = startingAt;
        while (!filter.applies(now)) {
            now = now.plusDays(1);
        }
        return now;
    }

    private LocalDate findPreviousFreeDate(LocalDate starting, LocalDate notBefore) {
        assert (starting.isAfter(notBefore));
        LocalDate now = starting;
        while (!filter.applies(now)) {
            now = now.minusDays(1);
            if (!now.isAfter(notBefore)) {
                return null;
            }
        }
        return now;
    }
}
