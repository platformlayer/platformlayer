package org.platformlayer.service.memcached.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.service.memcached.ops.MemcachedServiceController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(MemcachedServiceController.class)
public class MemcachedService {
    public String dnsName;
}
