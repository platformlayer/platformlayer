package org.platformlayer;

import java.net.Inet4Address;
import java.net.InetAddress;

import org.platformlayer.choice.ScoreChooser;

public class InetAddressChooser extends ScoreChooser<InetAddress, Integer> {
	final int scoreIpv4;
	final int scoreIpv6;

	public InetAddressChooser(int scoreIpv4, int scoreIpv6) {
		super(true);
		this.scoreIpv4 = scoreIpv4;
		this.scoreIpv6 = scoreIpv6;
	}

	@Override
	protected Integer score(InetAddress address) {
		int score = 0;
		score += address instanceof Inet4Address ? scoreIpv4 : scoreIpv6;
		return score;
	}

	public static InetAddressChooser preferIpv4() {
		InetAddressChooser chooser = new InetAddressChooser(100, 0);
		return chooser;
	}

	public static InetAddressChooser preferIpv6() {
		InetAddressChooser chooser = new InetAddressChooser(0, 100);
		return chooser;
	}

}
