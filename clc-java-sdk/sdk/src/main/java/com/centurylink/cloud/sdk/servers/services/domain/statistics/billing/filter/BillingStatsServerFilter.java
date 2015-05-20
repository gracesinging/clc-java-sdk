package com.centurylink.cloud.sdk.servers.services.domain.statistics.billing.filter;

import com.centurylink.cloud.sdk.servers.client.domain.server.metadata.ServerMetadata;
import com.centurylink.cloud.sdk.servers.services.ServerService;
import com.centurylink.cloud.sdk.servers.services.domain.group.filters.GroupFilter;
import com.centurylink.cloud.sdk.servers.services.domain.server.filters.ServerFilter;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class BillingStatsServerFilter implements BillingStatsFilter {

    private List<ServerMetadata> serverMetadataList;
    private List<String> serverIdRestrictionsList;

    public BillingStatsServerFilter(ServerFilter serverFilter, ServerService serverService) {
        serverMetadataList = serverService.find(serverFilter);

        serverIdRestrictionsList = serverMetadataList
            .stream()
            .map(ServerMetadata::getId)
            .collect(toList());
    }

    @Override
    public GroupFilter getFilter() {
        return
            new GroupFilter().id(
                serverMetadataList
                    .stream()
                    .map(ServerMetadata::getGroupId)
                    .distinct()
                    .collect(toList())
            );
    }

    public List<String> getServerIdRestrictionsList() {
        return serverIdRestrictionsList;
    }
}
