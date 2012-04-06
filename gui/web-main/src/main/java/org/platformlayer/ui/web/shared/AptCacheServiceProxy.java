package org.platformlayer.ui.web.shared;

import org.platformlayer.service.aptcache.model.AptCacheService;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.EntityProxyId;
import com.google.web.bindery.requestfactory.shared.ProxyFor;
import com.google.web.bindery.requestfactory.shared.SkipInterfaceValidation;

@ProxyFor(value = AptCacheService.class /* , locator = AptCacheServiceLocator.class */)
@SkipInterfaceValidation
// @SkipInterfaceValidation as we rely on PlatformLayer ServiceLayerDecorator
public interface AptCacheServiceProxy extends EntityProxy {
    // Boilerplate
    EntityProxyId<AptCacheServiceProxy> stableId();

    // Getters / Setters
    String getDnsName();

    void setDnsName(String dnsName);
}
