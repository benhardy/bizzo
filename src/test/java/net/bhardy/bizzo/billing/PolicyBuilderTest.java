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

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static java.util.stream.Collectors.toList;
import static net.bhardy.bizzo.billing.ActionChoice.Kind.NEXT_DAY;
import static net.bhardy.bizzo.billing.ActionChoice.Kind.PREVIOUS_DAY;
import static net.bhardy.bizzo.billing.PolicyFilter.daysOfWeek;
import static net.bhardy.bizzo.billing.PolicyFilter.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 */
@SuppressWarnings("squid:S1118") // don't care if PolicyBuilderTest constructor is public
public class PolicyBuilderTest {
    private static final LocalDate friday4th = LocalDate.of(2017, 8, 4);
    private static final LocalDate saturday5th = LocalDate.of(2017, 8, 5);
    private static final LocalDate sunday6th = LocalDate.of(2017, 8, 6);
    private static final LocalDate monday7th = LocalDate.of(2017, 8, 7);
    private static final LocalDate tuesday8th = LocalDate.of(2017, 8, 8);

    public static class CycleTypes {
        @Test
        public void monthlySimple() {
            BillingPolicy policy = BillingPolicy.builder()
                    .monthlyOnDay(5)
                    .build();

            assertEquals(CycleType.MONTHLY, policy.getCycleType());
        }

        @Test
        public void monthlyFiltered() {
            BillingPolicy policy = BillingPolicy.builder()
                    .monthlyOnDay(5)
                    .filter(not(daysOfWeek(WEDNESDAY)))
                    .action(NEXT_DAY)
                    .build();

            assertEquals(CycleType.MONTHLY, policy.getCycleType());
        }

        @Test
        public void weeklySimple() {
            BillingPolicy policy = BillingPolicy.builder()
                    .weeklyOnDay(FRIDAY)
                    .build();

            assertEquals(CycleType.WEEKLY, policy.getCycleType());
        }

        @Test
        public void weeklyFiltered() {
            BillingPolicy policy = BillingPolicy.builder()
                    .weeklyOnDay(FRIDAY)
                    .filter(not(daysOfWeek(WEDNESDAY)))
                    .action(NEXT_DAY)
                    .build();

            assertEquals(CycleType.WEEKLY, policy.getCycleType());
        }

        @Test
        public void dailySimple() {
            BillingPolicy policy = BillingPolicy.builder()
                    .daily()
                    .build();

            assertEquals(CycleType.DAILY, policy.getCycleType());
        }

        @Test
        public void dailyFiltered() {
            BillingPolicy policy = BillingPolicy.builder()
                    .daily()
                    .filter(not(daysOfWeek(WEDNESDAY)))
                    .action(NEXT_DAY)
                    .build();

            assertEquals(CycleType.DAILY, policy.getCycleType());
        }
    }

    public static class IsDueOn {
        @Test
        public void weeklySimple() {
            BillingPolicy policy = BillingPolicy.builder()
                    .weeklyOnDay(SATURDAY)
                    .build();

            assertTrue(policy.isDueOn(saturday5th));
        }

        @Test
        public void weeklyFiltered() {
            BillingPolicy policy = BillingPolicy.builder()
                    .weeklyOnDay(FRIDAY)
                    .filter(not(daysOfWeek(SATURDAY, SUNDAY)))
                    .action(NEXT_DAY)
                    .build();

            assertFalse(policy.isDueOn(saturday5th));
            assertFalse(policy.isDueOn(monday7th));
        }

        @Test
        public void monthlySimple() {
            BillingPolicy policy = BillingPolicy.builder()
                    .monthlyOnDay(5)
                    .build();

            assertTrue(policy.isDueOn(saturday5th));
            assertTrue(policy.isDueOn(LocalDate.of(2017, 12, 5)));
        }

        @Test
        public void monthlyFiltered() {
            BillingPolicy policy = BillingPolicy.builder()
                    .monthlyOnDay(5)
                    .filter(not(daysOfWeek(SATURDAY, SUNDAY)))
                    .action(NEXT_DAY)
                    .build();

            assertFalse(policy.isDueOn(saturday5th));
            assertFalse(policy.isDueOn(sunday6th));
            assertTrue(policy.isDueOn(monday7th));
        }
    }

