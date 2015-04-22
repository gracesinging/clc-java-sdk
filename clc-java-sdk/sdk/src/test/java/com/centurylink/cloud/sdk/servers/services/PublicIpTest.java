package com.centurylink.cloud.sdk.servers.services;

import com.centurylink.cloud.sdk.core.client.domain.Link;
import com.centurylink.cloud.sdk.servers.AbstractServersSdkTest;
import com.centurylink.cloud.sdk.servers.client.domain.server.IpAddress;
import com.centurylink.cloud.sdk.servers.client.domain.server.PublicIpAddressResponse;
import com.centurylink.cloud.sdk.servers.client.domain.server.metadata.ServerMetadata;
import com.centurylink.cloud.sdk.servers.client.domain.ip.PortConfig;
import com.centurylink.cloud.sdk.servers.client.domain.ip.PublicIpAddressRequest;
import com.centurylink.cloud.sdk.servers.services.domain.server.refs.ServerRef;
import com.centurylink.cloud.sdk.tests.fixtures.SingleServerFixture;
import com.google.inject.Inject;
import org.testng.annotations.Test;

import java.util.List;

import static com.centurylink.cloud.sdk.tests.TestGroups.INTEGRATION;
import static com.centurylink.cloud.sdk.tests.TestGroups.LONG_RUNNING;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author aliaksandr krasitski
 */
@Test(groups = {INTEGRATION, LONG_RUNNING})
public class PublicIpTest extends AbstractServersSdkTest {

    @Inject
    ServerService serverService;

    @Test
    public void testPublicIpTest() {
        ServerRef serverRef = SingleServerFixture.server();

        serverService
            .addPublicIp(serverRef,
                    new PublicIpAddressRequest()
                            .ports(
                                    PortConfig.HTTPS,
                                    PortConfig.HTTP
                            )
                            .sourceRestrictions("70.100.60.140/32")
            );

        ServerMetadata server = serverService.findByRef(serverRef);
        List<IpAddress> ipAddresses = server.getDetails().getIpAddresses();
        ipAddresses.parallelStream().forEach(address -> {
            if (address.getPublicIp() != null) {
                PublicIpAddressResponse resp = serverService.getPublicIp(serverRef, address.getPublicIp());
                serverService.removePublicIp(serverRef, address.getPublicIp());
            }
        });

        List<IpAddress> initialIpAddresses = serverService.findByRef(serverRef).getDetails().getIpAddresses();

        assertEquals(ipAddresses.stream().filter(addr -> addr.getPublicIp() != null).count(),
                initialIpAddresses.stream().filter(addr -> addr.getPublicIp() != null).count() + 1);
    }

}
