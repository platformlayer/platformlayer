package org.platformlayer.ops.pool;

import java.net.InetAddress;

import org.platformlayer.core.model.AddressModel;
import org.platformlayer.ops.networks.AddressModels;
import org.platformlayer.ops.networks.IpRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.net.InetAddresses;

public class NetworkPoolBuilder extends PoolBuilderBase<AddressModel> {
	static final Logger log = LoggerFactory.getLogger(NetworkPoolBuilder.class);

	final IpRange range;
	final int skipCount;

	final AddressModel addressModel;

	public NetworkPoolBuilder(String cidr, int skipCount) {
		this(cidr, skipCount, null);
	}

	public NetworkPoolBuilder(String cidr, int skipCount, AddressModel addressModel) {
		this.addressModel = addressModel;
		this.range = IpRange.parse(cidr);
		this.skipCount = skipCount;
	}

	@Override
	public Iterable<String> getItems() {
		return Iterables.transform(Iterables.skip(range.all(), skipCount), new Function<InetAddress, String>() {

			@Override
			public String apply(InetAddress address) {
				// AddressModel addressModel = new AddressModel();

				return address.getHostAddress();

				// String cidr = address.getHostAddress() + "/" + range.getNetmaskLength();
				// addressModel.cidr = cidr;
				// AddressModels.populateDefaults(addressModel);
				//
				// return addressModel;
			}

		});
	}

	@Override
	public String toKey(AddressModel item) {
		return item.getInetAddress().getHostAddress();
	}

	@Override
	public AddressModel toItem(String key) {
		AddressModel addressModel = new AddressModel();

		if (this.addressModel == null) {
			InetAddress address = InetAddresses.forString(key);
			String cidr = address.getHostAddress() + "/" + range.getNetmaskLength();
			addressModel.cidr = cidr;

			AddressModels.populateDefaults(addressModel);
		} else {
			addressModel.copyFrom(this.addressModel);

			addressModel.address = key;

			IpRange netmask;
			if (!Strings.isNullOrEmpty(addressModel.netmask)) {
				netmask = IpRange.parse(addressModel.netmask);
			} else {
				netmask = this.range;
			}

			String cidr = addressModel.address + "/" + netmask.getNetmaskLength();
			addressModel.cidr = cidr;

			AddressModels.populateDefaults(addressModel);
		}
		return addressModel;
	}

}
