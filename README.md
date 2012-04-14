PlatformLayer: Everything-as-a-service
======================================

Introduction
------------

PlatformLayer is open-source platform-as-a-service software.
Instead of tying you in to one stack though, you can build your platform
from whatever components you want (pick your own database,
webserver, load balancer etc).

PlatformLayer builds machines on clouds
running OpenStack.

Because there are so many choices of software you could make, PlatformLayer
 makes it super-easy to build a new service if one doesn't already exist.  That's
 why we say PlatformLayer is _everything_-as-a-service.

Right now it's early days, so please do contribute if your favorite service
isn't yet available.  Have a look in the services directory at the services
we're working on.  To build a service, you create a java classes which define
the 'model' which the service will expose; you create a 'controller' for each model
which describes how to build the model; you also create a service class
which ties it all together.

The intention is to have a lot of simple services which work together,
rather than a complex design.  Services build on each other to
produce complex systems.

There's lots of code for lots of different services in the tree, much of which
is still fairly experimental (it's early days).

These are good ones to try out first:

* memcache: Runs memcache, which is the simplest service (Cache-aaS)
* solr: Runs solr, allowing full-text indexing & search (Search-aaS)

Check the [wiki] for more information!


Installation
------------

Please see the [wiki]

[wiki]: https://github.com/platformlayer/platformlayer/wiki

