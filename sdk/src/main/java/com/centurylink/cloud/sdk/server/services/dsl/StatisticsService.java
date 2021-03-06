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

package com.centurylink.cloud.sdk.server.services.dsl;

import com.centurylink.cloud.sdk.base.services.dsl.DataCenterService;
import com.centurylink.cloud.sdk.core.auth.services.BearerAuthentication;
import com.centurylink.cloud.sdk.server.services.dsl.domain.statistics.billing.BillingStatsEngine;
import com.centurylink.cloud.sdk.server.services.dsl.domain.statistics.monitoring.MonitoringStatsEngine;

/**
 * @author Ilya Drabenia
 */
public class StatisticsService {

    private final ServerService serverService;
    private final GroupService groupService;
    private final DataCenterService dataCenterService;

    private BearerAuthentication authentication;

    public StatisticsService(
            ServerService serverService,
            GroupService groupService,
            DataCenterService dataCenterService,
            BearerAuthentication authentication
    ) {
        this.serverService = serverService;
        this.groupService = groupService;
        this.dataCenterService = dataCenterService;
        this.authentication = authentication;
    }

    public BillingStatsEngine billingStats() {
        return new BillingStatsEngine(serverService, groupService, dataCenterService);
    }

    public MonitoringStatsEngine monitoringStats() {
        return new MonitoringStatsEngine(serverService, groupService, dataCenterService, authentication.getAccountAlias());
    }


}
