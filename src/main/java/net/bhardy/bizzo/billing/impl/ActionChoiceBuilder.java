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
            public boolean isDueOn(LocalDate today) {
                return underlyingPolicy.isDueOn(today) && filter.applies(today);
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

    private List<LocalDate> filterDates(LocalDate day, int howMany, Kind actionKind) {
        List<LocalDate> considering = underlyingPolicy.upcomingDueDates(day, howMany);
        List<LocalDate> filtered = new ArrayList<>();
        for (LocalDate when : considering) {
            when = adjustDateIfNeeded(when, actionKind);
            if (when != null && !when.isBefore(day)) {
                filtered.add(when);
            }
        }
        return filtered;
    }

    private LocalDate adjustDateIfNeeded(LocalDate when, Kind actionKind) {
        while (when != null && !filter.applies(when)) {
            switch (actionKind) {
                case PREVIOUS_DAY:
                    when = when.minusDays(1);
                    break;
                case NEXT_DAY:
                    when = when.plusDays(1);
                    break;
                case SKIP:
                    when = null;
                    break;
            }
        }
        return when;
    }
}
