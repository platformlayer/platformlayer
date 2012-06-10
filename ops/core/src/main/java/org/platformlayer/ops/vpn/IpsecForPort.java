package org.platformlayer.ops.vpn;

import java.io.File;

import org.openstack.utils.Utf8;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.SyntheticFile;
import org.platformlayer.ops.tree.OpsTreeBase;

public class IpsecForPort extends OpsTreeBase {
	public int port;

	@Handler
	public void handler() {
	}

	public static class SpdFile extends SyntheticFile {
		public int port;

		@Override
		protected byte[] getContentsBytes() throws OpsException {
			StringBuilder sb = new StringBuilder();

			sb.append(String.format("spdadd ::/0[%d] ::/0 any -P in ipsec esp/transport//require;\n", port));
			sb.append(String.format("spdadd ::/0[%d] ::/0 any -P out ipsec esp/transport//require;\n", port));
			sb.append(String.format("spdadd ::/0 ::/0[%d] any -P in ipsec esp/transport//require;\n", port));
			sb.append(String.format("spdadd ::/0 ::/0[%d] any -P out ipsec esp/transport//require;\n", port));

			return Utf8.getBytes(sb.toString());
		}
	}

	@Override
	protected void addChildren() throws OpsException {
		SpdFile spd = addChild(SpdFile.class);
		spd.filePath = new File("/etc/ipsec-tools.d/", "port-" + port + ".conf");
		spd.port = port;

		// Because we're just adding rules here, we can simply call setkey -f
		spd.setUpdateAction(Command.build("setkey -f {0}", spd.filePath));
	}

}
