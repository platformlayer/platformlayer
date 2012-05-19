package org.platformlayer;

import java.net.InetAddress;

import org.platformlayer.choice.ScoreChooser;

public class EndpointChooser extends ScoreChooser<EndpointInfo, Integer> {

	InetAddressChooser addressChooser;

	public EndpointChooser() {
		super(true);
	}

	@Override
	protected Integer score(EndpointInfo candidate) {
		InetAddress address = candidate.getAddress();

		int score = 0;
		if (addressChooser != null) {
			score += addressChooser.score(address);
		}
		return score;
	}

	public static EndpointChooser preferIpv4() {
		EndpointChooser chooser = new EndpointChooser();
		chooser.addressChooser = InetAddressChooser.preferIpv4();
		return chooser;
	}

	public static EndpointChooser any() {
		EndpointChooser chooser = new EndpointChooser();
		return chooser;
	}

}
