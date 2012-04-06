package org.platformlayer.ops.firewall;

import java.util.Queue;

import org.apache.log4j.Logger;
import org.platformlayer.ops.EnumUtils;

import com.google.common.collect.Lists;

public class FirewallRecordParser {
    static final Logger log = Logger.getLogger(FirewallRecordParser.class);

    public static FirewallRecord parseRule(String rule) {
        String[] tokenArray = rule.split(" ");
        if (tokenArray.length < 3) {
            log.info("Cannot parse rule: " + rule);
            return null;
        }

        Queue<String> tokens = Lists.newLinkedList();
        for (String token : tokenArray)
            tokens.add(token);

        FirewallRecord record = new FirewallRecord();

        // @1 pass out all
        // @2 pass out quick on lo0 all
        // @3 pass out quick proto tcp/udp from any to any keep state
        // @1 block in log quick from any to any with short
        // @2 block in all
        // @3 pass in quick on lo0 all
        // @4 pass in quick proto icmp from any to any
        // @5 pass in quick proto udp from any port = 67 to any port = 68 keep state
        // @6 pass in quick proto tcp from any to any port = 22
        // @7 pass in quick proto udp from any to any port = 53
        // @8 pass in quick proto tcp from any to any port = 80
        // @9 pass in quick proto tcp from any to any port = 443
        // @10 pass in quick proto tcp from any to any port = 7777

        @SuppressWarnings("unused")
        int ruleId = -1;
        if (tokens.peek().startsWith("@")) {
            ruleId = Integer.parseInt(tokens.remove().substring(1));
        }

        record.decision = EnumUtils.valueOfCaseInsensitive(FirewallRecord.Decision.class, tokens.remove());
        record.direction = EnumUtils.valueOfCaseInsensitive(FirewallRecord.Direction.class, tokens.remove());

        while (!tokens.isEmpty()) {
            String nextToken = tokens.remove();
            if (nextToken.equals("quick")) {
                record.isQuick = true;
            } else if (nextToken.equals("proto")) {
                String proto = tokens.remove();
                record.protocol = FirewallRecord.Protocol.parse(proto);
            } else if (nextToken.equals("on")) {
                String device = tokens.remove();
                record.device = device;
            } else if (nextToken.equals("from")) {
                record.setSrcFilter(parseFilter(tokens));
            } else if (nextToken.equals("to")) {
                record.setDestFilter(parseFilter(tokens));
            } else if (nextToken.equals("keep")) {
                nextToken = tokens.remove();
                if (!nextToken.equals("state")) {
                    throw new IllegalStateException("Expected 'keep' to be followed by 'state'");
                }
                record.setKeepState(true);
            } else if (nextToken.equals("all")) {
                if (!tokens.isEmpty())
                    throw new IllegalStateException("Expected 'all' to be last token");
            } else if (nextToken.equals("log")) {
                record.setLogPacket(true);
            } else {
                throw new IllegalArgumentException("Unknown token in rule: " + rule + " token=" + nextToken);
            }
        }

        return record;
    }

    private static PortAddressFilter parseFilter(Queue<String> tokens) {
        PortAddressFilter filter = new PortAddressFilter();

        String netmask = tokens.remove();
        filter.setNetmask(parseNetmask(netmask));

        if (!tokens.isEmpty()) {
            String peekNext = tokens.peek();

            if (peekNext.equals("port")) {
                tokens.remove(); // port
                if (tokens.peek().equals("=")) {
                    // port = x
                    tokens.remove(); // =
                    int port = Integer.parseInt(tokens.remove());
                    filter.setPortHigh(port);
                    filter.setPortLow(port);
                } else {
                    // port1# >< port2# true if port is greater than port1 and less than port2
                    // => numbers are exclusive!
                    // port x >< y
                    String portLowString = tokens.remove();
                    String op = tokens.remove();
                    String portHighString = tokens.remove();

                    if (op.equals("><")) {
                        filter.setPortLow(Integer.parseInt(portLowString) + 1);
                        filter.setPortHigh(Integer.parseInt(portHighString) - 1);
                    } else {
                        throw new IllegalArgumentException("Unknown/unhandled port operator: " + op);
                    }
                }
            }
        }
        return filter;
    }

    private static FirewallNetmask parseNetmask(String token) {
        if (token.equals("any")) {
            return FirewallNetmask.Public;
        }
        String cidr = token;
        return FirewallNetmask.buildCidr(cidr);
    }

}
