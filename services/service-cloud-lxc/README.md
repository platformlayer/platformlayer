Configure a host
----------------

Requirements:

```bash
apt-get install bridge-utils
```

Sanity:

```bash
apt-get install --yes screen
``` 
Set /etc/network/interfaces like this:

```
# This file describes the network interfaces available on your system
# and how to activate them. For more information, see interfaces(5).

# The loopback network interface
auto lo
iface lo inet loopback

# The primary network interface
auto br100
iface br100 inet static
        bridge_ports eth0
        bridge_stp off
        bridge_maxwait 0
        bridge_fd 0
        address 192.168.128.1
        netmask 255.255.0.0
        gateway 192.168.1.1
```

```

# Create a filesystem backed pool of private IP addresses
mkdir -p /var/pools/network-private/all
mkdir -p /var/pools/network-private/assigned
for i in {1..255}
do
IP=192.168.130.${i}
FILE=/var/pools/network-private/all/${IP}
echo "address=${IP}" > ${FILE}
echo "bridge=br100" >> ${FILE}
echo "netmask=255.255.0.0" >> ${FILE}
echo "gateway=192.168.1.1" >> ${FILE}
done

# Create a filesystem backed pool of public IP addresses
mkdir -p /var/pools/network-public/assigned
touch /var/pools/network-public/all
for IP in 192.168.128.1; do
echo ${IP} > /var/pools/network-public/all
done


# Create a pool of VNC ports for KVM
mkdir -p /var/pools/kvm-vnc/all
mkdir -p /var/pools/kvm-vnc/assigned
for i in {5900..5999}
do
KEY=${i}
echo "port=${i}" > /var/pools/kvm-vnc/all/${KEY}
done

# Create a pool of monitor ports for KVM
mkdir -p /var/pools/kvm-monitor/all
mkdir -p /var/pools/kvm-monitor/assigned
for i in {10000..10099}
do
KEY=${i}
echo "port=${i}" > /var/pools/kvm-monitor/all/${KEY}
done

```


```

pl put-item directCloud/main "{}"
pl put-item directHost/host1 "{ 'cloud': 'directCloud/main', 'host': '192.168.128.1', 'ipRange': '192.168.128.1/32' }"


# Add the service SSH key to the host machine
pl get-sshkey machines-direct
pl get-sshkey machines-direct | ssh root@192.168.128.1 'tee -a /root/.ssh/authorized_keys'

# Create a simple image store on the same server
pl put-item imageStore/main "{ 'dnsName': '192.168.128.1' }"

pl get-sshkey imagestore
ssh root@192.168.128.1 'adduser --system --shell /bin/bash imagestore'
# We use socat to copy images around
ssh root@192.168.128.1 'apt-get install --yes socat'
ssh root@192.168.128.1 'mkdir -p /home/imagestore/.ssh/'
pl get-sshkey imagestore | ssh root@192.168.128.1 'tee -a /home/imagestore/.ssh/authorized_keys'
ssh root@192.168.128.1 'chown -R imagestore /home/imagestore/.ssh/'

```


```
# Test with memcache

pl put-item memcacheServer/directmc1 "{ 'dnsName': 'directmc1.platformlayer.org' }"

# Open memcache port to the world (0.0.0.0/0)
# Note: this is a really bad idea - only safe for demos
pl put-item networkConnection/directmc1-world "{ 'sourceCidr': '0.0.0.0/0', 'destItem': 'memcacheServer/directmc1', 'port': 11211 }"


endpoint=`pl get-endpoint memcacheServer/directmc1`
endpoint=${endpoint/:/ }
echo "ENDPOINT=${endpoint}"
echo "stats" | nc ${endpoint}

# Clean up
pl delete-item networkConnection/directmc1-world
pl delete-item memcacheServer/directmc1
```

#pl put-item memcacheServer/directmc2 "{ 'dnsName': 'directmc2.platformlayer.org' }"
#pl put-item networkConnection/directmc2-world "{ 'sourceCidr': '0.0.0.0/0', 'destItem': 'memcacheServer/directmc2', 'port': 11211 }"

