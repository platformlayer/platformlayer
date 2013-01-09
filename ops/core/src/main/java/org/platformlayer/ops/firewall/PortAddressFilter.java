package org.platformlayer.ops.firewall;

import org.slf4j.*;

public class PortAddressFilter {
	static final Logger log = LoggerFactory.getLogger(PortAddressFilter.class);

	/**
	 * Low port to which rule applies, INCLUSIVE
	 */
	int portLow;
	/**
	 * High port to which rule applies, EXCLUSIVE
	 */
	int portHigh;
	FirewallNetmask netmask = FirewallNetmask.Public;

	public PortAddressFilter deepCopy() {
		PortAddressFilter clone = new PortAddressFilter();
		clone.portLow = this.portLow;
		clone.portHigh = this.portHigh;
		clone.netmask = this.netmask.deepCopy();

		if (!clone.equals(this)) {
			throw new IllegalStateException();
		}

		return clone;
	}

	public String buildKey() {
		String key = "";
		if (netmask != null) {
			key += netmask.buildKey();
		}

		if (portLow != 0) {
			if (portLow != portHigh) {
				key += "-" + portLow + "-" + portHigh;
			} else {
				key += "-" + portLow;
			}
		}

		return key;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		if (netmask != null) {
			sb.append(netmask);
		} else {
			sb.append("all");
		}

		if (portLow != 0) {
			sb.append(' ');
			sb.append(Integer.toString(portLow));
			sb.append(' ');
			sb.append(Integer.toString(portHigh));
		}

		return sb.toString();
	}

	public int getPortLow() {
		return portLow;
	}

	public void setPortLow(int portLow) {
		this.portLow = portLow;
	}

	public int getPortHigh() {
		return portHigh;
	}

	public void setPortHigh(int portHigh) {
		this.portHigh = portHigh;
	}

	public FirewallNetmask getNetmask() {
		return netmask;
	}

	public PortAddressFilter setNetmask(FirewallNetmask netmask) {
		this.netmask = netmask;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((netmask == null) ? 0 : netmask.hashCode());
		result = prime * result + portHigh;
		result = prime * result + portLow;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		PortAddressFilter other = (PortAddressFilter) obj;
		if (netmask == null) {
			if (other.netmask != null) {
				return false;
			}
		} else if (!netmask.equals(other.netmask)) {
			return false;
		}
		if (portHigh != other.portHigh) {
			return false;
		}
		if (portLow != other.portLow) {
			return false;
		}
		return true;
	}

	public boolean isUnfiltered() {
		return portLow == 0 && portHigh == 0 && netmask.isUnfiltered();
	}

	public static PortAddressFilter withPortRange(int portLow, int portHigh) {
		PortAddressFilter filter = new PortAddressFilter();
		filter.setPortHigh(portHigh);
		filter.setPortLow(portLow);
		return filter;
	}

	public static PortAddressFilter withCidr(String cidr) {
		PortAddressFilter filter = new PortAddressFilter();
		filter.setNetmask(FirewallNetmask.buildCidr(cidr));
		return filter;
	}

}
