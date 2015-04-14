package com.centurylink.cloud.sdk.servers.services;

import com.centurylink.cloud.sdk.servers.services.domain.server.TimeToLive;
import com.centurylink.cloud.sdk.servers.services.domain.server.TimeToLiveParseException;
import org.testng.annotations.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Calendar;

/**
 * @author Aliaksandr Krasitski
 */
public class TimeToLiveTest {

    @Test
    public void testConstructorDate() {
        new TimeToLive(Calendar.getInstance().getTime());
    }

    @Test
    public void testConstructorCalendar() {
        new TimeToLive(Calendar.getInstance());
    }

    @Test
    public void testConstructorZonedDateTime() {
        new TimeToLive(ZonedDateTime.now());
    }

    @Test(expectedExceptions = TimeToLiveParseException.class)
    public void testConstructorStringFail1() {
        new TimeToLive("2015-12-31");
        new TimeToLive("2015-12-31T23:00");
    }

    @Test(expectedExceptions = TimeToLiveParseException.class)
    public void testConstructorStringFail2() {
        new TimeToLive("2015-12-31T23:00");
    }

    @Test
    public void testConstructorString() {
        new TimeToLive("2015-12-31T23:00+03:00");
    }

    @Test
    public void testConstructorStringPattern() {
        new TimeToLive("2015-12-31T23:00+03:00", "yyyy-MM-dd'T'HH:mmXXX");
    }
}
