package org.platformlayer.ops.packages;

import java.io.File;
import java.util.List;

import org.slf4j.*;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.filesystem.SyntheticFile;
import org.slf4j.LoggerFactory;

import com.fathomdb.Utf8;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class AptSourcesConfigurationFile extends SyntheticFile {
	private static final Logger log = LoggerFactory.getLogger(AptSourcesConfigurationFile.class);

	// Softlayer mirror is currently out of date (June 8 2012)
	// apt-get update can't cope with one out of date mirror, even if there's another one that is... ?
	private static final boolean USE_SOFTLAYER_MIRROR = false;

	// Sometimes the country mirrors have sync failures...
	// TODO: How to work around this??
	private static final boolean USE_COUNTRY = false;

	public static class AptSource {
		public String url;
		public String distro;
		public List<String> areas;

		public AptSource(String url, String distro, List<String> areas) {
			super();
			this.url = url;
			this.distro = distro;
			this.areas = areas;
		}
	}

	public static class DefaultAptSourcesConfigurationFile extends AptSourcesConfigurationFile {
		boolean built = false;

		@Override
		protected byte[] getContentsBytes() throws OpsException {
			if (!built) {
				OpsTarget target = OpsContext.get().getInstance(OpsTarget.class);

				String etcIssue = target.readTextFile(new File("/etc/issue"));
				if (etcIssue.startsWith("Debian")) {
					addDefaultsDebian(target);
				} else if (etcIssue.startsWith("Ubuntu")) {
					addDefaultsUbuntu(target);
				} else {
					throw new OpsException("Unknown operating system: " + etcIssue);
				}
				built = true;
			}
			return super.getContentsBytes();
		}
	}

	public AptSourcesConfigurationFile() {
		filePath = new File("/etc/apt/sources.list");
	}

	final List<AptSource> sources = Lists.newArrayList();

	public void addDefaultsDebian(OpsTarget target) {
		List<String> areas = Lists.newArrayList();
		areas.add("main");

		AsBlock asBlock = AsBlock.find(target);

		String mainDebianMirror = null;
		if (asBlock != null) {
			Country country = asBlock.getCountry();
			if (Objects.equal(AsBlock.HETZNER, asBlock)) {
				// Hmm... no wheezy mirrors yet??
				// add(new AptSource("http://mirror.hetzner.de/debian/package", "wheezy", areas));
				// add(new AptSource("http://mirror.hetzner.de/debian/security", "wheezy/updates", areas));
			}

			if (USE_SOFTLAYER_MIRROR && Objects.equal(AsBlock.SOFTLAYER, asBlock)) {
				add(new AptSource("http://mirrors.service.softlayer.com/debian", "wheezy", areas));
				add(new AptSource("http://mirrors.service.softlayer.com/debian-security", "wheezy/updates", areas));
			}

			if (USE_COUNTRY) {
				mainDebianMirror = "http://ftp." + country.getTld() + ".debian.org/debian/";
			} else {
				mainDebianMirror = "http://ftp.debian.org/debian/";
			}
		} else {
			log.warn("Could not determine AS-Block for:" + target);

			mainDebianMirror = "http://ftp.debian.org/debian/";
		}

		{
			add(new AptSource(mainDebianMirror, "wheezy", areas));

			// No wheezy updates yet
			// add(new AptSource(mainDebianMirror, "wheezy/updates", areas));
		}

		{
			// Nice to have it here, even if it isn't really live for wheezy just yet
			add(new AptSource("http://security.debian.org/", "wheezy/updates", areas));
		}
	}

	public void addDefaultsUbuntu(OpsTarget target) {
		List<String> areas = Lists.newArrayList();
		areas.add("main");
		areas.add("multiverse");
		areas.add("universe");

		AsBlock asBlock = AsBlock.find(target);

		String mainDebianMirror = null;
		if (asBlock != null) {
			Country country = asBlock.getCountry();
			if (Objects.equal(AsBlock.HETZNER, asBlock)) {
				// Hmm... no wheezy mirrors yet??
				// add(new AptSource("http://mirror.hetzner.de/debian/package", "wheezy", areas));
				// add(new AptSource("http://mirror.hetzner.de/debian/security", "wheezy/updates", areas));
			}

			if (USE_SOFTLAYER_MIRROR && Objects.equal(AsBlock.SOFTLAYER, asBlock)) {
				add(new AptSource("http://mirrors.service.softlayer.com/debian", "wheezy", areas));
				add(new AptSource("http://mirrors.service.softlayer.com/debian-security", "wheezy/updates", areas));
			}

			if (Objects.equal(AsBlock.GOOGLE_COMPUTE_ENGINE, asBlock)) {
				add(new AptSource("http://gce_ubuntu_mirror.commondatastorage.googleapis.com/precise", "precise", areas));
				add(new AptSource("http://gce_ubuntu_mirror.commondatastorage.googleapis.com/precise",
						"precise-security", areas));
				add(new AptSource("http://gce_ubuntu_mirror.commondatastorage.googleapis.com/precise",
						"precise-updates", areas));
			}

			mainDebianMirror = "http://" + country.getTld() + ".archive.ubuntu.com/ubuntu";
		} else {
			log.warn("Could not determine AS-Block for:" + target);

			mainDebianMirror = "http://archive.ubuntu.com/ubuntu";
		}

		{
			add(new AptSource(mainDebianMirror, "precise", areas));
			add(new AptSource(mainDebianMirror, "precise-security", areas));
			add(new AptSource(mainDebianMirror, "precise-updates", areas));

			// deb-src http://gce_ubuntu_mirror.commondatastorage.googleapis.com/precise precise main multiverse
			// universe
			// deb-src http://gce_ubuntu_mirror.commondatastorage.googleapis.com/precise precise-security main
			// multiverse universe
			// deb-src http://gce_ubuntu_mirror.commondatastorage.googleapis.com/precise precise-updates main multiverse
			// universe
			// deb-src http://us.archive.ubuntu.com/ubuntu precise main multiverse universe
			// deb-src http://us.archive.ubuntu.com/ubuntu precise-security main multiverse universe
			// deb-src http://us.archive.ubuntu.com/ubuntu precise-updates main multiverse universe
		}

	}

	private void add(AptSource source) {
		sources.add(source);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (AptSource source : sources) {
			sb.append("deb ");
			sb.append(source.url);
			sb.append(" ");
			sb.append(source.distro);
			sb.append(" ");
			sb.append(Joiner.on(" ").join(source.areas));
			sb.append("\n");
		}

		return sb.toString();
	}

	@Override
	protected byte[] getContentsBytes() throws OpsException {
		return Utf8.getBytes(toString());
	}

}
