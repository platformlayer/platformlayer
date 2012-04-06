package org.platformlayer.service.imagefactory;

import org.platformlayer.Strings;

public class OperatingSystem {
    // Lenny not getting security updates
    // public static final OperatingSystem DebianLenny = new OperatingSystem(Distribution.Debian, "lenny");
    public static final OperatingSystem DebianSqueeze = new OperatingSystem(Distribution.Debian, "squeeze");
    public static final OperatingSystem DebianWheezy = new OperatingSystem(Distribution.Debian, "wheezy");

    final Distribution distribution;
    final String version;

    public enum Distribution {
        Ubuntu, Debian;

        public String getDefaultOsVersion() {
            switch (this) {
            case Debian:
                return "squeeze";
                // case Ubuntu:
                // return "lucid";

            default:
                throw new IllegalStateException("Unknown distribution: " + this);
            }
        }

        public static Distribution parse(String s) {
            for (Distribution d : Distribution.values()) {
                if (Strings.equalsIgnoreCase(d.toString(), s)) {
                    return d;
                }
            }

            throw new IllegalStateException("Unknown distribution: " + s);
        }
    }

    public OperatingSystem(Distribution distribution, String version) {
        this.distribution = distribution;
        this.version = version;
    }

    public Distribution getDistribution() {
        return distribution;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "OperatingSystem [distribution=" + distribution + ", version=" + version + "]";
    }

}
