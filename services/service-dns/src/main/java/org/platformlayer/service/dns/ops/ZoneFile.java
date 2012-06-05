package org.platformlayer.service.dns.ops;

import java.util.Collections;
import java.util.List;

import org.platformlayer.PrimitiveComparators;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class ZoneFile {
	// $ORIGIN fathomdb.com
	// $TTL 30
	//
	// @ IN SOA ns23.pair.com. root.pair.com. (
	// 2001072300 ; Serial
	// 3600 ; Refresh
	// 300 ; Retry
	// 604800 ; Expire
	// 3600 ) ; Minimum
	//
	// @ IN NS ns23.pair.com.
	// @ IN NS ns0.ns0.com.
	//
	// test A 8.8.8.8

	final String domainName;
	final int ttl = 30;

	final List<Record> records = Lists.newArrayList();

	public ZoneFile(String domainName) {
		this.domainName = domainName;
	}

	String data;

	public void addRecord(Record record) {
		records.add(record);
		invalidate();
	}

	private void invalidate() {
		data = null;
	}

	static abstract class Record implements Comparable<Record> {
		public abstract void append(StringBuilder sb);

		protected int scoreClass(Class<? extends Record> c) {
			if (c == SoaRecord.class) {
				return 0;
			}
			if (c == NsRecord.class) {
				return 1;
			}
			if (c == AddressRecord.class) {
				return 2;
			}
			throw new IllegalStateException();
		}

		protected int compareByClass(Class<? extends Record> left, Class<? extends Record> right) {
			int leftScore = scoreClass(left);
			int rightScore = scoreClass(right);

			return PrimitiveComparators.compare(leftScore, rightScore);
		}
	}

	public String getKey() {
		return domainName;
	}

	// public DnsFile(String key) {
	// this.key = key;
	// }
	//
	// public void addA(String dnsName, List<String> addresses) {
	// for (String address : addresses) {
	// addA(dnsName, address);
	// }
	// }
	//
	// private void addA(String dnsName, String address) {
	// // +fqdn:ip:ttl:timestamp:lo
	// sb.append("+" + dnsName + ":" + address + "\n");
	// }

	// public void addNS(String dnsName, String address, String serverId) {
	// // .fqdn:ip:x:ttl:timestamp:lo
	// // Name server for our domain fqdn. tinydns-data creates
	// // an NS (``name server'') record showing x.ns.fqdn as a name server for fqdn;
	// // an A (``address'') record showing ip as the IP address of x.ns.fqdn; and
	// // an SOA (``start of authority'') record for fqdn listing x.ns.fqdn as the primary name server and
	// // hostmaster@fqdn as the contact address.
	// // You may have several name servers for one domain, with a different x for each server. tinydns will return
	// // only one SOA record per domain.
	// // If x contains a dot then tinydns-data will use x as the server name rather than x.ns.fqdn. This feature is
	// // provided only for compatibility reasons; names not ending with fqdn will force
	// // clients to contact parent servers much more often than they otherwise would, and will reduce the overall
	// // reliability of DNS. You should omit ip if x has IP addresses assigned elsewhere in
	// // data; in this case, tinydns-data will omit the A record.
	//
	// sb.append("." + dnsName + ":" + address + ":" + serverId + "\n");
	// }

	static class SoaRecord extends Record {
		final String primary;
		final String email;
		final long serial;
		final int refresh;
		final int retry;
		final int expiry;
		final int minimum;

		static final int DEFAULT_REFRESH = 600;
		static final int DEFAULT_RETRY = 30;
		static final int DEFAULT_EXPIRY = 600;
		static final int DEFAULT_MINIMUM = 30;

		static final String DEFAULT_EMAIL = "hostmaster";

		static long buildDefaultSerial() {
			long serial = System.currentTimeMillis();
			serial /= 1000;
			serial -= 1000000000;

			return serial;
		}

		public SoaRecord(String primary, String email, long serial, int refresh, int retry, int expiry, int minimum) {
			super();
			this.primary = primary;
			this.email = email;
			this.serial = serial;
			this.refresh = refresh;
			this.retry = retry;
			this.expiry = expiry;
			this.minimum = minimum;
		}

		public SoaRecord(String primary, String email, long serial) {
			this(primary, email, serial, DEFAULT_REFRESH, DEFAULT_RETRY, DEFAULT_EXPIRY, DEFAULT_MINIMUM);
		}

		public SoaRecord(String primary) {
			this(primary, DEFAULT_EMAIL, buildDefaultSerial(), DEFAULT_REFRESH, DEFAULT_RETRY, DEFAULT_EXPIRY,
					DEFAULT_MINIMUM);
		}

		@Override
		public void append(StringBuilder sb) {
			// @ IN SOA ns23.pair.com. root.pair.com. (
			// 2001072300 ; Serial
			// 3600 ; Refresh
			// 300 ; Retry
			// 604800 ; Expire
			// 3600 ) ; Minimum

			sb.append("@ IN SOA ");
			sb.append(primary);
			sb.append(". ");
			sb.append(email);
			sb.append(" (");

			sb.append(serial);
			sb.append(' ');

			sb.append(refresh);
			sb.append(' ');

			sb.append(retry);
			sb.append(' ');

			sb.append(expiry);
			sb.append(' ');

			sb.append(minimum);
			sb.append(")\n");
		}

		@Override
		public int compareTo(Record o) {
			if (getClass() != o.getClass()) {
				return compareByClass(getClass(), o.getClass());
			}

			// Should only be one SOA record anyway
			return primary.compareTo(((SoaRecord) o).primary);
		}
	}

	static class NsRecord extends Record {
		final String ns;

		public NsRecord(String ns) {
			this.ns = ns;
		}

		@Override
		public void append(StringBuilder sb) {
			// @ IN NS ns23.pair.com.
			// @ IN NS ns0.ns0.com.
			sb.append("@ IN NS ");
			sb.append(ns);
			sb.append(".\n");
		}

		@Override
		public int compareTo(Record o) {
			if (getClass() != o.getClass()) {
				return compareByClass(getClass(), o.getClass());
			}

			return ns.compareTo(((NsRecord) o).ns);
		}
	}

	/*
	 * An A or AAAA record
	 */
	static class AddressRecord extends Record {
		final String name;
		final List<String> addresses;

		public AddressRecord(String name, List<String> addresses) {
			this.name = name;
			this.addresses = addresses;
		}

		public AddressRecord(String name, String address) {
			this.name = name;
			this.addresses = Collections.singletonList(address);
		}

		@Override
		public void append(StringBuilder sb) {
			// test A 8.8.8.8

			for (String address : addresses) {
				sb.append(name);
				if (address.contains(":")) {
					sb.append(". AAAA ");
					sb.append(address);
				} else {
					sb.append(". A ");
					sb.append(address);
				}
				sb.append("\n");
			}
		}

		@Override
		public int compareTo(Record o) {
			if (getClass() != o.getClass()) {
				return compareByClass(getClass(), o.getClass());
			}

			AddressRecord other = (AddressRecord) o;
			int v = name.compareTo(other.name);
			if (v != 0) {
				return v;
			}

			return 0;
			// return PrimitiveComparators.compare(addresses, other.addresses)
		}
	}

	public String getData() {
		String data = this.data;
		if (data == null) {
			Collections.sort(records);

			StringBuilder sb = new StringBuilder();
			sb.append("$ORIGIN " + this.domainName + ".\n");
			sb.append("$TTL 30\n");

			for (Record record : records) {
				record.append(sb);
			}
			data = sb.toString();
			this.data = data;
		}
		return data;
	}

	@Override
	public String toString() {
		return getData();
	}

	public void addAddress(String dnsName, List<String> addresses) {
		AddressRecord a = new AddressRecord(dnsName, addresses);
		addRecord(a);
	}

	public void addNS(String domain, String address, String serverDnsName) {
		// .fqdn:ip:x:ttl:timestamp:lo
		// Name server for our domain fqdn. tinydns-data creates
		// an NS (``name server'') record showing x.ns.fqdn as a name server for fqdn;
		// an A (``address'') record showing ip as the IP address of x.ns.fqdn; and
		// an SOA (``start of authority'') record for fqdn listing x.ns.fqdn as the primary name server and
		// hostmaster@fqdn as the contact address.
		// You may have several name servers for one domain, with a different x for each server. tinydns will return
		// only one SOA record per domain.
		// If x contains a dot then tinydns-data will use x as the server name rather than x.ns.fqdn. This feature is
		// provided only for compatibility reasons; names not ending with fqdn will force
		// clients to contact parent servers much more often than they otherwise would, and will reduce the overall
		// reliability of DNS. You should omit ip if x has IP addresses assigned elsewhere in
		// data; in this case, tinydns-data will omit the A record.

		boolean hasSoa = Iterables.tryFind(records, Predicates.instanceOf(SoaRecord.class)).isPresent();

		if (!hasSoa) {
			SoaRecord soa = new SoaRecord(domain);
			addRecord(soa);
		}

		NsRecord ns = new NsRecord(serverDnsName);
		addRecord(ns);

		AddressRecord a = new AddressRecord(serverDnsName, address);
		addRecord(a);
	}

}
