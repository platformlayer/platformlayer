```
pl put-item dnsServer/ns1 "{ 'dnsName': 'ns1.platformlayer.org' }"
pl put-item dnsServer/ns2 "{ 'dnsName': 'ns2.platformlayer.org' }"
pl put-item dnsServer/ns3 "{ 'dnsName': 'ns3.platformlayer.org' }"

pl put-item dnsZone/platformlayer.org "{ 'dnsName': 'platformlayer.org' }"
pl put-item dnsZone/platformlayer.net "{ 'dnsName': 'platformlayer.net' }"
pl put-item dnsZone/dns.platformlayer.net "{ 'dnsName': 'dns.platformlayer.net' }"

pl put-item networkConnection/ns1-world "{ 'sourceCidr': '0.0.0.0/0', 'destItem': 'dnsServer/ns1', 'port': 53, 'protocol': 'Udp' }"
pl put-item networkConnection/ns2-world "{ 'sourceCidr': '0.0.0.0/0', 'destItem': 'dnsServer/ns2', 'port': 53, 'protocol': 'Udp' }"
pl put-item networkConnection/ns3-world "{ 'sourceCidr': '0.0.0.0/0', 'destItem': 'dnsServer/ns3', 'port': 53, 'protocol': 'Udp' }"


pl put-item dnsRecord/resolver.platformlayer.net "{ 'dnsName': 'resolver.platformlayer.net', 'address': ['8.8.8.8', '8.8.4.4'] }"


```