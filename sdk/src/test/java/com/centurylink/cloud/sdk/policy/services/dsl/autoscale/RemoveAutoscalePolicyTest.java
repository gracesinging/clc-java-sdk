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

package com.centurylink.cloud.sdk.policy.services.dsl.autoscale;

import com.centurylink.cloud.sdk.policy.services.AbstractAutoscalePolicySdkTest;
import com.centurylink.cloud.sdk.policy.services.client.domain.autoscale.AutoscalePolicyMetadata;
import com.centurylink.cloud.sdk.policy.services.dsl.domain.autoscale.refs.AutoscalePolicy;
import com.centurylink.cloud.sdk.server.services.dsl.domain.server.refs.Server;
import com.centurylink.cloud.sdk.tests.recorded.WireMockFileSource;
import com.centurylink.cloud.sdk.tests.recorded.WireMockMixin;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.centurylink.cloud.sdk.tests.TestGroups.RECORDED;

@Test(groups = {RECORDED})
@WireMockFileSource("remove")
public class RemoveAutoscalePolicyTest extends AbstractAutoscalePolicySdkTest implements WireMockMixin {

    private Server server1;
    private Server server2;

    @BeforeMethod
    public void init() {
        AutoscalePolicy autoscalePolicy = AutoscalePolicy.refById("02c9a0551e494c0fa6ede693268c0216");
        server1 = Server.refById("gb1altdsrv101");
        server2 = Server.refById("gb1altdsrv201");

        autoscalePolicyService.setAutoscalePolicyOnServer(autoscalePolicy, server1, server2);
    }

    @Test
    public void testRemovePolicy() {
        autoscalePolicyService.removeAutoscalePolicyOnServer(server1, server2);

        AutoscalePolicyMetadata metadata1 = autoscalePolicyService.getAutoscalePolicyOnServer(server1);
        AutoscalePolicyMetadata metadata2 = autoscalePolicyService.getAutoscalePolicyOnServer(server2);

        assertNull(metadata1);
        assertNull(metadata2);
    }
}
