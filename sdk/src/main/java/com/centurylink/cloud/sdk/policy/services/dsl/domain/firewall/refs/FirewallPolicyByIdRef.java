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

package com.centurylink.cloud.sdk.policy.services.dsl.domain.firewall.refs;

import com.centurylink.cloud.sdk.base.services.dsl.domain.datacenters.refs.DataCenter;
import com.centurylink.cloud.sdk.policy.services.dsl.domain.firewall.filter.FirewallPolicyFilter;

public class FirewallPolicyByIdRef extends FirewallPolicy {
    private final String id;

    FirewallPolicyByIdRef(String id, DataCenter dataCenter) {
        super(dataCenter);
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public FirewallPolicyFilter asFilter() {
        return
            new FirewallPolicyFilter()
                .dataCenters(getDataCenter())
                .id(id);
    }
}
