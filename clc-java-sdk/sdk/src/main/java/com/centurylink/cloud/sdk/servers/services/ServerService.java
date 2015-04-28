package com.centurylink.cloud.sdk.servers.services;

import com.centurylink.cloud.sdk.core.client.domain.Link;
import com.centurylink.cloud.sdk.core.commons.client.QueueClient;
import com.centurylink.cloud.sdk.core.commons.services.domain.queue.future.OperationFuture;
import com.centurylink.cloud.sdk.core.commons.services.domain.queue.future.job.JobFuture;
import com.centurylink.cloud.sdk.core.commons.services.domain.queue.future.job.NoWaitingJobFuture;
import com.centurylink.cloud.sdk.core.commons.services.domain.queue.future.job.ParallelJobsFuture;
import com.centurylink.cloud.sdk.core.commons.services.domain.queue.future.job.SequentialJobsFuture;
import com.centurylink.cloud.sdk.core.services.ResourceNotFoundException;
import com.centurylink.cloud.sdk.servers.client.ServerClient;
import com.centurylink.cloud.sdk.servers.client.domain.ip.PublicIpMetadata;
import com.centurylink.cloud.sdk.servers.client.domain.server.*;
import com.centurylink.cloud.sdk.servers.client.domain.server.metadata.ServerMetadata;
import com.centurylink.cloud.sdk.servers.services.domain.group.filters.GroupFilter;
import com.centurylink.cloud.sdk.servers.services.domain.group.refs.Group;
import com.centurylink.cloud.sdk.servers.services.domain.ip.CreatePublicIpConfig;
import com.centurylink.cloud.sdk.servers.services.domain.ip.ModifyPublicIpConfig;
import com.centurylink.cloud.sdk.servers.services.domain.ip.PublicIpConverter;
import com.centurylink.cloud.sdk.servers.services.domain.server.CreateServerConfig;
import com.centurylink.cloud.sdk.servers.services.domain.server.ServerConverter;
import com.centurylink.cloud.sdk.servers.services.domain.server.filters.ServerFilter;
import com.centurylink.cloud.sdk.servers.services.domain.server.future.CreateServerJobFuture;
import com.centurylink.cloud.sdk.servers.services.domain.server.refs.Server;
import com.centurylink.cloud.sdk.servers.services.domain.server.refs.ServerByIdRef;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.centurylink.cloud.sdk.core.services.filter.Filters.nullable;
import static com.centurylink.cloud.sdk.core.services.function.Predicates.isAlwaysTruePredicate;
import static com.centurylink.cloud.sdk.core.services.function.Predicates.notNull;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;

/**
 * @author ilya.drabenia
 */
public class ServerService {
    private final ServerConverter serverConverter;
    private final GroupService groupService;
    private final ServerClient client;
    private final QueueClient queueClient;
    private final PublicIpConverter publicIpConverter;

    @Inject
    public ServerService(ServerConverter serverConverter, ServerClient client, QueueClient queueClient,
                         GroupService groupService, PublicIpConverter publicIpConverter) {
        this.serverConverter = serverConverter;
        this.client = client;
        this.queueClient = queueClient;
        this.groupService = groupService;
        this.publicIpConverter = publicIpConverter;
    }

    public OperationFuture<ServerMetadata> create(CreateServerConfig command) {
        BaseServerResponse response = client
            .create(serverConverter.buildCreateServerRequest(command));

        ServerMetadata serverInfo = client
            .findServerByUuid(response.findServerUuid());

        return new OperationFuture<>(
            serverInfo,
            new SequentialJobsFuture(
                () ->
                    new CreateServerJobFuture(response.findStatusId(), serverInfo.getId(), queueClient, client),

                () ->
                    addPublicIpIfNeeded(command, serverInfo)
            )
        );
    }

    private JobFuture addPublicIpIfNeeded(CreateServerConfig command, ServerMetadata serverInfo) {
        if (command.getNetwork().getPublicIpConfig() != null) {
            return
                addPublicIp(
                    serverInfo.asRefById(),
                    command.getNetwork().getPublicIpConfig()
                )
                .jobFuture();
        } else {
            return
                new NoWaitingJobFuture();
        }
    }

