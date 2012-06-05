package org.platformlayer.ops.packages;

import java.io.File;
import java.net.InetAddress;
import java.util.List;

import org.apache.log4j.Logger;
import org.openstack.utils.Utf8;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.SshOpsTarget;
import org.platformlayer.ops.filesystem.SyntheticFile;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class AptSourcesConfigurationFile extends SyntheticFile {
	private static final Logger log = Logger.getLogger(AptSourcesConfigurationFile.class);

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
				addDefaults(target);
				built = true;
			}
			return super.getContentsBytes();
		}
	}

	public AptSourcesConfigurationFile() {
		filePath = new File("/etc/apt/sources.list");
	}

	final List<AptSource> sources = Lists.newArrayList();

	public void addDefaults(OpsTarget target) {
		SshOpsTarget sshOpsTarget = (SshOpsTarget) target;
		InetAddress host = sshOpsTarget.getHost();

		List<String> areas = Lists.newArrayList();
		areas.add("main");

		AsBlock asBlock = AsBlock.find(host);

		String mainDebianMirror = null;
		if (asBlock != null) {
			Country country = asBlock.getCountry();
			if (Objects.equal(AsBlock.HETZNER, asBlock)) {
				// Hmm... no wheezy mirrors yet??
				// add(new AptSource("http://mirror.hetzner.de/debian/package", "wheezy", areas));
				// add(new AptSource("http://mirror.hetzner.de/debian/security", "wheezy/updates", areas));
			}

			if (Objects.equal(AsBlock.SOFTLAYER, asBlock)) {
				add(new AptSource("http://mirrors.service.softlayer.com/debian", "wheezy", areas));
				add(new AptSource("http://mirrors.service.softlayer.com/debian-security", "wheezy/updates", areas));
			}

			mainDebianMirror = "http://ftp." + country.getTld() + ".debian.org/debian/";
		} else {
			log.warn("Could not determine AS-Block for:" + host);

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
