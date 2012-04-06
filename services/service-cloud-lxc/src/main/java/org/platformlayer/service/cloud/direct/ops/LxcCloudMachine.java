package org.platformlayer.service.cloud.direct.ops;
//package org.openstack.service.lxc.ops;
//
//import java.util.Properties;
//
//import org.openstack.service.lxc.model.LxcInstance;
//import org.openstack.service.lxc.ops.cloud.LxcCloudHost;
//import org.platformlayer.ids.ManagedItemId;
//import org.platformlayer.ops.MachineBase;
//import org.platformlayer.ops.OpsException;
//import org.platformlayer.ops.PlatformLayerKey;
//
//public class LxcCloudMachine extends MachineBase {
//
//    private static final String PROPERTY_KEY_ADDRESS = "network.address";
//
//    private final LxcCloudHost lxcHost;
//    private final String lxcId;
//    private final Properties properties;
//
//    public LxcCloudMachine(LxcCloudHost lxcHost, String lxcId, Properties properties) {
//        this.lxcHost = lxcHost;
//        this.lxcId = lxcId;
//        this.properties = properties;
//    }
//
//    @Override
//    public void terminate() throws OpsException {
//        lxcHost.terminate(lxcId);
//    }
//
//    @Override
//    public String getAddress() {
//        String address = properties.getProperty(PROPERTY_KEY_ADDRESS);
//        return address;
//    }
//
//    public String getLxcKey() {
//        return lxcId;
//    }
//
//    @Override
//    public PlatformLayerKey getKey() {
//        return new PlatformLayerKey(LxcInstance.class, new ManagedItemId(lxcId));
//    }
// }
