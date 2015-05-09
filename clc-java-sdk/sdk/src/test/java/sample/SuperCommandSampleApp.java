package sample;

import com.centurylink.cloud.sdk.ClcSdk;
import com.centurylink.cloud.sdk.core.auth.services.domain.credentials.PropertiesFileCredentialsProvider;
import com.centurylink.cloud.sdk.core.services.refs.ReferenceNotResolvedException;
import com.centurylink.cloud.sdk.servers.client.domain.server.metadata.ServerMetadata;
import com.centurylink.cloud.sdk.servers.services.GroupService;
import com.centurylink.cloud.sdk.servers.services.ServerService;
import com.centurylink.cloud.sdk.servers.services.domain.group.refs.Group;
import com.centurylink.cloud.sdk.servers.services.domain.ip.CreatePublicIpConfig;
import com.centurylink.cloud.sdk.servers.services.domain.ip.port.PortConfig;
import com.centurylink.cloud.sdk.servers.services.domain.server.*;
import com.centurylink.cloud.sdk.servers.services.domain.server.filters.ServerFilter;
import com.centurylink.cloud.sdk.servers.services.domain.server.refs.Server;
import com.centurylink.cloud.sdk.servers.services.domain.template.refs.Template;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.ZonedDateTime;

import static com.centurylink.cloud.sdk.common.management.services.domain.datacenters.refs.DataCenter.DE_FRANKFURT;
import static com.centurylink.cloud.sdk.common.management.services.domain.datacenters.refs.DataCenter.US_CENTRAL_SALT_LAKE_CITY;
import static com.centurylink.cloud.sdk.servers.services.domain.InfrastructureConfig.dataCenter;
import static com.centurylink.cloud.sdk.servers.services.domain.group.GroupHierarchyConfig.group;
import static com.centurylink.cloud.sdk.servers.services.domain.server.ServerType.STANDARD;
import static com.centurylink.cloud.sdk.servers.services.domain.template.filters.os.CpuArchitecture.x86_64;
import static com.centurylink.cloud.sdk.servers.services.domain.template.filters.os.OsType.CENTOS;
import static com.centurylink.cloud.sdk.tests.TestGroups.SAMPLES;
import static java.util.stream.Collectors.toList;

@Test(groups = SAMPLES)
public class SuperCommandSampleApp extends Assert {

    private ServerService serverService;
    private GroupService groupService;


    public SuperCommandSampleApp() {
        ClcSdk sdk = new ClcSdk(
            new PropertiesFileCredentialsProvider("centurylink-clc-sdk-uat.properties")
        );

        serverService = sdk.serverService();
        groupService = sdk.groupService();
    }

    @BeforeClass
    public void init() {
        clearAll();

        groupService
            .defineInfrastructure(dataCenter(DE_FRANKFURT).subitems(
                group("Sample application").subitems(
                    nginxServer(),

                    group("Business").subitems(
                        apacheHttpServer(),
                        mysqlServer()
                    )
                )
            ))

            .waitUntilComplete();
    }

    @AfterClass
    public void deleteServers() {
        clearAll();
    }

    public static CreateServerConfig centOsServer(String name) {
        return new CreateServerConfig()
            .name(name)
            .description(name)
            .type(STANDARD)
            .machine(new Machine()
                .cpuCount(1)
                .ram(2)
            )
            .template(Template.refByOs()
                .dataCenter(US_CENTRAL_SALT_LAKE_CITY)
                .type(CENTOS)
                .version("6")
                .architecture(x86_64)
            )
            .timeToLive(
                ZonedDateTime.now().plusHours(2)
            );
    }

    public static CreateServerConfig mysqlServer() {
        CreateServerConfig mySqlSrv = centOsServer("MySQL");

        mySqlSrv.getMachine()
            .disk(new DiskConfig()
                .type(DiskType.RAW)
                .size(10)
            );

        return mySqlSrv;
    }

    public static CreateServerConfig nginxServer() {
        return centOsServer("Nginx")
            .network(new NetworkConfig()
                .publicIpConfig(new CreatePublicIpConfig()
                    .openPorts(PortConfig.HTTP)));
    }

    public static CreateServerConfig apacheHttpServer() {
        return centOsServer("Apache");
    }

    private void clearAll() {
        Group ref = Group.refByName(DE_FRANKFURT, "Sample application");

        try {
            groupService.delete(ref);
        } catch (ReferenceNotResolvedException ex) {
            // noop
        }
    }

    private ServerMetadata loadServerMetadata(Server server) {
        ServerMetadata metadata = serverService.findByRef(server);
        assertNotNull(metadata);

        return metadata;
    }

    private void checkServerIsStarted(String name) {
        assert
            serverService
                .findLazy(new ServerFilter().nameContains(name))
                .findFirst().get()
                .getDetails()
                .getPowerState()
                .equals("started");
    }

    @Test
    public void checkServersIsActiveTest() {
        checkServerIsStarted("nginx");
        checkServerIsStarted("mysql");
        checkServerIsStarted("apache");
    }

    @Test
    public void nginxTest() {
        ServerMetadata nginxMetadata = loadServerMetadata(
            Server.refByDescription(DE_FRANKFURT, "nginx")
        );

        assert
            nginxMetadata.getDetails().getIpAddresses().stream()
            .filter(address -> address.getPublicIp() != null)
            .collect(toList())
            .size() == 1;
    }
}