    /**
     * Delete existing server
     * @param server server reference
     * @return OperationFuture wrapper for ServerRef
     */
    public OperationFuture<Server> delete(Server server) {
        BaseServerResponse response = client.delete(idByRef(server));

        return new OperationFuture<>(
            server,
            response.findStatusId(),
            queueClient
        );
    }

    /**
     * Delete existing servers
     * @param servers the array of servers to delete
     * @return OperationFuture wrapper for list of ServerRef
     */
    public OperationFuture<List<Server>> delete(Server... servers) {
        List<JobFuture> futures = Arrays.asList(servers).stream()
            .map(serverRef -> delete(serverRef).jobFuture())
            .collect(toList());

        return new OperationFuture<>(
            Arrays.asList(servers),
            new ParallelJobsFuture(futures)
        );
    }

    /**
     * Delete existing servers
     * @param filter server filter object
     * @return OperationFuture wrapper for list of ServerRef
     */
    public OperationFuture<List<Server>> delete(ServerFilter filter) {
            List<Server> serverRefs = find(filter).stream()
                .map(metadata -> metadata.asRefById())
                .collect(toList());
            return delete(serverRefs.toArray(new Server[serverRefs.size()]));
    }

    public ServerMetadata findByRef(Server serverRef) {
        return
            findLazy(
                serverRef.asFilter()
            )
            .findFirst().orElseThrow(() ->
                    new ResourceNotFoundException("Server by reference %s not found", serverRef.toString())
            );
    }

    String idByRef(Server ref) {
        if (ref.is(ServerByIdRef.class)) {
            return ref.as(ServerByIdRef.class).getId();
        } else {
            return findByRef(ref).getId();
        }
    }

    public Stream<ServerMetadata> findLazy(ServerFilter serverFilter) {
        if (isAlwaysTruePredicate(serverFilter.getPredicate())
            && isAlwaysTruePredicate(serverFilter.getGroupFilter().getPredicate())
            && isAlwaysTruePredicate(serverFilter.getGroupFilter().getDataCenterFilter().getPredicate())
            && serverFilter.getServerIds().size() > 0) {
            return
                serverFilter
                    .getServerIds()
                    .stream()
                    .map(nullable(client::findServerById))
                    .filter(notNull());
        } else {
            return
                groupService
                    .findLazy(serverFilter.getGroupFilter())
                    .map(group -> client.getGroup(group.getId(), true))
                    .flatMap(group -> group.getServers().stream())
                    .filter(serverFilter.getPredicate());
        }
    }

    public List<ServerMetadata> find(ServerFilter serverFilter) {
        return findLazy(serverFilter).collect(toList());
    }

    /**
     * Power on a single server or group of servers
     *
     * @param serverRefs server references list
     * @return OperationFuture wrapper for BaseServerResponse list
     */
    public OperationFuture<List<BaseServerResponse>> powerOn(Server... serverRefs) {
        return powerOperationResponse(
            client.powerOn(ids(serverRefs))
        );
    }

    /**
     * Power on a single server or group of servers
     *
     * @param serverFilter search servers criteria
     * @return OperationFuture wrapper for BaseServerResponse list
     */
    public OperationFuture<List<BaseServerResponse>> powerOn(ServerFilter serverFilter) {
        return powerOperationResponse(
            client.powerOn(ids(serverFilter))
        );
    }

    /**
     * Power off a single server or group of servers
     *
     * @param serverRefs server references list
     * @return OperationFuture wrapper for BaseServerResponse list
     */
    public OperationFuture<List<BaseServerResponse>> powerOff(Server... serverRefs) {
        return powerOperationResponse(
            client.powerOff(ids(serverRefs))
        );
    }

