package org.platformlayer.ops.networks;

import java.net.InetAddress;

import org.platformlayer.InetAddressChooser;

public class NearestAddressChooser extends InetAddressChooser {

	final NetworkPoint src;

	private NearestAddressChooser(int scoreIpv4, int scoreIpv6, NetworkPoint src) {
		super(scoreIpv4, scoreIpv6);
		this.src = src;
	}

	@Override
	protected Integer score(InetAddress address) {
		int score = super.score(address);

		NetworkPoint addrPoint = NetworkPoint.forAddress(address);
		int distance = NetworkPoint.estimateDistance(src, addrPoint);

		score -= distance;
		return score;
	}

	public static NearestAddressChooser build(NetworkPoint src) {
		int scoreIpv4 = 0;
		int scoreIpv6 = 0;

		// if (address instanceof Inet4Address) {
		// scoreIpv4 += 100;
		// } else {
		scoreIpv6 += 100;
		// }

		NearestAddressChooser chooser = new NearestAddressChooser(scoreIpv4, scoreIpv6, src);
		return chooser;
	}

}
