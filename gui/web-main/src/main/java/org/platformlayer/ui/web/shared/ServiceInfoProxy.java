/*
 * Copyright 2007 Google Inc.
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
package org.platformlayer.ui.web.shared;

import java.util.List;

import org.platformlayer.core.model.ServiceInfo;
import org.platformlayer.ui.web.server.ServiceInfoLocator;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.EntityProxyId;
import com.google.web.bindery.requestfactory.shared.ProxyFor;

/**
 * Person DTO.
 */
@ProxyFor(value = ServiceInfo.class, locator = ServiceInfoLocator.class)
public interface ServiceInfoProxy extends EntityProxy {
    // Boilerplate
    EntityProxyId<ServiceInfoProxy> stableId();

    // Getters / Setters
    String getDescription();

    String getServiceType();

    List<String> getPublicTypes();

}
