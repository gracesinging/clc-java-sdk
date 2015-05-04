package com.centurylink.cloud.sdk.common.management.services;

import com.centurylink.cloud.sdk.common.management.client.DataCentersClient;
import com.centurylink.cloud.sdk.common.management.client.domain.datacenters.DataCenterMetadata;
import com.centurylink.cloud.sdk.common.management.services.domain.datacenters.filters.DataCenterFilter;
import com.centurylink.cloud.sdk.common.management.services.domain.datacenters.refs.DataCenter;
import com.centurylink.cloud.sdk.core.services.QueryService;
import com.google.inject.Inject;

import java.util.List;
import java.util.stream.Stream;

/**
 * @author ilya.drabenia
 */
public class DataCenterService implements QueryService<DataCenter, DataCenterFilter, DataCenterMetadata> {
    private final DataCentersClient serverClient;

    @Inject
    public DataCenterService(DataCentersClient serverClient) {
        this.serverClient = serverClient;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Stream<DataCenterMetadata> findLazy(DataCenterFilter criteria) {
        return findAll().stream().filter(criteria.getPredicate());
    }

    public List<DataCenterMetadata> findAll() {
        return serverClient.findAllDataCenters();
    }
}