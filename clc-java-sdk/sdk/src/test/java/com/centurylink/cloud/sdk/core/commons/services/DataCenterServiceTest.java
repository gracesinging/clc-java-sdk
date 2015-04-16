package com.centurylink.cloud.sdk.core.commons.services;

import com.centurylink.cloud.sdk.tests.AbstractSdkTest;
import com.centurylink.cloud.sdk.core.auth.AuthModule;
import com.centurylink.cloud.sdk.core.commons.CommonsModule;
import com.centurylink.cloud.sdk.core.commons.client.domain.datacenters.DataCenterMetadata;
import com.centurylink.cloud.sdk.core.commons.services.domain.datacenters.DataCenter;
import com.centurylink.cloud.sdk.core.commons.services.domain.datacenters.DataCenters;
import com.centurylink.cloud.sdk.core.commons.services.domain.datacenters.refs.IdDataCenterRef;
import com.google.inject.Inject;
import com.google.inject.Module;
import org.testng.annotations.Test;

import java.util.List;

import static com.centurylink.cloud.sdk.tests.TestGroups.INTEGRATION;

@Test(groups = INTEGRATION)
public class DataCenterServiceTest extends AbstractSdkTest {

    @Inject
    DataCenterService dataCenterService;

    @Override
    protected List<Module> modules() {
        return list(new AuthModule(), new CommonsModule());
    }

    @Test
    public void testFindDataCenterByIdRef() {
        DataCenterMetadata dataCenter = dataCenterService.findByRef(DataCenter.refById("de1"));

        assert dataCenter.getId().equalsIgnoreCase("de1");
    }

    @Test
    public void testFindDataCenterByNameRef() {
        DataCenterMetadata dataCenter = dataCenterService.findByRef(DataCenter.refByName("FrankFurt"));

        assert dataCenter.getId().equalsIgnoreCase("DE1");
    }

    @Test
    public void testFindMultipleDataCentersByRef() {
        List<DataCenterMetadata> dataCenters = dataCenterService.findByRef(
            DataCenters.DE_FRANKFURT,
            DataCenters.CA_TORONTO_1
        );

        assert verifyDataCenterById(dataCenters, 0, DataCenters.DE_FRANKFURT);
        assert verifyDataCenterById(dataCenters, 1, DataCenters.CA_TORONTO_1);
    }

    public boolean verifyDataCenterById(List<DataCenterMetadata> dataCenters, int index,
                                        IdDataCenterRef expectedDataCenter) {
        return dataCenters.get(index).getId().equalsIgnoreCase(expectedDataCenter.getId());
    }

}