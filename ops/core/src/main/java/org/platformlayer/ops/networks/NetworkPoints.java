package org.platformlayer.ops.networks;

import java.net.InetAddress;
import java.util.List;

import javax.inject.Inject;

import org.platformlayer.InetAddressChooser;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class NetworkPoints {
	private static final Logger log = LoggerFactory.getLogger(NetworkPoints.class);

	@Inject
	InstanceHelpers instances;

	public List<NetworkPoint> resolveAll(List<? extends ItemBase> items, boolean ignoreErrors) throws OpsException {
		List<NetworkPoint> points = Lists.newArrayList();
		for (ItemBase item : items) {
			NetworkPoint point = findNetworkPoint(item);
			if (point == null) {
				if (ignoreErrors) {
					log.debug("Ignoring unresolvable item");
				} else {
					throw new OpsException("Unable to resolve item: " + item.getKey());
				}
			}
			points.add(point);
		}
		return points;
	}

	public NetworkPoint getNetworkPoint(ItemBase item) throws OpsException {
		NetworkPoint networkPoint = findNetworkPoint(item);
		if (networkPoint == null) {
			throw new OpsException("Unable to resolve item to machine: " + item.getKey());
		}
		return networkPoint;
	}

	public NetworkPoint findNetworkPoint(ItemBase item) throws OpsException {
		Machine machine = instances.findMachine(item);
		if (machine == null) {
			log.warn("Unable to resolve item to machine: " + item.getKey());
			return null;
		}
		return machine.getNetworkPoint();
	}

	public InetAddress chooseBest(InetAddressChooser inetAddressChooser, NetworkPoint src, List<NetworkPoint> targets)
			throws OpsException {
		List<InetAddress> allAddresses = Lists.newArrayList();
		for (NetworkPoint target : targets) {
			List<InetAddress> findAddresses = target.findAddresses(src);
			allAddresses.addAll(findAddresses);
		}

		return inetAddressChooser.choose(allAddresses);
	}
}
