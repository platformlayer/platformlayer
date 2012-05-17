package org.platformlayer.service.httpfrontend.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.service.httpfrontend.ops.HttpSiteController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(HttpSiteController.class)
public class HttpSite extends ItemBase {
	public String hostname;

	public String backend;
}
