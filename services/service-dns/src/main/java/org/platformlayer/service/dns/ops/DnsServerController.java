package org.platformlayer.service.dns.ops;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.Strings;
import org.platformlayer.ops.CloudContext;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.filesystem.ManagedDirectory;
import org.platformlayer.ops.filesystem.SimpleFile;
import org.platformlayer.ops.firewall.FirewallRecord.Protocol;
import org.platformlayer.ops.helpers.ImageFactory;
import org.platformlayer.ops.helpers.ServiceContext;
import org.platformlayer.ops.instances.DiskImageRecipeBuilder;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.metrics.collectd.CollectdCollector;
import org.platformlayer.ops.metrics.collectd.ManagedService;
import org.platformlayer.ops.networks.PublicEndpoint;
import org.platformlayer.ops.packages.PackageDependency;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.ops.users.PosixGroup;
import org.platformlayer.ops.users.PosixUser;
import org.platformlayer.service.dns.model.DnsServer;

public class DnsServerController extends OpsTreeBase {
    static final Logger log = Logger.getLogger(DnsServerController.class);

    @Inject
    OpsContext opsContext;

    @Inject
    CloudContext cloud;

    @Inject
    ImageFactory imageFactory;

    @Inject
    ServiceContext service;

    @Handler
    public void doOperation() throws OpsException, IOException {
    }

    @Override
    protected void addChildren() throws OpsException {
        DnsServer model = OpsContext.get().getInstance(DnsServer.class);
        if (Strings.isEmpty(model.dnsName)) {
            throw new IllegalArgumentException("dnsName must be specified");
        }

        // We'd like to auto-gen the disk image, but there's no way to auto-specify the OS at the moment
        InstanceBuilder instance = InstanceBuilder.build(model.dnsName, DiskImageRecipeBuilder.loadDiskImageResource(getClass(), "DiskImageRecipe.xml"));
        instance.addTagToManaged = true;
        instance.hostPolicy.allowRunInContainer = true;

        instance.publicPorts.add(53);

        addChild(instance);

        // Not included by default on older debian (lenny)
        // Is this really needed??
        // instance.addChild(PackageDependency.build("sysvconfig"));

        instance.addChild(PackageDependency.build("dbndns"));
        instance.addChild(PackageDependency.build("daemontools"));

        instance.addChild(CollectdCollector.build());

        {
            PublicEndpoint endpoint = injected(PublicEndpoint.class);
            // endpoint.network = null;
            endpoint.publicPort = 53;
            endpoint.backendPort = 53;
            endpoint.dnsName = model.dnsName;
            endpoint.protocol = Protocol.Udp;

            endpoint.tagItem = OpsSystem.toKey(model);
            endpoint.parentItem = OpsSystem.toKey(model);

            instance.addChild(endpoint);
        }

        instance.addChild(ManagedDirectory.build("/opt/scripts", "755"));
        instance.addChild(ManagedDirectory.build("/var/dns/records", "755"));

        instance.addChild(PackageDependency.build("monit"));

        // Not created by default on older debian (e.g. lenny)
        instance.addChild(ManagedDirectory.build("/etc/monit/conf.d", "755"));

        instance.addChild(SimpleFile.build(getClass(), new File("/etc/default/monit")));
        instance.addChild(SimpleFile.build(getClass(), new File("/opt/scripts/dnsdatabasemonitor")).setFileMode("550"));

        instance.addChild(SimpleFile.build(getClass(), new File("/opt/scripts/tinydns")).setFileMode("550"));

        instance.addChild(TinyDnsRecordBootstrap.build());

        instance.addChild(SimpleFile.build(getClass(), new File("/etc/monit/monitrc")));
        instance.addChild(SimpleFile.build(getClass(), new File("/etc/monit/conf.d/dnsdatabasemonitor.monit")));

        {
            String groupName = "dbndns";

            instance.addChild(PosixGroup.build(groupName));

            for (String userName : new String[] { "dnscache", "dnslog", "tinydns" }) {
                PosixUser user = PosixUser.build(userName);
                user.primaryGroup = groupName;
                instance.addChild(user);
            }
        }

        instance.addChild(TinyDnsBootstrap.build());

        instance.addChild(ManagedService.build("monit"));

        // TODO: Refresh other DNS servers so they also point to this server
    }
}
