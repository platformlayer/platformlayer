package org.platformlayer.ops.networks;

import java.util.List;

import com.google.common.collect.Lists;

public class InterfaceModel {
	String internalName;
	String externalBridge;

	List<AddressModel> addresses = Lists.newArrayList();

	public String getInternalName() {
		return internalName;
	}

	public static InterfaceModel build(String internalName) {
		InterfaceModel model = new InterfaceModel();

		// String bridge4 = properties.getProperty("bridge", "br0");
		model.internalName = internalName;

		return model;
	}

	public void addAddress(AddressModel address) {
		addresses.add(address);
	}

	public AddressModel getPrimaryAddress() {
		return addresses.get(0);
	}

	public List<AddressModel> getExtraAddresses() {
		return addresses.subList(1, addresses.size());
	}
}
