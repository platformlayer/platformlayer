package org.platformlayer.service.dns.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.codegen.GwtModel;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.service.dns.ops.DnsRecordController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(DnsRecordController.class)
@GwtModel
public class DnsRecord extends ItemBase {
    public String dnsName;
    public String recordType;
    public List<String> address;
}
