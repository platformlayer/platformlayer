package org.platformlayer.service.dns.ops;

import java.util.List;

public class DnsFile {
    final StringBuilder sb = new StringBuilder();
    final String key;

    public String getKey() {
        return key;
    }

    public DnsFile(String key) {
        this.key = key;
    }

    public void addA(String dnsName, List<String> addresses) {
        for (String address : addresses) {
            addA(dnsName, address);
        }
    }

    private void addA(String dnsName, String address) {
        // +fqdn:ip:ttl:timestamp:lo
        sb.append("+" + dnsName + ":" + address + "\n");
    }

    public String getData() {
        return sb.toString();
    }

    public void addNS(String dnsName, String address, String serverId) {
        // .fqdn:ip:x:ttl:timestamp:lo
        // Name server for our domain fqdn. tinydns-data creates
        // an NS (``name server'') record showing x.ns.fqdn as a name server for fqdn;
        // an A (``address'') record showing ip as the IP address of x.ns.fqdn; and
        // an SOA (``start of authority'') record for fqdn listing x.ns.fqdn as the primary name server and hostmaster@fqdn as the contact address.
        // You may have several name servers for one domain, with a different x for each server. tinydns will return only one SOA record per domain.
        // If x contains a dot then tinydns-data will use x as the server name rather than x.ns.fqdn. This feature is provided only for compatibility reasons; names not ending with fqdn will force
        // clients to contact parent servers much more often than they otherwise would, and will reduce the overall reliability of DNS. You should omit ip if x has IP addresses assigned elsewhere in
        // data; in this case, tinydns-data will omit the A record.

        sb.append("." + dnsName + ":" + address + ":" + serverId + "\n");
    }

}
