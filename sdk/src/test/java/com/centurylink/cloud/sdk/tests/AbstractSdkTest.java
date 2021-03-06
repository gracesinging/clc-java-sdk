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

package com.centurylink.cloud.sdk.tests;

import com.centurylink.cloud.sdk.core.client.ClcClientException;
import com.centurylink.cloud.sdk.core.config.SdkConfiguration;
import com.centurylink.cloud.sdk.core.config.SdkConfigurationBuilder;
import com.centurylink.cloud.sdk.core.injector.SdkInjector;
import com.centurylink.cloud.sdk.core.injector.Module;
import com.centurylink.cloud.sdk.tests.mocks.BindMocksModule;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.centurylink.cloud.sdk.core.function.Streams.map;
import static com.centurylink.cloud.sdk.tests.TestGroups.RECORDED;
import static java.util.Arrays.asList;

/**
 * @author ilya.drabenia
 */
public abstract class AbstractSdkTest extends Assert {

    @BeforeClass(alwaysRun = true)
    @BeforeMethod(alwaysRun = true)
    public void injectDependencies() {
        SdkInjector
            .createInjector(composeModules())
            .injectMembers(this);
    }

    private List<Module> composeModules() {
        return new ArrayList<Module>() {{
            add(new Module() {
                @Override
                protected void configure() {
                    bind(SdkConfiguration.class, new SdkConfigurationBuilder().build());
                }
            });
            addAll(modules());
            add(new BindMocksModule(AbstractSdkTest.this));
        }};
    }

    protected List<Module> list(Module... modules) {
        return asList(modules);
    }

    protected abstract List<Module> modules();


    public <T> T fromJson(String filePath, Class<T> type) {
        try {
            return
                new ObjectMapper().readValue(
                    getClass().getResourceAsStream(filePath), type
                );
        } catch (IOException e) {
            throw new ClcClientException(e);
        }
    }

    public <T> T fromJson(String filePath, TypeReference<T> type) {
        try {
            return
                new ObjectMapper().readValue(
                    getClass().getResourceAsStream(filePath), type
                );
        } catch (IOException e) {
            throw new ClcClientException(e);
        }
    }

    // wiremock mixin support
    @BeforeMethod(groups = {RECORDED})
    public void startWireMockServer(Method method) {
        execMixinMethodIfExists("initWireMock", method);
    }


    @AfterMethod(groups = {RECORDED})
    public void stopWireMockServer() {
        execMixinMethodIfExists("destroyWireMock");
    }

    private void execMixinMethodIfExists(String methodName, Object... args) {
        try {
            List<Class<?>> parameterTypes = map(args, Object::getClass);
            Method initWireMockMethod = this.getClass().getMethod(
                methodName,
                parameterTypes.toArray(new Class[parameterTypes.size()])
            );

            if (initWireMockMethod != null) {
                initWireMockMethod.invoke(this, args);
            }
        } catch (IllegalAccessException | InvocationTargetException
                | NoSuchMethodException | SecurityException ex) {
            // ignore this exception
        }
    }

}
