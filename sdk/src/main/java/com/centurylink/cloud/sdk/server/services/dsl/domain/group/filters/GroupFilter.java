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

package com.centurylink.cloud.sdk.server.services.dsl.domain.group.filters;

import com.centurylink.cloud.sdk.base.services.client.domain.datacenters.DataCenterMetadata;
import com.centurylink.cloud.sdk.base.services.dsl.domain.datacenters.filters.DataCenterFilter;
import com.centurylink.cloud.sdk.base.services.dsl.domain.datacenters.refs.DataCenter;
import com.centurylink.cloud.sdk.core.function.Predicates;
import com.centurylink.cloud.sdk.core.function.Streams;
import com.centurylink.cloud.sdk.core.services.filter.AbstractResourceFilter;
import com.centurylink.cloud.sdk.core.services.filter.Filter;
import com.centurylink.cloud.sdk.core.services.filter.evaluation.AndEvaluation;
import com.centurylink.cloud.sdk.core.services.filter.evaluation.OrEvaluation;
import com.centurylink.cloud.sdk.server.services.client.domain.group.GroupMetadata;
import com.centurylink.cloud.sdk.server.services.dsl.domain.group.refs.Group;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.centurylink.cloud.sdk.core.function.Predicates.combine;
import static com.centurylink.cloud.sdk.core.function.Predicates.in;
import static com.centurylink.cloud.sdk.core.preconditions.ArgumentPreconditions.allItemsNotNull;
import static com.centurylink.cloud.sdk.core.preconditions.Preconditions.checkNotNull;
import static java.util.Arrays.asList;


/**
 * Class that used to filter groups of servers
 *
 * @author Ilya Drabenia
 */
public class GroupFilter extends AbstractResourceFilter<GroupFilter> {
    private List<String> ids = new ArrayList<>();
    private DataCenterFilter dataCenterFilter = new DataCenterFilter(Predicates.alwaysTrue());
    private Predicate<GroupMetadata> predicate = Predicates.alwaysTrue();

    public GroupFilter() {

    }

    public GroupFilter(Predicate<GroupMetadata> groupFilter) {
        this.predicate = groupFilter;
    }

    /**
     * Method allow to restrict target groups using data centers in which this groups exists.
     *
     * @param dataCenters is not null list of data center references
     * @return {@link GroupFilter}
     */
    public GroupFilter dataCenters(DataCenter... dataCenters) {
        dataCenterFilter.dataCenters(dataCenters);

        return this;
    }

    /**
     * Method allow to provide filtering predicate that restrict group by data centers that contains its.
     *
     * @param predicate is not null filtering predicate
     * @return {@link GroupFilter}
     */
    public GroupFilter dataCentersWhere(Predicate<DataCenterMetadata> predicate) {
        dataCenterFilter.where(
            dataCenterFilter.getPredicate().or(predicate)
        );

        return this;
    }

    /**
     * Method allow to provide data center filter that allow to restrict groups by data centers that contains its
     *
     * @param filter is not null data center filter
     * @return {@link GroupFilter}
     */
    public GroupFilter dataCentersWhere(DataCenterFilter filter) {
        dataCenterFilter = dataCenterFilter.and(filter);
        return this;
    }

    /**
     * Method allow to filter groups by its IDs. Matching will be strong and case sensitive.
     *
     * @param ids is not null list of group IDs
     * @return {@link GroupFilter}
     */
    public GroupFilter id(String... ids) {
        return id(asList(ids));
    }

    public GroupFilter id(List<String> ids) {
        checkNotNull(ids, "List of ids must be not a null");

        this.ids.addAll(ids);

        return this;
    }

    public GroupFilter groups(Group... groups) {
        allItemsNotNull(groups, "Groups");

        evaluation = new AndEvaluation<>(evaluation, Filter.or(
            Streams.map(groups, Group::asFilter)
        ), GroupMetadata::getId);

        return this;
    }

    /**
     * Method allow to filter groups by key phrase that contains in its name.
     * Filtering will be case insensitive and will use substring matching.
     *
     * @param subStrings is not null list of target group names
     * @return {@link GroupFilter}
     */
    public GroupFilter nameContains(String... subStrings) {
        checkNotNull(subStrings, "Name match criteria must be not a null");

        predicate = predicate.and(combine(
            GroupMetadata::getName, in(asList(subStrings), Predicates::containsIgnoreCase)
        ));

        return this;
    }

    /**
     * Method allow to filter groups by names.
     * Filtering will be case insensitive and will use string equality comparison.
     *
     * @param names is not null list of group names
     * @return {@link GroupFilter}
     */
    public GroupFilter names(String... names) {
        checkNotNull(names, "Name match criteria must be not a null");

        predicate = predicate.and(combine(
            GroupMetadata::getName, in(asList(names), Predicates::containsIgnoreCase)
        ));

        return this;
    }

    /**
     * Method allow to filter groups using predicate.
     *
     * @param filter is not null group filtering predicate
     * @return {@link GroupFilter}
     */
    public GroupFilter where(Predicate<GroupMetadata> filter) {
        checkNotNull(filter, "Filter predicate must be not a null");

        this.predicate = this.predicate.and(filter);

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroupFilter and(GroupFilter otherFilter) {
        checkNotNull(otherFilter, "Other filter must be not a null");

        evaluation = new AndEvaluation<>(evaluation, otherFilter, GroupMetadata::getId);

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroupFilter or(GroupFilter otherFilter) {
        checkNotNull(otherFilter, "Other filter must be not a null");

        evaluation = new OrEvaluation<>(evaluation, otherFilter, GroupMetadata::getId);

        return this;
    }

    public DataCenterFilter getDataCenterFilter() {
        return dataCenterFilter;
    }

    public Predicate<GroupMetadata> getPredicate() {
        return predicate;
    }

    public List<String> getIds() {
        return ids;
    }
}
