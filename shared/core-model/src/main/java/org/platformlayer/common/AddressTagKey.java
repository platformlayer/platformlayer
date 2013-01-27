package org.platformlayer.common;

import java.net.InetAddress;

import org.platformlayer.core.model.AddressModel;
import org.platformlayer.core.model.Tag;

import com.google.common.net.InetAddresses;

public class AddressTagKey extends TagKey<InetAddress> {
	public AddressTagKey(String key) {
		super(key, null);
	}

	@Override
	protected InetAddress toT(String s) {
		return InetAddresses.forString(s);
	}

	public Tag build(InetAddress t) {
		return Tag.build(key, t.getHostAddress());
	}

	public Tag build(AddressModel addressModel) {
		return build(addressModel.getInetAddress());
	}
}