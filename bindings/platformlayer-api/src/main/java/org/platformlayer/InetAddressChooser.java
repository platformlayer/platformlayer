package org.platformlayer;

import java.net.Inet4Address;
import java.net.InetAddress;

import org.platformlayer.choice.ScoreChooser;

public class InetAddressChooser extends ScoreChooser<InetAddress, Integer> {
	int scoreIpv4;
	int scoreIpv6;

	public InetAddressChooser() {
		super(true);
	}

	@Override
	protected Integer score(InetAddress address) {
		int score = 0;
		score += address instanceof Inet4Address ? scoreIpv4 : scoreIpv6;
		return score;
	}

	public static InetAddressChooser preferIpv4() {
		InetAddressChooser chooser = new InetAddressChooser();
		chooser.scoreIpv4 = 100;
		chooser.scoreIpv6 = 0;
		return chooser;
	}

	public static InetAddressChooser preferIpv6() {
		InetAddressChooser chooser = new InetAddressChooser();
		chooser.scoreIpv4 = 0;
		chooser.scoreIpv6 = 100;
		return chooser;
	}

}