    public static class UpcomingDueDates {
        @Test
        public void dailySimple() {
            BillingPolicy policy = BillingPolicy.builder()
                    .daily()
                    .build();

            List<LocalDate> nextBills = policy.upcomingDueDates(saturday5th).limit(2).collect(toList());

            assertEquals(saturday5th, nextBills.get(0));
            assertEquals(sunday6th, nextBills.get(1));
        }

        @Test
        public void dailyFiltered() {
            BillingPolicy policy = BillingPolicy.builder()
                    .daily()
                    .filter(not(daysOfWeek(SATURDAY, SUNDAY)))
                    .action(NEXT_DAY)
                    .build();

            List<LocalDate> nextBills = policy.upcomingDueDates(saturday5th).limit(2).collect(toList());

            assertEquals(monday7th, nextBills.get(0));
            assertEquals(tuesday8th, nextBills.get(1));
        }

        @Test
        public void dailyFilteredBackward() {
            BillingPolicy policy = BillingPolicy.builder()
                    .daily()
                    .filter(not(daysOfWeek(SATURDAY, SUNDAY)))
                    .action(NEXT_DAY)
                    .build();

            List<LocalDate> nextBills = policy.upcomingDueDates(friday4th).limit(4).collect(toList());

            assertEquals(friday4th, nextBills.get(0));
            assertEquals(monday7th, nextBills.get(1));
            assertEquals(tuesday8th, nextBills.get(2));
        }

        @Test
        public void weeklySimple() {
            LocalDate friday11th = friday4th.plusWeeks(1);

            BillingPolicy policy = BillingPolicy.builder()
                    .weeklyOnDay(FRIDAY)
                    .build();
            List<LocalDate> nextBills = policy.upcomingDueDates(friday4th).limit(2).collect(toList());

            assertEquals(friday4th, nextBills.get(0));
            assertEquals(friday11th, nextBills.get(1));
        }

        @Test
        public void weeklyFilteredNoop() {
            BillingPolicy policy = BillingPolicy.builder()
                    .weeklyOnDay(SATURDAY)
                    .filter(not(daysOfWeek(SUNDAY)))
                    .action(NEXT_DAY)
                    .build();
            List<LocalDate> nextBills = policy.upcomingDueDates(saturday5th).limit(2).collect(toList());

            assertEquals(saturday5th, nextBills.get(0));

            LocalDate saturday12th = LocalDate.of(2017, 8, 12);
            assertEquals(saturday12th, nextBills.get(1));
        }

        @Test
        public void weeklyFilteredShift() {
            // i want saturdays but i don't actually like them...
            BillingPolicy policy = BillingPolicy.builder()
                    .weeklyOnDay(SATURDAY)
                    .filter(not(daysOfWeek(SATURDAY)))
                    .action(NEXT_DAY)
                    .build();
            List<LocalDate> nextBills = policy.upcomingDueDates(saturday5th).limit(2).collect(toList());

            assertEquals(sunday6th, nextBills.get(0));

            LocalDate sunday13th = LocalDate.of(2017, 8, 13);
            assertEquals(sunday13th, nextBills.get(1));
        }

        @Test
        public void weeklyFilteredBackwardNoop() {
            BillingPolicy policy = BillingPolicy.builder()
                    .weeklyOnDay(SATURDAY)
                    .filter(not(daysOfWeek(SUNDAY)))
                    .action(NEXT_DAY)
                    .build();
            List<LocalDate> nextBills = policy.upcomingDueDates(saturday5th).limit(2).collect(toList());

            LocalDate saturday12th = LocalDate.of(2017, 8, 12);
            assertEquals(saturday5th, nextBills.get(0));
            assertEquals(saturday12th, nextBills.get(1));
        }

        @Test
        public void weeklyFilteredBackwardShift() {
            // i want saturdays but i don't actually like them...
            BillingPolicy policy = BillingPolicy.builder()
                    .weeklyOnDay(SATURDAY)
                    .filter(not(daysOfWeek(SATURDAY)))
                    .action(PREVIOUS_DAY)
                    .build();
            List<LocalDate> nextBills = policy.upcomingDueDates(friday4th).limit(2).collect(toList());

            LocalDate friday11th = LocalDate.of(2017, 8, 11);
            assertEquals(friday4th, nextBills.get(0));
            assertEquals(friday11th, nextBills.get(1));
        }

