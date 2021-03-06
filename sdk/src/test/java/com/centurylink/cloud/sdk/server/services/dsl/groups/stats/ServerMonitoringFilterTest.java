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

package com.centurylink.cloud.sdk.server.services.dsl.groups.stats;

import com.centurylink.cloud.sdk.core.client.ClcClientException;
import com.centurylink.cloud.sdk.server.services.client.domain.group.MonitoringStatisticRequest;
import com.centurylink.cloud.sdk.server.services.dsl.domain.group.GroupConverter;
import com.centurylink.cloud.sdk.server.services.dsl.domain.group.MonitoringType;
import com.centurylink.cloud.sdk.server.services.dsl.domain.group.ServerMonitoringFilter;
import org.testng.annotations.Test;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@Test
public class ServerMonitoringFilterTest {

    GroupConverter converter = new GroupConverter();

    @Test(expectedExceptions = NullPointerException.class)
    public void defaultFilterTest() {
        converter.createMonitoringStatisticRequest(new ServerMonitoringFilter());
    }

    @Test(expectedExceptions = ClcClientException.class)
    public void incorrectStartEndTest() {
        converter.createMonitoringStatisticRequest(
            new ServerMonitoringFilter()
                .from(OffsetDateTime.now().minusDays(1))
                .to(OffsetDateTime.now().minusDays(2))
        );
    }

    @Test(expectedExceptions = ClcClientException.class)
    public void incorrectIntervalTest() {
        converter.createMonitoringStatisticRequest(
            new ServerMonitoringFilter()
                .interval(Duration.ofDays(3))
                .from(OffsetDateTime.now().minusDays(2))
                .to(OffsetDateTime.now().minusDays(1))
        );
    }

    @Test
    public void typeHourlyTest() {
        ServerMonitoringFilter config = new ServerMonitoringFilter()
            .last(Duration.ofDays(2));
        OffsetDateTime start = config.getFrom();
        MonitoringStatisticRequest req = converter.createMonitoringStatisticRequest(config);

        assertEquals(req.getType(), config.getType().name());
        assertEquals(req.getStart(), start.atZoneSameInstant(ZoneOffset.UTC).toString());
        assertEquals(req.getSampleInterval(), "00:01:00:00");
    }

    @Test(expectedExceptions = ClcClientException.class)
    public void typeHourlyMaxPeriodTest() {
        converter.createMonitoringStatisticRequest(
            new ServerMonitoringFilter()
                .from(OffsetDateTime.now().minusDays(ServerMonitoringFilter.MAX_HOURLY_PERIOD_DAYS + 1))
        );
    }

    @Test(expectedExceptions = ClcClientException.class)
    public void typeHourlyMinIntervalTest() {
        converter.createMonitoringStatisticRequest(
            new ServerMonitoringFilter()
                .from(OffsetDateTime.now().minusHours(3))
                .interval(Duration.ofMinutes(ServerMonitoringFilter.MIN_HOURLY_INTERVAL_HOURS * 60 - 1))
        );
    }

    @Test
    public void typeLatestTest() {
        ServerMonitoringFilter config = new ServerMonitoringFilter()
            .type(MonitoringType.LATEST);
        MonitoringStatisticRequest req = converter.createMonitoringStatisticRequest(config);

        assertEquals(req.getType(), config.getType().name());
        assertNull(req.getStart());
        assertNull(req.getEnd());
        assertNull(req.getSampleInterval());
    }

    @Test
    public void typeRealtimeTest() {
        ServerMonitoringFilter config = new ServerMonitoringFilter()
            .type(MonitoringType.REALTIME)
            .from(OffsetDateTime.now().minusHours(2));

        OffsetDateTime start = config.getFrom();
        MonitoringStatisticRequest req = converter.createMonitoringStatisticRequest(config);

        assertEquals(req.getType(), config.getType().name());
        assertEquals(req.getStart(), start.atZoneSameInstant(ZoneOffset.UTC).toString());
        assertEquals(req.getSampleInterval(), "00:00:05:00");
    }

    @Test(expectedExceptions = ClcClientException.class)
    public void typeRealtimeMaxPeriodTest() {
        converter.createMonitoringStatisticRequest(
            new ServerMonitoringFilter()
                .type(MonitoringType.REALTIME)
                .from(OffsetDateTime.now().minusHours(ServerMonitoringFilter.MAX_REALTIME_PERIOD_HOURS + 1))
        );
    }

    @Test(expectedExceptions = ClcClientException.class)
    public void typeRealtimeMinIntervalTest() {
        converter.createMonitoringStatisticRequest(
            new ServerMonitoringFilter()
                .type(MonitoringType.REALTIME)
                .from(OffsetDateTime.now().minusHours(3))
                .interval(Duration.ofMinutes(ServerMonitoringFilter.MIN_REALTIME_INTERVAL_MINUTES - 1))
        );
    }
}
