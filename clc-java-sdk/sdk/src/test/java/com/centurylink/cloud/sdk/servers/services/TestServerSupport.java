package com.centurylink.cloud.sdk.servers.services;

import com.centurylink.cloud.sdk.servers.services.domain.Machine;
import com.centurylink.cloud.sdk.servers.services.domain.datacenter.DataCenter;
import com.centurylink.cloud.sdk.servers.services.domain.group.DefaultGroups;
import com.centurylink.cloud.sdk.servers.services.domain.server.CreateServerCommand;
import com.centurylink.cloud.sdk.servers.services.domain.group.Group;
import com.centurylink.cloud.sdk.servers.services.domain.os.OperatingSystem;
import com.centurylink.cloud.sdk.servers.services.domain.template.Template;

import static com.centurylink.cloud.sdk.servers.services.domain.group.DefaultGroups.DEFAULT_GROUP;
import static com.centurylink.cloud.sdk.servers.services.domain.server.ServerType.STANDARD;
import static com.centurylink.cloud.sdk.servers.services.domain.datacenter.DataCenters.DE_FRANKFURT;
import static com.centurylink.cloud.sdk.servers.services.domain.os.CpuArchitecture.x86_64;
import static com.centurylink.cloud.sdk.servers.services.domain.os.OsType.CENTOS;

/**
 * @author ilya.drabenia
 */
public class TestServerSupport {
    public static final String PASSWORD = "1qa@WS3ed";
    private final ServerService serverService;

    public TestServerSupport(ServerService serverService) {
        this.serverService = serverService;
    }

    public CreateServerCommand createAnyServer() {
        return
            serverService.create(new CreateServerCommand()
                .name("ALTRS1")
                .type(STANDARD)
                .password(PASSWORD)

                .group(Group.refByName()
                    .dataCenter(DataCenter.refById(DE_FRANKFURT.getId()))
                    .name(DEFAULT_GROUP)
                )

                .machine(new Machine()
                    .cpuCount(1)
                    .ram(2)
                )

                .template(new Template().os(new OperatingSystem()
                    .type(CENTOS)
                    .version("6")
                    .architecture(x86_64)
                ))
            )

            .waitUntilComplete()
            .getResult();
    }

    public void deleteServer(CreateServerCommand newServer) {
        serverService
            .delete(newServer)
            .waitUntilComplete();
    }
}
