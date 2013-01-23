package org.platformlayer.ops.images.direct;

import java.net.InetAddress;

public class InetAddressPair {

	public final InetAddress src;
	public final InetAddress dest;

	public InetAddressPair(InetAddress src, InetAddress dest) {
		this.src = src;
		this.dest = dest;
	}

	@Override
	public String toString() {
		return "InetAddressPair [" + src + " => " + dest + "]";
	}

}
