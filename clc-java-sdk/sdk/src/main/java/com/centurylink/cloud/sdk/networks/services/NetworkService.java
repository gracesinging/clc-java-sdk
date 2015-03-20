package com.centurylink.cloud.sdk.networks.services;

import com.centurylink.cloud.sdk.core.datacenters.client.DataCentersClient;
import com.centurylink.cloud.sdk.core.datacenters.client.domain.deployment.capacilities.NetworkMetadata;
import com.centurylink.cloud.sdk.core.datacenters.services.DataCenterService;
import com.centurylink.cloud.sdk.core.datacenters.services.domain.refs.DataCenterRef;
import com.centurylink.cloud.sdk.core.exceptions.ReferenceNotSupportedException;
import com.centurylink.cloud.sdk.networks.services.domain.refs.IdNetworkRef;
import com.centurylink.cloud.sdk.networks.services.domain.refs.NetworkRef;
import com.google.inject.Inject;

import java.util.List;

/**
 * @author ilya.drabenia
 */
public class NetworkService {
    private final DataCentersClient dataCentersClient;
    private final DataCenterService dataCentersService;

    @Inject
    public NetworkService(DataCentersClient dataCentersClient, DataCenterService dataCentersService) {
        this.dataCentersClient = dataCentersClient;
        this.dataCentersService = dataCentersService;
    }

    public NetworkMetadata findByRef(NetworkRef networkRef) {
        if (networkRef.is(IdNetworkRef.class)) {
            return
                dataCentersClient
                    .getDataCenterDeploymentCapabilities(dataCenterId(networkRef.getDataCenter()))
                    .findNetworkById(networkRef.as(IdNetworkRef.class).getId());
        } else {
            throw new ReferenceNotSupportedException(networkRef.getClass());
        }
    }

    public List<NetworkMetadata> findByDataCenter(DataCenterRef dataCenter) {
        return
            dataCentersClient
                .getDataCenterDeploymentCapabilities(dataCenterId(dataCenter))
                .getDeployableNetworks();
    }

    private String dataCenterId(DataCenterRef dataCenter) {
        return dataCentersService
            .findByRef(dataCenter)
            .getId();
    }
}