    /**
     * Power off a single server or group of servers
     *
     * @param serverFilter search servers criteria
     * @return OperationFuture wrapper for BaseServerResponse list
     */
    public OperationFuture<List<BaseServerResponse>> powerOff(ServerFilter serverFilter) {
        return powerOperationResponse(
            client.powerOff(ids(serverFilter))
        );
    }

    /**
     * Start maintenance mode on a single server or group of servers
     *
     * @param serverRefs server references list
     * @return OperationFuture wrapper for BaseServerResponse list
     */
    public OperationFuture<List<BaseServerResponse>> startMaintenance(Server... serverRefs) {
        return powerOperationResponse(
            client.startMaintenance(ids(serverRefs))
        );
    }

    /**
     * Start maintenance mode on a single server or group of servers
     *
     * @param serverFilter search servers criteria
     * @return OperationFuture wrapper for BaseServerResponse list
     */
    public OperationFuture<List<BaseServerResponse>> startMaintenance(ServerFilter serverFilter) {
        return powerOperationResponse(
            client.startMaintenance(ids(serverFilter))
        );
    }

    /**
     * Stop maintenance mode on a single server or group of servers
     *
     * @param serverRefs server references list
     * @return OperationFuture wrapper for BaseServerResponse list
     */
    public OperationFuture<List<BaseServerResponse>> stopMaintenance(Server... serverRefs) {
        return powerOperationResponse(
            client.stopMaintenance(ids(serverRefs))
        );
    }

    /**
     * Stop maintenance mode on a single server or group of servers
     *
     * @param serverFilter search servers criteria
     * @return OperationFuture wrapper for BaseServerResponse list
     */
    public OperationFuture<List<BaseServerResponse>> stopMaintenance(ServerFilter serverFilter) {
        return powerOperationResponse(
            client.stopMaintenance(ids(serverFilter))
        );
    }

    /**
     * Pause a single server or group of servers
     *
     * @param serverRefs server references list
     * @return OperationFuture wrapper for BaseServerResponse list
     */
    public OperationFuture<List<BaseServerResponse>> pause(Server... serverRefs) {
        return
            powerOperationResponse(
                client.pause(ids(serverRefs))
            );
    }

    /**
     * Pause a single server or group of servers
     *
     * @param serverFilter search servers criteria
     * @return OperationFuture wrapper for BaseServerResponse list
     */
    public OperationFuture<List<BaseServerResponse>> pause(ServerFilter serverFilter) {
        return powerOperationResponse(
            client.pause(ids(serverFilter))
        );
    }

    /**
     * Reboot a single server or group of servers
     *
     * @param serverRefs server references list
     * @return OperationFuture wrapper for BaseServerResponse list
     */
    public OperationFuture<List<BaseServerResponse>> reboot(Server... serverRefs) {
        return powerOperationResponse(
            client.reboot(ids(serverRefs))
        );
    }

    /**
     * Reboot a single server or group of servers
     *
     * @param serverFilter search servers criteria
     * @return OperationFuture wrapper for BaseServerResponse list
     */
    public OperationFuture<List<BaseServerResponse>> reboot(ServerFilter serverFilter) {
        return powerOperationResponse(
            client.reboot(ids(serverFilter))
        );
    }

    /**
     * Reset a single server or group of servers
     *
     * @param serverRefs server references list
     * @return OperationFuture wrapper for BaseServerResponse list
     */
    public OperationFuture<List<BaseServerResponse>> reset(Server... serverRefs) {
        return powerOperationResponse(
            client.reset(ids(serverRefs))
        );
    }

    /**
     * Reset a single server or group of servers
     *
     * @param serverFilter search servers criteria
     * @return OperationFuture wrapper for BaseServerResponse list
     */
    public OperationFuture<List<BaseServerResponse>> reset(ServerFilter serverFilter) {
        return powerOperationResponse(
            client.reset(ids(serverFilter))
        );
    }

    /**
     * Shut down a single server or group of servers
     *
     * @param serverRefs server references list
     * @return OperationFuture wrapper for BaseServerResponse list
     */
    public OperationFuture<List<BaseServerResponse>> shutDown(Server... serverRefs) {
        return powerOperationResponse(
            client.shutDown(ids(serverRefs))
        );
    }

