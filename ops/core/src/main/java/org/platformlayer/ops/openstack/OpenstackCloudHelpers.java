//package org.platformlayer.ops.openstack;
//
//import java.util.List;
//
//import javax.inject.Inject;
//
//import org.openstack.client.OpenstackCredentials;
//import org.platformlayer.ops.OpsException;
//import org.platformlayer.ops.machines.PlatformLayerHelpers;
//import org.platformlayer.service.machines.openstack.v1.OpenstackCloud;
//
//import com.google.common.collect.Lists;
//
//public class OpenstackCloudHelpers {
//
//	@Inject
//	PlatformLayerHelpers platformLayer;
//
//	public List<OpenstackCredentials> findOpenstackClouds() throws OpsException {
//		List<OpenstackCredentials> credentials = Lists.newArrayList();
//		for (OpenstackCloud cloud : platformLayer.listItems(OpenstackCloud.class)) {
//			OpenstackCredentials credential = new OpenstackCredentials(cloud.getEndpoint(), cloud.getUsername(),
//					cloud.getPassword(), cloud.getTenant());
//			credentials.add(credential);
//		}
//		return credentials;
//	}
// }
