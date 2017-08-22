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

import org.junit.Ignore;
import org.junit.Test;

import java.time.LocalDate;
import java.util.List;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static net.bhardy.bizzo.billing.ActionChoice.Kind.NEXT_DAY;
import static net.bhardy.bizzo.billing.PolicyFilter.daysOfWeek;
import static net.bhardy.bizzo.billing.PolicyFilter.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class PolicyBuilderTest {
    private final LocalDate friday4th = LocalDate.of(2017, 8, 4);
    private final LocalDate saturday5th = LocalDate.of(2017, 8, 5);
    private final LocalDate sunday6th = LocalDate.of(2017, 8, 6);
    private final LocalDate monday7th = LocalDate.of(2017, 8, 7);
    private final LocalDate tuesday8th = LocalDate.of(2017, 8, 8);

    @Test
    public void cycleType_monthlySimple() {
        BillingPolicy policy = BillingPolicy.builder()
                .monthlyOnDay(5)
                .build();

        assertEquals(CycleType.MONTHLY, policy.getCycleType());
    }

    @Test
    public void cycleType_monthlyFiltered() {
        BillingPolicy policy = BillingPolicy.builder()
                .monthlyOnDay(5)
                .filter(not(daysOfWeek(WEDNESDAY)))
                .action(NEXT_DAY)
                .build();

        assertEquals(CycleType.MONTHLY, policy.getCycleType());
    }

    @Test
    public void cycleType_weeklySimple() {
        BillingPolicy policy = BillingPolicy.builder()
                .weeklyOnDay(FRIDAY)
                .build();

        assertEquals(CycleType.WEEKLY, policy.getCycleType());
    }

    @Test
    public void cycleType_weeklyFiltered() {
        BillingPolicy policy = BillingPolicy.builder()
                .weeklyOnDay(FRIDAY)
                .filter(not(daysOfWeek(WEDNESDAY)))
                .action(NEXT_DAY)
                .build();

        assertEquals(CycleType.WEEKLY, policy.getCycleType());
    }

    @Test
    public void cycleType_dailySimple() {
        BillingPolicy policy = BillingPolicy.builder()
                .daily()
                .build();

        assertEquals(CycleType.DAILY, policy.getCycleType());
    }

    @Test
    public void cycleType_dailyFiltered() {
        BillingPolicy policy = BillingPolicy.builder()
                .daily()
                .filter(not(daysOfWeek(WEDNESDAY)))
                .action(NEXT_DAY)
                .build();

        assertEquals(CycleType.DAILY, policy.getCycleType());
    }

    @Test
    public void isDueOn_weeklySimple() {
        BillingPolicy policy = BillingPolicy.builder()
                .weeklyOnDay(SATURDAY)
                .build();

        assertTrue(policy.isDueOn(saturday5th));
    }

    @Test
    public void isDueOn_weeklyFiltered() {
        BillingPolicy policy = BillingPolicy.builder()
                .weeklyOnDay(FRIDAY)
                .filter(not(daysOfWeek(SATURDAY, SUNDAY)))
                .action(NEXT_DAY)
                .build();

        assertFalse(policy.isDueOn(saturday5th));
        assertFalse(policy.isDueOn(monday7th));
    }

    @Test
    public void isDueOn_monthlySimple() {
        BillingPolicy policy = BillingPolicy.builder()
                .monthlyOnDay(5)
                .build();

        assertTrue(policy.isDueOn(saturday5th));
        assertTrue(policy.isDueOn(LocalDate.of(2017, 12, 5)));
    }

    @Test
    public void isDueOn_monthlyFiltered() {
        BillingPolicy policy = BillingPolicy.builder()
                .monthlyOnDay(5)
                .filter(not(daysOfWeek(SATURDAY, SUNDAY)))
                .action(NEXT_DAY)
                .build();

        assertFalse(policy.isDueOn(saturday5th));
        assertFalse(policy.isDueOn(sunday6th));
        assertTrue(policy.isDueOn(monday7th));
    }

    @Test
    public void upcomingDueDates_dailySimple() {
        BillingPolicy policy = BillingPolicy.builder()
                .daily()
                .build();

        List<LocalDate> nextBills = policy.upcomingDueDates(saturday5th, 2);

        assertEquals(saturday5th, nextBills.get(0));
        assertEquals(sunday6th, nextBills.get(1));
    }

    @Ignore  // this reveals an underlying bug we need to fix
    @Test
    public void upcomingDueDates_dailyFiltered() {
        BillingPolicy policy = BillingPolicy.builder()
                .daily()
                .filter(not(daysOfWeek(SATURDAY, SUNDAY)))
                .action(NEXT_DAY)
                .build();

        List<LocalDate> nextBills = policy.upcomingDueDates(saturday5th, 2);

        assertEquals(monday7th, nextBills.get(0));
        assertEquals(tuesday8th, nextBills.get(1));
    }

    @Test
    public void upcomingDueDates_weeklySimple() {
        LocalDate friday11th = friday4th.plusWeeks(1);

        BillingPolicy policy = BillingPolicy.builder()
                .weeklyOnDay(FRIDAY)
                .build();
        List<LocalDate> nextBills = policy.upcomingDueDates(friday4th, 2);

        assertEquals(friday4th, nextBills.get(0));
        assertEquals(friday11th, nextBills.get(1));
    }

    @Test
    public void upcomingDueDates_weeklyFiltered() {
        BillingPolicy policy = BillingPolicy.builder()
                .weeklyOnDay(SATURDAY)
                .filter(not(daysOfWeek(SUNDAY)))
                .action(NEXT_DAY)
                .build();
        List<LocalDate> nextBills = policy.upcomingDueDates(saturday5th, 2);

        assertEquals(saturday5th, nextBills.get(0));

        LocalDate saturday12th = LocalDate.of(2017, 8, 12);
        assertEquals(saturday12th, nextBills.get(1));
    }

    @Test
    public void upcomingDueDates_monthlySimple() {
        BillingPolicy policy = BillingPolicy.builder()
                .monthlyOnDay(5)
                .build();

        List<LocalDate> nextBills = policy.upcomingDueDates(saturday5th, 2);

        assertEquals(saturday5th, nextBills.get(0));

        LocalDate secondExpected = LocalDate.of(2017, 9, 5);
        assertEquals(secondExpected, nextBills.get(1));
    }

    @Test
    public void upcomingDueDates_monthlyFiltered() {
        BillingPolicy policy = BillingPolicy.builder()
                .monthlyOnDay(5)
                .filter(not(daysOfWeek(SATURDAY, SUNDAY)))
                .action(NEXT_DAY)
                .build();

        List<LocalDate> nextBills = policy.upcomingDueDates(saturday5th, 2);

        LocalDate actual1 = nextBills.get(0);
        assertEquals(monday7th, actual1);

        LocalDate expected2 = LocalDate.of(2017, 9, 5);
        LocalDate actual2 = nextBills.get(1);
        assertEquals(expected2, actual2);
        assertEquals(TUESDAY, actual2.getDayOfWeek());
    }
}
