```
pl put-item app/codex "{  'dnsName': 'codex.private.platformlayer.net',  'source': 'codex:promote-production:gwt-codex-1.0-SNAPSHOT.war' }"

# TODO... we'd like to do this.  We might have to extend HasPorts to report the target item as well as the port
#pl put-item networkConnection/world-codex "{ 'sourceCidr': '0.0.0.0/0', 'destItem': 'app/codex' }"

pl put-item networkConnection/world-codex "{ 'sourceCidr': '0.0.0.0/0', 'destItem': 'jettyService/codex' }"


```


