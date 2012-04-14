package org.platformlayer.service.solr.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.service.solr.ops.SolrServerController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(SolrServerController.class)
public class SolrServer extends ItemBase {
	public String dnsName;

	public String clusterId;
}
