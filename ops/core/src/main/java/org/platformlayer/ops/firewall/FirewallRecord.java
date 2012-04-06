package org.platformlayer.ops.firewall;

import org.apache.log4j.Logger;
import org.platformlayer.ops.EnumUtils;

public class FirewallRecord {
    static final Logger log = Logger.getLogger(FirewallRecord.class);

    public enum Protocol {
        All, Tcp, Udp, Icmp, TcpOrUdp, Esp, Ah;

        public static Protocol parse(String proto) {
            if (proto.equalsIgnoreCase("tcp/udp"))
                return TcpOrUdp;
            return EnumUtils.valueOfCaseInsensitive(FirewallRecord.Protocol.class, proto);
        }

        public String toIpfString() {
            switch (this) {
            case TcpOrUdp:
                return "tcp/udp";

            default:
                return this.toString().toLowerCase();
            }
        }
    }

    public enum Decision {
        Block, Pass
    }

    public enum Direction {
        In, Out
    }

    Protocol protocol = Protocol.All;
    Decision decision;
    Direction direction;
    PortAddressFilter srcFilter = new PortAddressFilter();
    PortAddressFilter destFilter = new PortAddressFilter();
    boolean isQuick;
    boolean keepState;
    String device;
    boolean logPacket;
    public boolean fromIpsec;

    public FirewallRecord() {
    }

    public FirewallRecord deepCopy() {
        FirewallRecord clone = new FirewallRecord();
        clone.protocol = this.protocol;
        clone.decision = this.decision;
        clone.direction = this.direction;
        clone.srcFilter = this.srcFilter.deepCopy();
        clone.destFilter = this.destFilter.deepCopy();
        clone.isQuick = this.isQuick;
        clone.keepState = this.keepState;
        clone.device = this.device;
        clone.logPacket = this.logPacket;
        clone.fromIpsec = this.fromIpsec;

        if (!clone.equals(this))
            throw new IllegalStateException();

        return clone;
    }

    public String buildKey() {
        String key = protocol + "-" + decision + "-" + direction + "-" + srcFilter.buildKey() + "-" + destFilter.buildKey();
        if (device != null)
            key += "-" + device;
        if (isQuick)
            key += "-quick";
        if (keepState)
            key += "-keepstate";
        if (logPacket)
            key += "-logPacket";
        if (fromIpsec)
            key += "-fromIpsec";
        return key;
    }

