Simple memcache service for platformlayer

pl put-item memcacheServer/mc1 "{ 'dnsName': 'mc1.platformlayer.org' }"

# Open memcache port to the world (0.0.0.0/0)
# Note: this is a really bad idea - only safe for demos
pl put-item networkConnection/mc1-world "{ 'sourceCidr': '0.0.0.0/0', 'destItem': 'memcacheServer/mc1', 'port': 11211 }"


endpoint=`pl get-endpoint memcacheServer/mc1`
endpoint=${endpoint/:/ }
echo "ENDPOINT=${endpoint}"
echo "stats" | nc ${endpoint}


pl delete-item networkConnection/mc1-world
pl delete-item memcacheServer/mc1

