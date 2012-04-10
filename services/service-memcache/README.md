Simple memcache service for platformlayer

pl put-item memcacheServer/mc1 "{ 'dnsName': 'mc1.platformlayer.org' }"
pl put-item networkConnection/mc1-world "{ 'sourceCidr': '0.0.0.0/0', 'destItem': 'memcacheServer/mc1', 'port': 11211 }"


pl get-endpoint memcacheServer/mc1
echo "stats" | nc ${ip} 11211


pl delete-item networkConnection/mc1-world

pl delete-item memcacheServer/mc1

