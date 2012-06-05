

```

# Start with single node "cluster"
pl put-item zookeeperCluster/main "{ 'dnsName': 'zk.dns.platformlayer.net', 'clusterSize': 1 }"

# Grow to 3 node cluster
pl put-item zookeeperCluster/main "{ 'dnsName': 'zk.dns.platformlayer.net', 'clusterSize': 3 }"


```

