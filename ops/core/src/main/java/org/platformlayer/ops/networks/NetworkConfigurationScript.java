//package org.platformlayer.ops.networks;
//
//import java.io.File;
//
//import org.slf4j.*;
//import com.fathomdb.Utf8;
//import org.platformlayer.ops.Injection;
//import org.platformlayer.ops.OpsException;
//import org.platformlayer.ops.filesystem.SyntheticFile;
//
//public class NetworkConfigurationScript extends SyntheticFile {
//	private static final Logger log = LoggerFactory.getLogger(NetworkConfigurationScript.class);
//
//	public String interfaceName;
//	public AddressModel address;
//
//	public static NetworkConfigurationScript build(String interfaceName, AddressModel address) {
//		NetworkConfigurationScript o = Injection.getInstance(NetworkConfigurationScript.class);
//		File ifupDir = new File("/etc/network/if-up.d");
//		o.filePath = new File(ifupDir, "ipv6-" + interfaceName);
//		o.fileMode = "0755";
//
//		o.interfaceName = interfaceName;
//		o.address = address;
//
//		return o;
//	}
//
//	@Override
//	protected byte[] getContentsBytes() throws OpsException {
//		ScriptBuilder sb = new ScriptBuilder();
//
//		String cidr = address.getCidr();
//		String gateway = address.getGateway();
//
//		sb.addLiteral("if [ \"$IFACE\" != \"" + interfaceName + "\" ]; then exit 0 fi");
//		sb.add("ip -6 addr add {0} dev {1}", cidr, interfaceName);
//		sb.add("ip -6 route add 2000::/3 via {0}", gateway);
//
//		return Utf8.getBytes(sb.toString());
//	}
// }
