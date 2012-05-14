package org.platformlayer.ops.machines;

public class NetworkStrategy {

	public static final Strategy<NetworkAddress> PREFER_IPV6 = new Strategy<NetworkAddress>() {

		@Override
		public NetworkAddress choose(NetworkAddress a, NetworkAddress b) {
			boolean aIs6 = a.isIpv6();
			boolean bIs6 = b.isIpv6();

			if (aIs6 && !bIs6) {
				return a;
			}
			if (bIs6 && !aIs6) {
				return b;
			}

			throw new UnsupportedOperationException("Cannot choose between: " + a + " vs " + b);

			// if (address.contains(":")) {
			// log.info("Ignoring IPV6 address: " + address);
			// continue;
			// }
			// if (!Strings.isNullOrEmpty(address)) {
			// return address;
			// } }
		}
	};

}
