package com.centurylink.cloud.sdk.servers.services.domain.datacenter.refs;

/**
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
}
