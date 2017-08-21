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

import org.junit.Test;

import java.time.LocalDate;
import java.util.List;

import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.DayOfWeek.TUESDAY;
import static net.bhardy.bizzo.billing.PolicyFilter.daysOfWeek;
import static net.bhardy.bizzo.billing.PolicyFilter.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class PolicyBuilderTest {
    @Test
    public void isDueOn_monthlySimple() {
        BillingPolicy policy = BillingPolicy.builder()
                .sameDayEveryMonth(5)
                .build();

        LocalDate oneSaturday = LocalDate.of(2017, 8, 5);
        assertTrue(policy.isDueOn(oneSaturday));
    }
    @Test
    public void isDueOn_monthlyFiltered() {
        BillingPolicy policy = BillingPolicy.builder()
                .sameDayEveryMonth(5)
                .filter(not(daysOfWeek(SATURDAY, SUNDAY)))
                .action(ActionChoice.Kind.NEXT_DAY)
                .build();

        LocalDate oneSaturday = LocalDate.of(2017, 8, 5);
        assertFalse(policy.isDueOn(oneSaturday));
        LocalDate maybeSunday = LocalDate.of(2017, 8, 6);
        assertFalse(policy.isDueOn(maybeSunday));
        LocalDate shouldBeMonday = LocalDate.of(2017, 8, 7);
        assertTrue(policy.isDueOn(shouldBeMonday));
    }

    @Test
    public void upcomingDueDates_monthlySimple() {
        BillingPolicy policy = BillingPolicy.builder()
                .sameDayEveryMonth(5)
                .build();

        LocalDate oneSaturday = LocalDate.of(2017, 8, 5);
        List<LocalDate> nextBills = policy.upcomingDueDates(oneSaturday, 2);

        LocalDate expected1 = LocalDate.of(2017, 8, 5);
        LocalDate actual1 = nextBills.get(0);
        assertEquals(expected1, actual1);
        assertEquals(SATURDAY, actual1.getDayOfWeek());

        LocalDate expected2 = LocalDate.of(2017, 9, 5);
        LocalDate actual2 = nextBills.get(1);
        assertEquals(expected2, actual2);
        assertEquals(TUESDAY, actual2.getDayOfWeek());
    }

    @Test
    public void upcomingDueDates_monthlyFiltered() {
        BillingPolicy policy = BillingPolicy.builder()
                .sameDayEveryMonth(5)
                .filter(not(daysOfWeek(SATURDAY, SUNDAY)))
                .action(ActionChoice.Kind.NEXT_DAY)
                .build();

        LocalDate oneSaturday = LocalDate.of(2017, 8, 5);

        List<LocalDate> nextBills = policy.upcomingDueDates(oneSaturday, 2);

        LocalDate expected1 = LocalDate.of(2017, 8, 7);
        LocalDate actual1 = nextBills.get(0);
        assertEquals(expected1, actual1);
        assertEquals(MONDAY, actual1.getDayOfWeek()); // just to be obvious

        LocalDate expected2 = LocalDate.of(2017, 9, 5);
        LocalDate actual2 = nextBills.get(1);
        assertEquals(expected2, actual2);
        assertEquals(TUESDAY, actual2.getDayOfWeek());
    }
}
