package org.platformlayer.service.cloud.direct.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.AddressModel;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.service.cloud.direct.ops.DirectNetworkController;
import org.platformlayer.xaas.Controller;

import com.google.common.collect.Lists;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(DirectNetworkController.class)
public class DirectNetwork extends ItemBase {
	public List<AddressModel> networks;

	public List<AddressModel> getNetworks() {
		if (networks == null) {
			networks = Lists.newArrayList();
		}
		return networks;
	}
}
