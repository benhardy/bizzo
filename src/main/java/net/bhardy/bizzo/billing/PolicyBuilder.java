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

/**
 * Starting points for creating filters, based on the basic CycleTypes.
 */
public interface PolicyBuilder {
    /**
     * This bill will be due every single day. Unless you do some filtering.
     */
    FilterOption everyDay();

    /**
     * This bill will be due once a a week on the specified day of week.
     */
    FilterOption weeklyOnDay(DayOfWeek onWhichDay);

    /**
     * This bill will be due once a a month on the specified day of month (number).
     */
    FilterOption monthlyOnDay(int dayOfMonth);
}
