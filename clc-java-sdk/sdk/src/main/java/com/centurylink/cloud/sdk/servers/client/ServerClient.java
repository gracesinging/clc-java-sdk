package com.centurylink.cloud.sdk.servers.client;

import com.centurylink.cloud.sdk.core.auth.services.BearerAuthentication;
import com.centurylink.cloud.sdk.core.client.ClcApiConstants;
import com.centurylink.cloud.sdk.core.client.InvocationFuture;
import com.centurylink.cloud.sdk.servers.client.domain.GetStatusResponse;
import com.centurylink.cloud.sdk.core.datacenters.client.domain.GetDataCenterListResponse;
import com.centurylink.cloud.sdk.core.datacenters.client.domain.GetDataCenterResponse;
import com.centurylink.cloud.sdk.core.datacenters.client.domain.deployment.capacilities.GetDeploymentCapabilitiesResponse;
import com.centurylink.cloud.sdk.servers.client.domain.group.GetGroupResponse;
import com.centurylink.cloud.sdk.servers.client.domain.server.CreateServerRequest;
import com.centurylink.cloud.sdk.servers.client.domain.server.CreateServerResponse;
import com.centurylink.cloud.sdk.servers.client.domain.server.metadata.ServerMetadata;
import com.centurylink.cloud.sdk.servers.client.domain.server.template.CreateTemplateRequest;
import com.google.common.util.concurrent.SettableFuture;
import com.google.inject.Inject;

import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.WebTarget;

import static com.centurylink.cloud.sdk.core.client.ClcApiConstants.CLC_API_URL;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

/**
 * @author ilya.drabenia
 */
public class ServerClient {
    private final BearerAuthentication authentication;

    @Inject
    public ServerClient(BearerAuthentication authFilter) {
        this.authentication = authFilter;
    }

    /**
     * Creates a new server. Calls to this operation must include a token acquired
     * from the authentication endpoint. See the Login API for information on acquiring
     * this token.
     */
    public CreateServerResponse create(CreateServerRequest request) {
        return
            client("/servers/{accountAlias}")
                .request().post(
                    entity(request, APPLICATION_JSON_TYPE)
                )
                .readEntity(CreateServerResponse.class);
    }

    public SettableFuture<CreateServerResponse> createAsync(CreateServerRequest request) {
        final InvocationFuture<CreateServerResponse> future = new InvocationFuture<CreateServerResponse>();

        client("/servers/{accountAlias}")
            .request()
            .async()
            .post(entity(request, APPLICATION_JSON_TYPE), new InvocationCallback<CreateServerResponse>() {
                @Override
                public void completed(CreateServerResponse createServerResponse) {
                    future.toListenableFuture().set(createServerResponse);
                }

                @Override
                public void failed(Throwable throwable) {
                    future.toListenableFuture().setException(throwable);
                }
            });

        return future.toListenableFuture();
    }

    public CreateServerResponse delete(String serverId) {
        return
            client("/servers/{accountAlias}/{serverId}")
                .resolveTemplate("serverId", serverId)
                .request()
                .delete(CreateServerResponse.class);
    }

    public ServerMetadata findServerByUuid(String uuid) {
        return
            client("/servers/{accountAlias}/{serverId}?uuid=true")
                .resolveTemplate("serverId", uuid)
                .request()
                .get(ServerMetadata.class);
    }

    public SettableFuture<ServerMetadata> findServerByUuidAsync(String uuid) {
        final InvocationFuture<ServerMetadata> future = new InvocationFuture<>();

        client("/servers/{accountAlias}/{serverId}?uuid=true")
            .resolveTemplate("serverId", uuid)
            .request()
            .buildGet()
            .submit(new InvocationCallback<ServerMetadata>() {
                @Override
                public void completed(ServerMetadata serverMetadata) {
                    future.toListenableFuture().set(serverMetadata);
                }

                @Override
                public void failed(Throwable throwable) {
                    future.toListenableFuture().setException(throwable);
                }
            });

        return future.toListenableFuture();
    }

    public ServerMetadata findServerById(String id) {
        return
            client("/servers/{accountAlias}/{serverId}")
                .resolveTemplate("serverId", id)
                .request()
                .get(ServerMetadata.class);
    }

    public GetDataCenterResponse getDataCenter(String dataCenterId) {
        return
            client("/datacenters/{accountAlias}/{dataCenterId}")
                .queryParam("groupLinks", true)
                .resolveTemplate("dataCenterId", dataCenterId)
                .request().get(GetDataCenterResponse.class);
    }

    public GetGroupResponse getGroup(String rootGroupId) {
        return
            client("/groups/{accountAlias}/{rootGroupId}")
                .resolveTemplate("rootGroupId", rootGroupId)
                .request().get(GetGroupResponse.class);
    }

    public GetDeploymentCapabilitiesResponse getDataCenterDeploymentCapabilities(String dataCenterId) {
        return
            client("/datacenters/{accountAlias}/{dataCenterId}/deploymentCapabilities")
                .resolveTemplate("dataCenterId", dataCenterId)
                .request().get(GetDeploymentCapabilitiesResponse.class);
    }

    public GetStatusResponse getJobStatus(String jobId) {
        return
            client("/operations/{accountAlias}/status/{statusId}")
                .resolveTemplate("statusId", jobId)
                .request()
                .get(GetStatusResponse.class);
    }

    public GetDataCenterListResponse findAllDataCenters() {
        return
            client("/datacenters/{accountAlias}?groupLinks=true")
                .request()
                .get(GetDataCenterListResponse.class);
    }

    public CreateServerResponse convertToTemplate(CreateTemplateRequest request) {
        return
            client("/servers/{accountAlias}/{serverId}/convertToTemplate")
                .resolveTemplate("serverId", request.getServerId())
                .request()
                .post(entity(request, APPLICATION_JSON))
                .readEntity(CreateServerResponse.class);
    }

    private WebTarget client(String target) {
        return
            ClcApiConstants
                .CLIENT_BUILDER
                    .build()
                    .register(authentication)
                    .target(CLC_API_URL + target)
                    .resolveTemplate("accountAlias", authentication.getAccountAlias());
    }
}
