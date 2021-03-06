/*
 * (c) 2015 CenturyLink. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.centurylink.cloud.sdk.server.services.dsl.domain.server.filters;

import com.centurylink.cloud.sdk.base.services.client.domain.datacenters.DataCenterMetadata;
import com.centurylink.cloud.sdk.base.services.dsl.domain.datacenters.filters.DataCenterFilter;
import com.centurylink.cloud.sdk.base.services.dsl.domain.datacenters.refs.DataCenter;
import com.centurylink.cloud.sdk.core.function.Predicates;
import com.centurylink.cloud.sdk.core.services.filter.AbstractResourceFilter;
import com.centurylink.cloud.sdk.core.services.filter.Filter;
import com.centurylink.cloud.sdk.core.services.filter.evaluation.AndEvaluation;
import com.centurylink.cloud.sdk.core.services.filter.evaluation.OrEvaluation;
import com.centurylink.cloud.sdk.server.services.client.domain.group.GroupMetadata;
import com.centurylink.cloud.sdk.server.services.client.domain.server.metadata.ServerMetadata;
import com.centurylink.cloud.sdk.server.services.dsl.domain.group.filters.GroupFilter;
import com.centurylink.cloud.sdk.server.services.dsl.domain.group.refs.Group;
import com.centurylink.cloud.sdk.server.services.dsl.domain.server.PowerState;
import com.centurylink.cloud.sdk.server.services.dsl.domain.server.ServerStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.centurylink.cloud.sdk.core.function.Predicates.combine;
import static com.centurylink.cloud.sdk.core.function.Predicates.in;
import static com.centurylink.cloud.sdk.core.function.Streams.map;
import static com.centurylink.cloud.sdk.core.preconditions.ArgumentPreconditions.allItemsNotNull;
import static com.centurylink.cloud.sdk.core.preconditions.Preconditions.checkNotNull;
import static java.util.Arrays.asList;

/**
 * Class allow to select by specified search conditions needed subset of account servers
 *
 * @author Ilya Drabenia
 */
public class ServerFilter extends AbstractResourceFilter<ServerFilter> {
    private List<String> serverIds = new ArrayList<>();
    private GroupFilter groupFilter = new GroupFilter(Predicates.alwaysTrue());
    private Predicate<ServerMetadata> predicate = Predicates.alwaysTrue();
    private boolean searchInSubGroups = false;

    public ServerFilter() {
    }

    public ServerFilter(Predicate<ServerMetadata> predicate) {
        checkNotNull(predicate);

        this.predicate = predicate;
    }

    /**
     * Method allow to restrict target groups using data centers in which this groups exists.
     *
     * @param dataCenters is not null list of data center references
     * @return {@link ServerFilter}
     */
    public ServerFilter dataCenters(DataCenter... dataCenters) {
        groupFilter.dataCenters(dataCenters);

        return this;
    }

    /**
     * Method allow to provide filtering predicate that restrict group by data centers that contains its.
     *
     * @param predicate is not null filtering predicate
     * @return {@link ServerFilter}
     */
    public ServerFilter dataCentersWhere(Predicate<DataCenterMetadata> predicate) {
        groupFilter.dataCentersWhere(predicate);

        return this;
    }

    /**
     * Method allow to provide data center filter that allow to restrict groups by data centers that contains its
     *
     * @param filter is not null data center filter
     * @return {@link ServerFilter}
     */
    public ServerFilter dataCentersWhere(DataCenterFilter filter) {
        groupFilter.dataCentersWhere(filter);

        return this;
    }

    /**
     * Method allow to filter groups by its IDs. Matching will be strong and case sensitive.
     *
     * @param ids is not null list of group IDs
     * @return {@link ServerFilter}
     */
    public ServerFilter groupId(String... ids) {
        allItemsNotNull(ids, "Group ID list");

        groupFilter.id(ids);

        return this;
    }

    /**
     * Method allow to filter groups by key phrase that contains in its name.
     * Filtering will be case insensitive and will use substring matching.
     *
     * @param subStrings is not null list of target group names
     * @return {@link ServerFilter}
     */
    public ServerFilter groupNameContains(String... subStrings) {
        allItemsNotNull(subStrings, "Group name keywords");

        groupFilter.nameContains(subStrings);

        return this;
    }

    public ServerFilter groupNames(String... names) {
        allItemsNotNull(names, "Group names");

        groupFilter.names(names);

        return this;
    }

    /**
     * Method allow to filter groups using predicate.
     *
     * @param filter is not null group filtering predicate
     * @return {@link ServerFilter}
     */
    public ServerFilter groupsWhere(Predicate<GroupMetadata> filter) {
        groupFilter.where(filter);

        return this;
    }

    /**
     * Method allow to specify {@link GroupFilter} for restrict server groups
     *
     * @param filter is not a null group filter object
     * @return {@link ServerFilter}
     * @throws NullPointerException if {@code filter} is null
     */
    public ServerFilter groupsWhere(GroupFilter filter) {
        groupFilter = groupFilter.and(filter);

        return this;
    }

