//package org.platformlayer.ops.firewall;
//
//import org.apache.log4j.Logger;
//import org.platformlayer.ops.OpsException;
//import org.platformlayer.ops.firewall.FirewallRecord.Protocol;
//
//public class DefaultFirewallConfiguration {
//    static final Logger log = Logger.getLogger(DefaultFirewallConfiguration.class);
//
//    public static void addDefaultConfiguration(FirewallConfiguration configuration) throws OpsException {
//        // # Block any packets which are too short to be real
//        // block in log quick all with short
//        // # drop and log any IP packets with options set in them.
//        // #block in log all with ipopts
//        configuration.addRule("pass in quick proto icmp from any to any");
//        configuration.addRule("pass out quick proto icmp from any to any");
//
//        // # Allow ssh inbound & outbound
//        configuration.addPublicServiceTcpPort(22);
//        configuration.addClientTcpPort(22);
//
//        // We use DNS outbound
//        configuration.addClientUdpPort(53);
//        configuration.addClientTcpPort(53);
//
//        // We use HTTP & HTTPS outbound
//        configuration.addClientTcpPort(80);
//        configuration.addClientTcpPort(443);
//
//        // // # Allow HTTP & HTTPS
//        // configuration.addRule("pass in quick proto tcp from any to any port = 80");
//        // configuration.addRule("pass in quick proto tcp from any to any port = 443");
//
//        // # Allow Hudson
//        // configuration.addRule("pass in quick proto tcp from any to any port = 7777");
//
//        // // # Allow our RemoteAgent management tool
//        // configuration.addRule("pass in quick proto tcp from any to any port = 8078");
//
//        // // # We allow access to zookeeper (TODO)
//        // configuration.addRule("pass in quick proto tcp from any to any port = 2181");
//
//        // # Allow our CA
//        // # TODO: Only if we're running a CA?
//        // #pass in quick proto tcp from any to any port = 8079
//
//        // # Allow Continuous Integration server
//        // # TODO: Only if we're running a CI Server
//        // pass in quick proto tcp from any to any port = 8080
//
//        // # Allow Ops server
//        // # TODO: Only if we're running an ops Server
//        // pass in quick proto tcp from any to any port = 8081
//
//        // // # Allow ESP queries
//        // configuration.addRule("pass in quick proto tcp from any to any port = 8110");
//
//        // # Allow NTP packets in
//        configuration.addPublicServiceUdpPort(123);
//
//        OpsServer server = configuration.smartGetServer(true);
//        if (server.isBehindNat()) {
//            // If the server is behind NAT, we want to use NAT-T traversal, so we ignore port 500
//            // This is particularly important for EC2, which blocks ESP traffic between machines
//            // configuration.addRule("block in quick proto udp from any to any port = 500");
//
//            // Actually though, we still need to let port 500 through, otherwise IKE fails
//            // configuration.addRule("pass in quick proto esp from any to any");
//            configuration.addPublicServiceUdpPort(500);
//        } else {
//            // # TODO - restrict to private addresses only??
//            // #rules to allow IPSec VPN. ESP=encrypted. 500=IKE. 4500=NAT-T IKE
//            configuration.addPublicServiceUdpPort(500);
//        }
//
//        configuration.addRule("pass in quick proto esp from any to any");
//        configuration.addRule("pass out quick proto esp from any to any");
//        if (configuration.getOperatingSystem() == OperatingSystem.Linux) {
//            configuration.addRule(FirewallRecord.pass().in().protocol(Protocol.Ah));
//            configuration.addRule(FirewallRecord.pass().out().protocol(Protocol.Ah));
//        }
//        configuration.addPublicServiceUdpPort(4500);
//
//        // if (Ec2Server.isEc2(server))
//        // {
//        // // We allow traffic from the private network
//        // configuration.addRule(null)
//        // }
//
//        if (!ServerTypeUtils.isEc2(server)) {
//            // Allow iSCSI
//            configuration.addRule("pass out quick proto tcp from any to 10.0.0.0/16 port = 3260");
//            configuration.addRule("pass in quick proto tcp from 10.0.0.0/16 port = 3260 to any");
//        }
//
//        if (configuration.getOperatingSystem() == OperatingSystem.Linux) {
//            // iptables -A INPUT -m policy --pol ipsec --dir in -j ACCEPT
//            configuration.addRule(FirewallRecord.pass().in().fromIpsec());
//        }
//
//        // iptables -A OUTPUT -p tcp -m state --state NEW -j ACCEPT
//        configuration.addRule(FirewallRecord.pass().out().protocol(Protocol.Tcp).withKeepState());
//        // iptables -A INPUT -m state --state ESTABLISHED,RELATED -j ACCEPT
//        // configuration.addRule(FirewallRecord.pass().in().ifKeepStateEstablished());
//
//        // # Allow 3309, 3307 for secure MySQL
//        // configuration.addRule("pass in quick proto tcp from any to any port = 3307");
//        // configuration.addRule("pass in quick proto tcp from any to any port = 3309");
//        // configuration.addRule("pass in quick proto tcp from any to any port = 3310");
//
//        // # Allow incoming connections on our public interfaces
//        // # Note this is ports 10001 - 29999
//        // <#list server.publicAddresses as publicAddress>
//        // pass in quick proto tcp from any to ${publicAddress.internalAddress.hostAddress} port 10000 >< 30000
//        // </#list>
//
//        // #These are open for serving our internal DNS - ports 3300 - 3399
//        // configuration.addRule("pass in quick proto tcp from any to any port 3299 >< 3400");
//
//        // #These are open for serving to haproxy - ports 8000 - 8999
//        // configuration.addRule("pass in quick proto tcp from any to any port 7999 >< 9000");
//    }
//
// }
