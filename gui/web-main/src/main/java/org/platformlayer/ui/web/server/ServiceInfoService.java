/*
 * Copyright 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.platformlayer.ui.web.server;

import java.util.List;

import org.platformlayer.core.model.ServiceInfo;

import com.google.common.collect.Lists;

/**
 * Service object for Schedule entities, used to demonstrate the use of non-static service objects with RequestFactory. RequestFactory finds this service via the {@link ServiceInfoServiceLocator}.
 */
public class ServiceInfoService {

    public List<ServiceInfo> findAll() {
        List<ServiceInfo> serviceInfos = Lists.newArrayList();
        // serviceInfos.add(buildService("Service 1"));
        // serviceInfos.add(buildService("Service 2"));

        ServerContext serverContext = ServerContext.get();
        boolean management = false;
        List<ServiceInfo> allServices = serverContext.serviceDictionary.getAllServices(management);
        for (ServiceInfo service : allServices) {
            serviceInfos.add(service);
        }

        return serviceInfos;
    }

    private ServiceInfo buildService(String description) {
        ServiceInfo service1 = new ServiceInfo();
        service1.description = description;
        return service1;
    }
}