    /**
     * Method allow to restrict searched servers by groups
     *
     * @param groups is list of group references
     * @return {@link ServerFilter}
     */
    public ServerFilter groups(Group... groups) {
        allItemsNotNull(groups, "Groups");

        groupFilter = groupFilter.and(Filter.or(
            map(groups, Group::asFilter)
        ));

        return this;
    }

    /**
     * Method allow to specify custom search servers predicate
     *
     * @param filter is not null custom filtering predicate
     * @return {@link ServerFilter}
     * @throws NullPointerException if {@code filter} is null
     */
    public ServerFilter where(Predicate<ServerMetadata> filter) {
        checkNotNull(filter, "Filter must be not a null");

        predicate = predicate.and(filter);

        return this;
    }

    /**
     * Method allow to restrict servers by target IDs. Matching is case insensitive.
     *
     * @param ids is a list of string ID representations
     * @return {@link ServerFilter}
     */
    public ServerFilter id(String... ids) {
        return this.id(asList(ids));
    }

    public ServerFilter id(List<String> ids) {
        checkNotNull(ids, "List of server ID must be not null");
        allItemsNotNull(ids, "List of ID");

        serverIds.addAll(map(ids, String::toLowerCase));

        return this;
    }

    /**
     * Method allow to restrict servers by names. Matching is case insensitive.
     *
     * @param names is a list of server names
     * @return {@link ServerFilter}
     */
    public ServerFilter names(String... names) {
        return id(names);
    }

    /**
     * Method allow to restrict servers by keywords that contains in target server name.
     * Matching is case insensitive. Comparison use search substring algorithms.
     *
     * @param subStrings is a list of server name keywords
     * @return {@link ServerFilter}
     */
    public ServerFilter nameContains(String... subStrings) {
        allItemsNotNull(subStrings, "Name keywords");

        predicate = predicate.and(combine(
            ServerMetadata::getName, in(asList(subStrings), Predicates::containsIgnoreCase)
        ));

        return this;
    }

    /**
     * Method allow to find server that description contains one of specified keywords.
     * Matching is case insensitive.
     *
     * @param subStrings is list of not null keywords
     * @return {@link ServerFilter}
     */
    public ServerFilter descriptionContains(String... subStrings) {
        allItemsNotNull(subStrings, "Description keywords");

        predicate = predicate.and(combine(
            ServerMetadata::getDescription, in(asList(subStrings), Predicates::containsIgnoreCase)
        ));

        return this;
    }

    /**
     * Method allow to select only active servers
     *
     * @return {@link GroupFilter}
     */
    public ServerFilter onlyActive() {
        predicate = predicate.and(
            serverMetadata -> "active".equals(
                serverMetadata.getStatus()
            )
        );

        return this;
    }

    /**
     * Method allow to restrict status of target servers
     *
     * @param statuses is a list target server statuses
     * @return {@link GroupFilter}
     */
    public ServerFilter status(String... statuses) {
        allItemsNotNull(statuses, "Statuses");

        predicate = predicate.and(combine(
            ServerMetadata::getStatus, in(statuses)
        ));

        return this;
    }

    /**
     * Method allow to restrict status of target servers
     *
     * @param statuses is a list target server statuses
     * @return {@link GroupFilter}
     */
    public ServerFilter status(ServerStatus... statuses) {
        allItemsNotNull(statuses, "Statuses");

        predicate = predicate.and(combine(
            ServerMetadata::getStatus, in(map(statuses, ServerStatus::getCode))
        ));

        return this;
    }

    /**
     * Method allow to find servers with specified power state of target servers
     *
     * @param states is a list target server power states
     * @return {@link GroupFilter}
     */
    public ServerFilter powerStates(PowerState... states) {
        allItemsNotNull(states, "Power states");

        predicate = predicate.and(combine(
            s -> s.getDetails().getPowerState(), in(map(states, PowerState::getCode))
        ));

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServerFilter and(ServerFilter otherFilter) {
        checkNotNull(otherFilter, "Other filter must be not null");

        evaluation = new AndEvaluation<>(evaluation, otherFilter, ServerMetadata::getId);

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServerFilter or(ServerFilter otherFilter) {
        checkNotNull(otherFilter, "Other filter must be not null");

        evaluation = new OrEvaluation<>(evaluation, otherFilter, ServerMetadata::getId);

        return this;
    }

    public GroupFilter getGroupFilter() {
        return groupFilter;
    }

    public Predicate<ServerMetadata> getPredicate() {
        return predicate;
    }

    public List<String> getServerIds() {
        return serverIds;
    }

    /**
     * Returns search in subgroups
     * @return search in subgroups param
     */
    public boolean isSearchInSubGroups() {
        return searchInSubGroups;
    }

    /**
     * Specify search in subgroups
     * @param searchInSubGroups new search in subgroups
     * @return current ServerFilter instance
     */
    public ServerFilter searchInSubGroups(boolean searchInSubGroups) {
        this.searchInSubGroups = searchInSubGroups;
        return this;
    }
}
