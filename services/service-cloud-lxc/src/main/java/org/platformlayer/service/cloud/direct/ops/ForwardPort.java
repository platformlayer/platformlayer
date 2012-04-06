package org.platformlayer.service.cloud.direct.ops;

import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsProvider;
import org.platformlayer.ops.OpsTarget;

public class ForwardPort {
    public OpsProvider<String> publicAddress;
    public int publicPort;
    public OpsProvider<String> privateAddress;
    public int privatePort;

    @Handler
    public void handler(OpsTarget target) throws OpsException {
        // TODO: Idempotency

        String dest = privateAddress.get() + ":" + privatePort;
        String publicIp = publicAddress.get();

        Command command;

        command = Command.build("iptables -t nat -A PREROUTING --dst {0} -p tcp --dport {1} -j DNAT --to {2}", publicIp, publicPort + "", dest);
        target.executeCommand(command);

        command = Command.build("iptables -t nat -A PREROUTING --dst {0} -p udp --dport {1} -j DNAT --to {2}", publicIp, publicPort + "", dest);
        target.executeCommand(command);
    }
}