        @Test
        public void monthlySimple() {
            BillingPolicy policy = BillingPolicy.builder()
                    .monthlyOnDay(5)
                    .build();

            List<LocalDate> nextBills = policy.upcomingDueDates(saturday5th).limit(2).collect(toList());

            assertEquals(saturday5th, nextBills.get(0));

            LocalDate secondExpected = LocalDate.of(2017, 9, 5);
            assertEquals(secondExpected, nextBills.get(1));
        }

        @Test
        public void monthlyFiltered() {
            BillingPolicy policy = BillingPolicy.builder()
                    .monthlyOnDay(5)
                    .filter(not(daysOfWeek(SATURDAY, SUNDAY)))
                    .action(NEXT_DAY)
                    .build();

            List<LocalDate> nextBills = policy.upcomingDueDates(saturday5th).limit(2).collect(toList());

            LocalDate actual1 = nextBills.get(0);
            assertEquals(monday7th, actual1);

            LocalDate expected2 = LocalDate.of(2017, 9, 5);
            LocalDate actual2 = nextBills.get(1);
            assertEquals(expected2, actual2);
            assertEquals(TUESDAY, actual2.getDayOfWeek());
        }

        @Test
        public void monthlyFilteredBackwards() {
            BillingPolicy policy = BillingPolicy.builder()
                    .monthlyOnDay(5)
                    .filter(not(daysOfWeek(SATURDAY, SUNDAY)))
                    .action(PREVIOUS_DAY)
                    .build();

            LocalDate thursday3rd = LocalDate.of(2017, 8, 3);
            List<LocalDate> nextBills = policy.upcomingDueDates(thursday3rd).limit(5).collect(toList());

            LocalDate actual1 = nextBills.get(0);
            assertEquals(friday4th, actual1);

            LocalDate expected2 = LocalDate.of(2017, 9, 5);
            LocalDate actual2 = nextBills.get(1);
            assertEquals(expected2, actual2);
            assertEquals(TUESDAY, actual2.getDayOfWeek());

            LocalDate expected3 = LocalDate.of(2017, 10, 5);
            LocalDate actual3 = nextBills.get(2);
            assertEquals(expected3, actual3);
            assertEquals(THURSDAY, actual3.getDayOfWeek());

            LocalDate expected4 = LocalDate.of(2017, 11, 3);  // 5th is a sunday here so go back to Friday
            LocalDate actual4 = nextBills.get(3);
            assertEquals(expected4, actual4);
            assertEquals(FRIDAY, actual4.getDayOfWeek());

            LocalDate expected5 = LocalDate.of(2017, 12, 5);
            LocalDate actual5 = nextBills.get(4);
            assertEquals(expected5, actual5);
            assertEquals(TUESDAY, actual5.getDayOfWeek());
        }

        @Test
        public void filterPreventsNextDate() {
            PolicyFilter avoidMidMonth = day -> day.getDayOfMonth() <= 5 || day.getDayOfMonth() > 20;

            BillingPolicy policy = BillingPolicy.builder()
                    .weeklyOnDay(SATURDAY)
                    .filter(avoidMidMonth)
                    .action(NEXT_DAY)
                    .build();
            List<LocalDate> nextBills = policy.upcomingDueDates(saturday5th).limit(3).collect(toList());

            assertEquals(saturday5th, nextBills.get(0));

            // first chance we get out of the filter
            assertEquals(LocalDate.of(2017, 8, 21), nextBills.get(1));

            // next regularly scheduled chance we get
            assertEquals(LocalDate.of(2017, 8, 26), nextBills.get(2));
        }

        @Test
        public void filterPreventsFirstDate() {
            PolicyFilter avoidMidMonth = day -> day.getDayOfMonth() <= 3 || day.getDayOfMonth() > 20;

            BillingPolicy policy = BillingPolicy.builder()
                    .weeklyOnDay(SATURDAY)
                    .filter(avoidMidMonth)
                    .action(NEXT_DAY)
                    .build();
            List<LocalDate> nextBills = policy.upcomingDueDates(saturday5th).limit(2).collect(toList());

            // first chance we get out of the filter
            assertEquals(LocalDate.of(2017, 8, 21), nextBills.get(0));

            // next regularly scheduled chance we get
            assertEquals(LocalDate.of(2017, 8, 26), nextBills.get(1));
        }
    }
}
