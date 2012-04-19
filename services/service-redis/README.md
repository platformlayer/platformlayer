Simple redis service for platformlayer

pl put-item redisServer/redis1 "{ 'dnsName': 'redis1.platformlayer.org' }"

# Open redis port to the world (0.0.0.0/0)
# Note: this is a really bad idea - only safe for demos
pl put-item networkConnection/redis1-world "{ 'sourceCidr': '0.0.0.0/0', 'destItem': 'redisServer/redis1', 'port': 6379 }"


pl get-endpoint redisServer/redis1

endpoint=`pl get-endpoint redisServer/redis1`
endpoint=${endpoint/:/ }
echo "ENDPOINT=${endpoint}"
echo "stats" | nc ${endpoint}


pl delete-item networkConnection/redis1-world
pl delete-item redisServer/redis1