    /**
     * Shut down a single server or group of servers
     *
     * @param serverFilter search servers criteria
     * @return OperationFuture wrapper for BaseServerResponse list
     */
    public OperationFuture<List<BaseServerResponse>> shutDown(ServerFilter serverFilter) {
        return powerOperationResponse(
            client.shutDown(ids(serverFilter))
        );
    }

    /**
     * Archive a single server or group of servers
     *
     * @param serverRefs server references list
     * @return OperationFuture wrapper for BaseServerResponse list
     */
    public OperationFuture<List<BaseServerResponse>> archive(Server... serverRefs) {
        return powerOperationResponse(
            client.archive(ids(serverRefs))
        );
    }

    /**
     * Archive a single server or group of servers
     *
     * @param serverFilter search servers criteria
     * @return OperationFuture wrapper for BaseServerResponse list
     */
    public OperationFuture<List<BaseServerResponse>> archive(ServerFilter serverFilter) {
        return powerOperationResponse(
            client.archive(ids(serverFilter))
        );
    }

    /**
     * Create snapshot of a single server or group of servers
     *
     * @param expirationDays expiration days (must be between 1 and 10)
     * @param serverRefs     server references list
     * @return OperationFuture wrapper for BaseServerResponse list
     */
    public OperationFuture<List<BaseServerResponse>> createSnapshot(Integer expirationDays, Server... serverRefs) {
        return
            powerOperationResponse(
                client.createSnapshot(
                    new CreateSnapshotRequest()
                        .snapshotExpirationDays(expirationDays)
                        .serverIds(ids(serverRefs))
                )
            );
    }

    /**
     * Create snapshot of a single server or group of servers
     *
     * @param expirationDays expiration days (must be between 1 and 10)
     * @param serverFilter search servers criteria
     * @return OperationFuture wrapper for BaseServerResponse list
     */
    public OperationFuture<List<BaseServerResponse>> createSnapshot(Integer expirationDays, ServerFilter serverFilter) {
        return
            powerOperationResponse(
                client.createSnapshot(
                    new CreateSnapshotRequest()
                        .snapshotExpirationDays(expirationDays)
                        .serverIds(ids(serverFilter))
                )
            );
    }

    /**
     * Delete all snapshots for provided servers
     * @param servers server references
     * @return OperationFuture wrapper for list of ServerRef
     */
    public OperationFuture<List<Server>> deleteSnapshot(Server... servers) {
        List<String> responseIds = new ArrayList<>();
        List<Server> serverList = Arrays.asList(servers);

        Arrays.asList(servers).stream()
            .map(serverRef -> findByRef(serverRef))
            .forEach(metadata -> {
                List<Snapshot> snapshotsList = metadata.getDetails().getSnapshots();
                String serverId = metadata.getId();

                responseIds.addAll(snapshotsList
                        .stream()
                        .map(snapshot ->
                            client.deleteSnapshot(serverId, getSnapshotId(snapshot)).getId())
                        .collect(toList())
                );
            });

        return new OperationFuture<>(
            serverList,
            responseIds,
            queueClient
        );
    }

    private String getSnapshotId(Snapshot snapshot) {
        String rel = "self";
        String slash = "/";

        String snapshotUrl = snapshot.getLinks().stream()
            .filter(link -> link.getRel().equals(rel))
            .map(Link::getHref)
            .findFirst()
            .get();

        return snapshotUrl.substring(snapshotUrl.lastIndexOf(slash) + 1);
    }

    /**
     * Delete all snapshots for server criteria
     * @param serverFilter search servers criteria
     * @return OperationFuture wrapper for list of ServerRef
     */
    public OperationFuture<List<Server>> deleteSnapshot(ServerFilter serverFilter) {
        return deleteSnapshot(getRefsFromFilter(serverFilter));
    }

