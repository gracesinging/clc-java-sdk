package com.centurylink.cloud.sdk.servers.services.domain.group;

import com.centurylink.cloud.sdk.core.datacenters.services.domain.DataCenter;
import com.centurylink.cloud.sdk.servers.client.domain.group.GroupMetadata;
import com.centurylink.cloud.sdk.servers.client.domain.group.GroupResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ilya.drabenia
 */
public class GroupConverter {

    public List<Group> newGroupList(String dataCenter, List<GroupMetadata> groups) {
        return
            groups.stream()
                .map(curGroup -> newGroup(dataCenter, curGroup))
                .collect(Collectors.toList());
    }

    public Group newGroup(String dataCenter,
                          GroupMetadata group) {
        return
            new Group()
                .id(group.getId())
                .name(group.getName())
                .dataCenter(new DataCenter(dataCenter));
    }

}
