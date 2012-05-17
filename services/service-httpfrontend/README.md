```
pl put-item httpServer/primary1 "{ 'dnsName': 'http-primary1.platformlayer.org' }"
pl put-item httpSite/test.platformlayer.org "{ 'hostname': 'test.platformlayer.org', 'backend': 'openstack://cloud1/test.platformlayer.org' }"

pl put-item networkConnection/http-primary1-world "{ 'sourceCidr': '0.0.0.0/0', 'destItem': 'httpServer/primary1', 'port': 80 }"

```