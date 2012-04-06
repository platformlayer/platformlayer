package org.platformlayer.ops.firewall;

import org.apache.log4j.Logger;

public class FirewallNetmask {
    static final Logger log = Logger.getLogger(FirewallNetmask.class);

    private static final String DEFAULT_AWS_GROUP = "default";

    private FirewallNetmask(NetmaskType netmaskType, String cidr, String awsSecurityGroup, String awsIdentifier) {
        super();
        this.netmaskType = netmaskType;
        this.cidr = cidr;
        this.awsSecurityGroup = awsSecurityGroup;
        this.awsIdentifier = awsIdentifier;

    }

    public FirewallNetmask deepCopy() {
        FirewallNetmask clone = new FirewallNetmask(this.netmaskType, this.cidr, this.awsSecurityGroup, this.awsIdentifier);

        if (!clone.equals(this))
            throw new IllegalStateException();

        return clone;
    }

    public enum NetmaskType {
        Cidr, LocalPrivateNetwork, AwsIdentifier
    };

    final NetmaskType netmaskType;
    final String cidr;
    final String awsSecurityGroup;
    final String awsIdentifier;

    public static final FirewallNetmask LocalPrivateNetwork = new FirewallNetmask(NetmaskType.LocalPrivateNetwork, null, null, null);
    public static final FirewallNetmask Public = new FirewallNetmask(NetmaskType.Cidr, "0.0.0.0/0", null, null);

    public NetmaskType getNetmaskType() {
        return netmaskType;
    }

    public boolean isUnfiltered() {
        return (getNetmaskType() == NetmaskType.Cidr && cidr.equals("0.0.0.0/0"));
    }

    public static FirewallNetmask buildCidr(String cidr) {
        if (cidr == null)
            cidr = "0.0.0.0/0";
        if (!cidr.contains("/"))
            cidr += "/32";
        return new FirewallNetmask(NetmaskType.Cidr, cidr, null, null);
    }

    public static FirewallNetmask buildAwsIdentifier(String awsId) {
        String fromAwsGroup;
        String fromAwsOwner;
        if (awsId.contains("@")) {
            String[] split = awsId.split("@");

            fromAwsGroup = split[0];
            fromAwsOwner = split[1];
        } else if (awsId.contains(":")) {
            String[] split = awsId.split(":");

            fromAwsOwner = split[0];
            fromAwsGroup = split[1];
        } else {
            fromAwsGroup = DEFAULT_AWS_GROUP;
            fromAwsOwner = awsId;
        }

        fromAwsOwner = normalizeAwsOwner(fromAwsOwner);

        fromAwsOwner = fromAwsOwner.trim().replace("-", "");
        return new FirewallNetmask(NetmaskType.AwsIdentifier, null, fromAwsGroup, fromAwsOwner);
    }

    public static String normalizeAwsOwner(String awsOwner) {
        if (awsOwner == null)
            return awsOwner;

        awsOwner = awsOwner.trim();
        awsOwner = awsOwner.replace("-", "");
        return awsOwner;
    }

    public String buildCidr() {
        switch (getNetmaskType()) {
        case Cidr:
            return this.cidr;

        default:
            throw new IllegalArgumentException("Unhandled type: " + getNetmaskType());

        }

    }

    public String getAwsSecurityGroup() {
        if (getNetmaskType() != NetmaskType.AwsIdentifier)
            throw new IllegalStateException();
        return awsSecurityGroup;
    }

    public String getAwsIdentifier() {
        if (getNetmaskType() != NetmaskType.AwsIdentifier)
            throw new IllegalStateException();
        return awsIdentifier;
    }

    @Override
    public String toString() {
        switch (getNetmaskType()) {
        case AwsIdentifier:
            return "AWS:" + getAwsSecurityGroup() + "@" + getAwsIdentifier();

        case Cidr:
            if (this.cidr.equals("0.0.0.0/0"))
                return "all";
            return this.cidr;

        case LocalPrivateNetwork:
            return getNetmaskType().toString();

        default:
            throw new IllegalArgumentException("Unhandled type: " + getNetmaskType());
        }
    }

    public String buildKey() {
        String key = toString();
        key = key.replace(" ", "");
        return key;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((awsIdentifier == null) ? 0 : awsIdentifier.hashCode());
        result = prime * result + ((awsSecurityGroup == null) ? 0 : awsSecurityGroup.hashCode());
        result = prime * result + ((cidr == null) ? 0 : cidr.hashCode());
        result = prime * result + ((netmaskType == null) ? 0 : netmaskType.hashCode());
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
        FirewallNetmask other = (FirewallNetmask) obj;
        if (awsIdentifier == null) {
            if (other.awsIdentifier != null)
                return false;
        } else if (!awsIdentifier.equals(other.awsIdentifier))
            return false;
        if (awsSecurityGroup == null) {
            if (other.awsSecurityGroup != null)
                return false;
        } else if (!awsSecurityGroup.equals(other.awsSecurityGroup))
            return false;
        if (cidr == null) {
            if (other.cidr != null)
                return false;
        } else if (!cidr.equals(other.cidr))
            return false;
        if (netmaskType == null) {
            if (other.netmaskType != null)
                return false;
        } else if (!netmaskType.equals(other.netmaskType))
            return false;
        return true;
    }

}