    /**
     * Restore a given archived server to a specified group
     *
     * @param server server reference
     * @param group  group reference
     * @return OperationFuture wrapper for BaseServerResponse
     */
    public OperationFuture<Link> restore(Server server, Group group) {
        return baseServerResponse(
            restore(server, groupService.findByRef(group).getId())
        );
    }

    private Link restore(Server server, String groupId) {
        return client.restore(
            idByRef(server),
            new RestoreServerRequest()
                .targetGroupId(groupId)
        );
    }

    /**
     * Revert a set of servers to snapshot
     * @param servers server references
     * @return OperationFuture wrapper for list of ServerRef
     */
    OperationFuture<List<Server>> revertToSnapshot(Server... servers) {
        List<Server> serverList = Arrays.asList(servers);

        List<JobFuture> futures = new ArrayList<>();

        Arrays.asList(servers).stream()
            .map(serverRef -> findByRef(serverRef))
            .forEach(metadata -> {
                List<Snapshot> snapshotsList = metadata.getDetails().getSnapshots();
                String serverId = metadata.getId();

                futures.addAll(snapshotsList
                    .stream()
                    .map(snapshot ->
                        baseServerResponse(client.revertToSnapshot(serverId, getSnapshotId(snapshot))).jobFuture())
                    .collect(toList()));
            });

        return new OperationFuture<>(
                serverList,
                new ParallelJobsFuture(futures)
            );
    }

    /**
     * Revert a set of servers to snapshot
     * @param filter search servers criteria
     * @return OperationFuture wrapper for list of ServerRef
     */
    OperationFuture<List<Server>> revertToSnapshot(ServerFilter filter) {
        return revertToSnapshot(getRefsFromFilter(filter));
    }

    private List<String> ids(Server... serverRefs) {
        return
            Stream
                .of(serverRefs)
                .filter(notNull())
                .map(this::idByRef)
                .map(String::toUpperCase)
                .collect(toList());
    }

    public List<String> ids(ServerFilter serverFilter) {
        List<ServerMetadata> serverMetadataList = find(serverFilter);

        return
            serverMetadataList
                .stream()
                .filter(notNull())
                .map(ServerMetadata::getId)
                .map(String::toUpperCase)
                .collect(toList());
    }

    public List<String> ids(GroupFilter groupFilter) {
        return ids(
            new ServerFilter()
                .groupsWhere(groupFilter)
        );
    }

    public List<String> ids(Group... groups) {
        return ids(
            new ServerFilter()
                .groups(groups)
        );
    }

    /**
     * Add public IP to server
     *
     * @param serverRef        server reference
     * @param publicIpConfig publicIp config
     * @return OperationFuture wrapper for ServerRef
     */
    public OperationFuture<Server> addPublicIp(Server serverRef, CreatePublicIpConfig publicIpConfig) {
        Link response = client.addPublicIp(idByRef(serverRef), publicIpConverter.createPublicIpRequest(publicIpConfig));

        return new OperationFuture<>(
            serverRef,
            response.getId(),
            queueClient
        );
    }

    /**
     * Modify ALL existing public IPs on server
     * @param server server reference
     * @param config publicIp config
     * @return OperationFuture wrapper for ServerRef
     */
    public OperationFuture<Server> modifyPublicIp(Server server, ModifyPublicIpConfig config) {
        checkNotNull(config, "PublicIpConfig must be not null");
        List<IpAddress> ipAddresses = findByRef(server).getDetails().getIpAddresses();
        List<String> responseIds = ipAddresses.stream()
            .map(address -> address.getPublicIp())
            .filter(notNull())
            .map(ipAddress ->
                client.modifyPublicIp(idByRef(server),
                    ipAddress,
                    publicIpConverter.createPublicIpRequest(config)))
            .map(Link::getId)
            .collect(toList());

        return new OperationFuture<>(
            server,
            responseIds,
            queueClient
        );
    }

