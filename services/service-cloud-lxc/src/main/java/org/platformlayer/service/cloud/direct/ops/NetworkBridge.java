package org.platformlayer.service.cloud.direct.ops;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.process.ProcessExecution;
import org.platformlayer.ops.process.ProcessExecutionException;
import org.platformlayer.ops.tree.OpsTreeBase;

public class NetworkBridge extends OpsTreeBase {
    public String bridge = "br0";
    public IpRange ipRange;

    public static NetworkBridge build(String bridge, IpRange ipRange) {
        NetworkBridge conf = new NetworkBridge();
        conf.bridge = bridge;
        conf.ipRange = ipRange;
        return conf;
    }

    @Handler
    public void handler() throws OpsException, IOException {
        // TODO: Only if not installed
        OpsTarget target = OpsContext.get().getInstance(OpsTarget.class);

        boolean found = false;

        // ProcessExecution execution = target.executeCommand(Command.build("/usr/sbin/brctl show"));
        // String[] lines = execution.getStdOut().split("\n");
        // for (int i = 0; i < lines.length; i++) {
        // if (i == 0)
        // continue;
        //
        // String line = lines[i];
        // String[] components = line.split("\t+");
        // if (components.length != 4) {
        // throw new IllegalStateException("Error parsing line: " + line);
        // }
        // if (components[0].equals(bridge)) {
        // found = true;
        // }
        // }
        //
        try {
            target.executeCommand(Command.build("/usr/sbin/brctl showmacs {0}", bridge));
            found = true;
        } catch (ProcessExecutionException e) {
            ProcessExecution execution = e.getExecution();
            if (execution.getExitCode() == 1 && execution.getStdErr().contains("No such device")) {
                found = false;
            }
        }

        if (!found) {
            target.executeCommand(Command.build("/usr/sbin/brctl addbr {0}", bridge));
        }

        target.executeCommand(Command.build("/usr/sbin/brctl setfd {0} 0", bridge));

        String netmask = ipRange.getNetmask();
        InetAddress ip = ipRange.getAddress(1);

        target.executeCommand(Command.build("/sbin/ifconfig {0} {1} netmask {2} promisc up", bridge, ip, netmask));

        File procIPV4 = new File("/proc/sys/net/ipv4");
        File ipForward = new File(procIPV4, "ip_forward");
        target.executeCommand(Command.build("echo 1 > {0}", ipForward));

        File proxyArp = new File(procIPV4, "conf/" + bridge + "/proxy_arp");
        target.executeCommand(Command.build("echo 1 > {0}", proxyArp));

        // TODO: Idempotency?
        target.executeCommand(Command.build("/sbin/iptables -t nat -A POSTROUTING -o eth0 -j MASQUERADE"));

        // apt-get install bridge-utils
        //
        // /sbin/brctl addbr br0
        // /sbin/brctl setfd br0 0
        // #/sbin/ifconfig br0 down
        //
        // PREFIX=10.200.17
        // /sbin/ifconfig br0 ${PREFIX}.1 netmask 255.255.255.0 promisc up
        // echo 1 > /proc/sys/net/ipv4/ip_forward
        // echo 1 > /proc/sys/net/ipv4/conf/br0/proxy_arp
        //
        // iptables -t nat -A POSTROUTING -o eth0 -j MASQUERADE
    }

    @Override
    protected void addChildren() throws OpsException {

    }
}
