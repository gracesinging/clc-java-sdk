/*
 * (c) 2015 CenturyLink. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.centurylink.cloud.sdk.policy.services.dsl.domain.autoscale.refs;

import com.centurylink.cloud.sdk.policy.services.dsl.domain.autoscale.filter.AutoscalePolicyFilter;

public class AutoscalePolicyByNameRef extends AutoscalePolicy {
    private final String name;

    AutoscalePolicyByNameRef(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public AutoscalePolicyFilter asFilter() {
        return new AutoscalePolicyFilter().names(name);
    }
}
