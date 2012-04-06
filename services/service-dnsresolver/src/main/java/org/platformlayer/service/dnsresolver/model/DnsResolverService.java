package org.platformlayer.service.dnsresolver.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.codegen.GwtModel;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.service.dnsresolver.ops.DnsResolverServiceController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@GwtModel
@Controller(DnsResolverServiceController.class)
public class DnsResolverService extends ItemBase {
    public String dnsName;
}