    /**
     * Modify provided public IP on server
     * @param server server reference
     * @param publicIp public ip
     * @param config publicIp config
     * @return OperationFuture wrapper for ServerRef
     */
    public OperationFuture<Server> modifyPublicIp(Server server, String publicIp, ModifyPublicIpConfig config) {
        checkNotNull(config, "PublicIpConfig must be not null");
        checkNotNull(publicIp, "public ip must not be null");

        Link response = client.modifyPublicIp(idByRef(server),
            publicIp,
            publicIpConverter.createPublicIpRequest(config)
        );

        return new OperationFuture<>(
            server,
            response.getId(),
            queueClient
        );
    }

    /**
     * Modify ALL existing public IPs on servers
     * @param servers The list of server references
     * @param config  publicIp config
     * @return OperationFuture wrapper for list of ServerRef
     */
    public OperationFuture<List<Server>> modifyPublicIp(List<Server> servers, ModifyPublicIpConfig config) {
        List<JobFuture> futures = servers.stream()
            .map(serverRef -> modifyPublicIp(serverRef, config).jobFuture())
            .collect(toList());

        return new OperationFuture<>(
            servers,
            new ParallelJobsFuture(futures));
    }

    /**
     * Modify existing public IP on servers
     * @param filter The server filter object
     * @param config  publicIp config
     * @return OperationFuture wrapper for list of ServerRef
     */
    public OperationFuture<List<Server>> modifyPublicIp(ServerFilter filter, ModifyPublicIpConfig config) {
        return modifyPublicIp(Arrays.asList(getRefsFromFilter(filter)), config);
    }

    Server[] getRefsFromFilter(ServerFilter filter) {
        List<Server> serverRefs = find(filter).stream()
            .map(metadata -> metadata.asRefById())
            .collect(toList());

        return serverRefs.toArray(new Server[serverRefs.size()]);
    }

    /**
     * Get public IP object
     *
     * @param serverRef server reference
     * @param publicIp  existing public IP address
     * @return public IP response object
     */
    public PublicIpMetadata getPublicIp(Server serverRef, String publicIp) {
        return client.getPublicIp(idByRef(serverRef), publicIp);
    }

    /**
     * Get list public IPs for provided server reference {@code server}
     *
     * @param server server reference
     * @return list public IPs
     */
    public List<PublicIpMetadata> findPublicIp(Server server) {
        List<IpAddress> ipAddresses = findByRef(server).getDetails().getIpAddresses();

        return ipAddresses.stream()
            .map(IpAddress::getPublicIp)
            .filter(notNull())
            .map(address -> getPublicIp(server, address))
            .collect(toList());
    }

    /**
     * Remove public IP from server
     *
     * @param serverRef server reference
     * @param ipAddress  existing public IP address
     * @return OperationFuture wrapper for ServerRef
     */
    public OperationFuture<Server> removePublicIp(Server serverRef, String ipAddress) {
        checkNotNull(ipAddress, "ipAddress must be not null");

        Link response = client.removePublicIp(idByRef(serverRef), ipAddress);

        return new OperationFuture<>(
            serverRef,
            response.getId(),
            queueClient
        );
    }

    /**
     * Remove all public IPs from server
     *
     * @param serverRef server reference
     * @return server reference
     */
    public OperationFuture<Server> removePublicIp(Server serverRef) {
        ServerMetadata serverMetadata = findByRef(serverRef);
        List<JobFuture> jobFutures = serverMetadata.getDetails().getIpAddresses()
            .stream()
            .map(IpAddress::getPublicIp)
            .filter(notNull())
            .map(address -> removePublicIp(serverRef, address).jobFuture())
            .collect(toList());

        return new OperationFuture<>(
            serverRef,
            new ParallelJobsFuture(jobFutures)
        );
    }

    public OperationFuture<List<BaseServerResponse>> powerOperationResponse(List<BaseServerResponse> apiResponse) {
        return
            new OperationFuture<>(
                apiResponse,
                apiResponse
                    .stream()
                    .filter(notNull())
                    .map(BaseServerResponse::findStatusId)
                    .collect(toList()),
                queueClient
            );
    }

    private OperationFuture<Link> baseServerResponse(Link response) {
        return new OperationFuture<>(
            response,
            response.getId(),
            queueClient
        );
    }
}