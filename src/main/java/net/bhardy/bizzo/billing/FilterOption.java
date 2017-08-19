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

/**
 * When building a policy, this stage lets you add a filter to your policy.
 * <p>
 * Or you can just call build to get your policy as-is.
 */
public interface FilterOption {
    /**
     * Finish building policy without adding a filter.
     *
     * @return the resulting BillingPolicy.
     */
    BillingPolicy build();

    /**
     * This method allows you to apply a filter to your policy that it, for example,
     * only falls due on certain days.
     * <p>
     * After adding a filter you will need to select an action to be performed if that
     * filter fails.
     *
     * @see PolicyFilter
     * @param filter - the PolicyFilter to use
     * @return the builder stage for selection an action when the filter fails.
     */
    ActionChoice filter(PolicyFilter filter);
}
