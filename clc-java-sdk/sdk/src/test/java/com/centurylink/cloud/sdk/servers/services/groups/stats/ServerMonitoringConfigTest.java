package com.centurylink.cloud.sdk.servers.services.groups.stats;

import com.centurylink.cloud.sdk.core.client.ClcClientException;
import com.centurylink.cloud.sdk.servers.client.domain.group.MonitoringStatisticRequest;
import com.centurylink.cloud.sdk.servers.services.domain.group.GroupConverter;
import com.centurylink.cloud.sdk.servers.services.domain.group.MonitoringType;
import com.centurylink.cloud.sdk.servers.services.domain.group.ServerMonitoringConfig;
import org.testng.annotations.Test;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@Test
public class ServerMonitoringConfigTest {

    GroupConverter converter = new GroupConverter();

    @Test(expectedExceptions = NullPointerException.class)
    public void defaultFilterTest() {
        converter.createMonitoringStatisticRequest(new ServerMonitoringConfig());
    }

    @Test(expectedExceptions = ClcClientException.class)
    public void incorrectStartEndTest() {
        converter.createMonitoringStatisticRequest(
            new ServerMonitoringConfig()
                .from(OffsetDateTime.now().minusDays(1))
                .to(OffsetDateTime.now().minusDays(2))
        );
    }

    @Test(expectedExceptions = ClcClientException.class)
    public void incorrectIntervalTest() {
        converter.createMonitoringStatisticRequest(
            new ServerMonitoringConfig()
                .interval(Duration.ofDays(3))
                .from(OffsetDateTime.now().minusDays(2))
                .to(OffsetDateTime.now().minusDays(1))
        );
    }

    @Test
    public void typeHourlyTest() {
        ServerMonitoringConfig config = new ServerMonitoringConfig()
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
            new ServerMonitoringConfig()
                .from(OffsetDateTime.now().minusDays(ServerMonitoringConfig.MAX_HOURLY_PERIOD_DAYS + 1))
        );
    }

    @Test(expectedExceptions = ClcClientException.class)
    public void typeHourlyMinIntervalTest() {
        converter.createMonitoringStatisticRequest(
            new ServerMonitoringConfig()
                .from(OffsetDateTime.now().minusHours(3))
                .interval(Duration.ofMinutes(ServerMonitoringConfig.MIN_HOURLY_INTERVAL_HOURS * 60 - 1))
        );
    }

    @Test
    public void typeLatestTest() {
        ServerMonitoringConfig config = new ServerMonitoringConfig()
            .type(MonitoringType.LATEST);
        MonitoringStatisticRequest req = converter.createMonitoringStatisticRequest(config);

        assertEquals(req.getType(), config.getType().name());
        assertNull(req.getStart());
        assertNull(req.getEnd());
        assertNull(req.getSampleInterval());
    }

    @Test
    public void typeRealtimeTest() {
        ServerMonitoringConfig config = new ServerMonitoringConfig()
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
            new ServerMonitoringConfig()
                .type(MonitoringType.REALTIME)
                .from(OffsetDateTime.now().minusHours(ServerMonitoringConfig.MAX_REALTIME_PERIOD_HOURS + 1))
        );
    }

    @Test(expectedExceptions = ClcClientException.class)
    public void typeRealtimeMinIntervalTest() {
        converter.createMonitoringStatisticRequest(
            new ServerMonitoringConfig()
                .type(MonitoringType.REALTIME)
                .from(OffsetDateTime.now().minusHours(3))
                .interval(Duration.ofMinutes(ServerMonitoringConfig.MIN_REALTIME_INTERVAL_MINUTES - 1))
        );
    }
}