    @Override
    public String toString() {
        String value = decision + " " + direction;
        if (logPacket)
            value += " log";
        value += " proto " + protocol + " from " + srcFilter + " to " + destFilter;
        if (device != null)
            value += " on " + device;
        if (isQuick)
            value += " quick";
        if (keepState)
            value += " keepstate";
        if (fromIpsec)
            value += " fromIpsec";
        return value;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public Decision getDecision() {
        return decision;
    }

    public void setDecision(Decision decision) {
        this.decision = decision;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public PortAddressFilter getSrcFilter() {
        return srcFilter;
    }

    public void setSrcFilter(PortAddressFilter srcFilter) {
        this.srcFilter = srcFilter;
    }

    public PortAddressFilter getDestFilter() {
        return destFilter;
    }

    public void setDestFilter(PortAddressFilter destFilter) {
        this.destFilter = destFilter;
    }

    public boolean isQuick() {
        return isQuick;
    }

    public void setQuick(boolean isQuick) {
        this.isQuick = isQuick;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public boolean isKeepState() {
        return keepState;
    }

    public void setKeepState(boolean keepState) {
        this.keepState = keepState;
    }

    public static FirewallRecord build(String rule) {
        return FirewallRecordParser.parseRule(rule);
    }

    public boolean isLogPacket() {
        return logPacket;
    }

    public void setLogPacket(boolean logPacket) {
        this.logPacket = logPacket;
    }

    public static FirewallRecord buildPassQuickLocalPort(Direction direction, int localPort, Protocol protocol, FirewallNetmask remoteNetmask) {
        FirewallRecord rule = new FirewallRecord();

        switch (direction) {
        case In:
            rule.getDestFilter().setPortHigh(localPort);
            rule.getDestFilter().setPortLow(localPort);
            rule.getSrcFilter().setNetmask(remoteNetmask);
            break;

        case Out:
            rule.getSrcFilter().setPortHigh(localPort);
            rule.getSrcFilter().setPortLow(localPort);
            rule.getDestFilter().setNetmask(remoteNetmask);
            break;

        default:
            throw new IllegalArgumentException();
        }

        rule.setProtocol(protocol);
        rule.setDecision(Decision.Pass);
        rule.setDirection(direction);
        rule.setQuick(true);

        return rule;
    }

    public static FirewallRecord buildPassQuickRemotePort(Direction direction, int remotePort, Protocol protocol, FirewallNetmask remoteNetmask) {
        FirewallRecord rule = new FirewallRecord();

        switch (direction) {
        case In:
            if (remotePort != 0) {
                rule.getSrcFilter().setPortHigh(remotePort);
                rule.getSrcFilter().setPortLow(remotePort);
            }
            rule.getSrcFilter().setNetmask(remoteNetmask);
            break;

        case Out:
            if (remotePort != 0) {
                rule.getDestFilter().setPortHigh(remotePort);
                rule.getDestFilter().setPortLow(remotePort);
            }
            rule.getDestFilter().setNetmask(remoteNetmask);
            break;

        default:
            throw new IllegalArgumentException();
        }

        rule.setProtocol(protocol);
        rule.setDecision(Decision.Pass);
        rule.setDirection(direction);
        rule.setQuick(true);

        return rule;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((decision == null) ? 0 : decision.hashCode());
        result = prime * result + ((destFilter == null) ? 0 : destFilter.hashCode());
        result = prime * result + ((device == null) ? 0 : device.hashCode());
        result = prime * result + ((direction == null) ? 0 : direction.hashCode());
        result = prime * result + (fromIpsec ? 1231 : 1237);
        result = prime * result + (isQuick ? 1231 : 1237);
        result = prime * result + (keepState ? 1231 : 1237);
        result = prime * result + (logPacket ? 1231 : 1237);
        result = prime * result + ((protocol == null) ? 0 : protocol.hashCode());
        result = prime * result + ((srcFilter == null) ? 0 : srcFilter.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FirewallRecord other = (FirewallRecord) obj;
        if (decision == null) {
            if (other.decision != null)
                return false;
        } else if (!decision.equals(other.decision))
            return false;
        if (destFilter == null) {
            if (other.destFilter != null)
                return false;
        } else if (!destFilter.equals(other.destFilter))
            return false;
        if (device == null) {
            if (other.device != null)
                return false;
        } else if (!device.equals(other.device))
            return false;
        if (direction == null) {
            if (other.direction != null)
                return false;
        } else if (!direction.equals(other.direction))
            return false;
        if (fromIpsec != other.fromIpsec)
            return false;
        if (isQuick != other.isQuick)
            return false;
        if (keepState != other.keepState)
            return false;
        if (logPacket != other.logPacket)
            return false;
        if (protocol == null) {
            if (other.protocol != null)
                return false;
        } else if (!protocol.equals(other.protocol))
            return false;
        if (srcFilter == null) {
            if (other.srcFilter != null)
                return false;
        } else if (!srcFilter.equals(other.srcFilter))
            return false;
        return true;
    }

    public static FirewallRecord pass() {
        FirewallRecord record = new FirewallRecord();
        record.setDecision(Decision.Pass);
        record.setQuick(true);
        return record;
    }

    public static FirewallRecord block() {
        FirewallRecord record = new FirewallRecord();
        record.setDecision(Decision.Block);
        record.setQuick(true);
        return record;
    }

    public FirewallRecord withKeepState() {
        setKeepState(true);
        return this;
    }

    public FirewallRecord dest(PortAddressFilter destFilter) {
        setDestFilter(destFilter);
        return this;
    }

    public FirewallRecord source(PortAddressFilter srcFilter) {
        setSrcFilter(srcFilter);
        return this;
    }

    public FirewallRecord in() {
        setDirection(Direction.In);
        return this;
    }

    public FirewallRecord protocol(Protocol protocol) {
        setProtocol(protocol);
        return this;
    }

    public FirewallRecord out() {
        setDirection(Direction.Out);
        return this;
    }

    public FirewallRecord fromIpsec() {
        this.fromIpsec = true;
        return this;
    }

    public boolean isFromIpsec() {
        return fromIpsec;
    }

    public void setFromIpsec(boolean fromIpsec) {
        this.fromIpsec = fromIpsec;
    }

    public static FirewallRecord buildBlockPort(Protocol protocol, int port) {
        return FirewallRecord.block().in().protocol(protocol).dest(PortAddressFilter.withPortRange(port, port));
    }

}
