PlatformLayer: Everything-as-a-service
======================================

Introduction
------------

PlatformLayer is open-source platform-as-a-service software.
Instead of tying you in to one stack though, you can build your platform
from whatever components you want (pick your own database,
webserver, load balancer etc).  PlatformLayer builds machines on clouds
running OpenStack.

Because there are so many choices of software you could make, PlatformLayer
 makes it super-easy to build a new service if one doesn't already exist.

Right now it's early days, so please do contribute if your favorite service
isn't yet available.  Have a look in the services directory at the services
we're working on.  To build a service, you create a java classes which define
the 'model' which the service will expose; you create a 'controller' for each model
which describes how to build the model; you also create a service class
which ties it all together.

The intention is to have a lot of simple services which work together,
rather than a complex design.  Services build on each other to
produce complex systems.

Basic code for these services exist already:

* instancesupervisor: launches OpenStack instances; in future it will be responsible for relaunching them if something happens
* dns: so we can have domain names that map to the machines
* dnsresolver: fast DNS resolution, including our internal domain names
* imagefactory: manages virtual machine disk images for services
* aptcache: caches software packages, which makes building images a bit faster
* collectd: used to collect metrics, in future we'll consume these metrics for automatic monitoring
* git: manages gitosis so that we can store code in our platform
* jenkins: does continuous integration, building code and pushing it to a webserver
* tomcat
* postgresql

Planned soon...

* A step-by-step how-to guide


Installation notes
------------------

Debian / Ubuntu

```bash
which mvn || apt-get install maven2
apt-get install git openjdk-6-jdk postgresql
git clone https://github.com/platformlayer/platformlayer.git
cd platformlayer
# (This will give you a sudo prompt, needed to install the DB)
./install.sh
```

