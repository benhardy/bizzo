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
import java.util.ArrayList;
import java.util.List;

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
            @Override
            public boolean isDueOn(final LocalDate today) {
                if (!filter.applies(today)) {
                    return false;
                }
                if (underlyingPolicy.isDueOn(today)) {
                    return true;
                }
                LocalDate original = findOriginalDate(today, actionKind);
                return underlyingPolicy.isDueOn(original);
            }

            @Override
            public List<LocalDate> upcomingDueDates(LocalDate day, int howMany) {
                return filterDates(day, howMany, actionKind);
            }

            @Override
            public CycleType getCycleType() {
                return underlyingPolicy.getCycleType();
            }
        };
        return policyBuilder.buildFilterOption(layered);
    }

    private LocalDate findOriginalDate(LocalDate today, Kind kind) {
        final int delta;
        if (kind == Kind.NEXT_DAY) {
            delta = -1;
        } else if (kind == Kind.PREVIOUS_DAY) {
            delta = 1;
        } else {
            throw new IllegalArgumentException("unwanted kind " + kind);
        }
        LocalDate original = today;
        LocalDate check = today.plusDays(delta);
        while (!filter.applies(check)) {
            original = check;
            check = check.plusDays(delta);
        }
        return original;
    }

    private List<LocalDate> filterDates(LocalDate day, int howMany, Kind actionKind) {
        List<LocalDate> considering = underlyingPolicy.upcomingDueDates(day, howMany);
        List<LocalDate> filtered = new ArrayList<>();
        for (LocalDate when : considering) {
            when = adjustDateIfNeeded(when, actionKind, false);
            if (when != null && !when.isBefore(day)) {
                filtered.add(when);
            }
        }
        return filtered;
    }

    private LocalDate adjustDateIfNeeded(LocalDate when, Kind actionKind, boolean reversed) {
        final int delta = reversed ? -1 : 1;
        while (when != null && !filter.applies(when)) {
            switch (actionKind) {
                case PREVIOUS_DAY:
                    when = when.minusDays(delta);
                    break;
                case NEXT_DAY:
                    when = when.plusDays(delta);
                    break;
                case SKIP:
                    when = null;
                    break;
            }
        }
        return when;
    }
}
