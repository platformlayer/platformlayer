package org.platformlayer.service.httpproxy.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.service.httpproxy.ops.UnmanagedHttpProxyController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(UnmanagedHttpProxyController.class)
// @GwtModel
public class UnmanagedHttpProxy extends ItemBase {
	public String url;
}
