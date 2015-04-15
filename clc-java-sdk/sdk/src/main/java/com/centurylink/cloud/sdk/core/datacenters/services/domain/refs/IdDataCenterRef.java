package com.centurylink.cloud.sdk.core.datacenters.services.domain.refs;

import com.centurylink.cloud.sdk.core.datacenters.services.domain.filters.DataCenterFilter;

/**
 * Class allow to specify unique data center by ID
 *
 * @author ilya.drabenia
 */
public class IdDataCenterRef extends DataCenterRef {
    private final String id;

    public IdDataCenterRef(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public DataCenterFilter asFilter() {
        return new DataCenterFilter().idIn(id);
    }
}
