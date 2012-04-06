//package org.platformlayer.service.network.ops;
//
//import javax.inject.Inject;
//
//import org.platformlayer.ops.Handler;
//import org.platformlayer.ops.Machine;
//import org.platformlayer.ops.machines.CloudController;
//import org.platformlayer.ops.machines.PlatformLayerCloudHelpers;
//
//public class CloudPortMapping {
//    public String network;
//    public int publicPort;
//    public int backendPort;
//
//    @Inject
//    PlatformLayerCloudHelpers cloudHelpers;
//
//    @Handler
//    public void handler(Machine machine) {
//        CloudController cloudController;
//
//        String endpoint = cloudController.bindEndpointToNetwork(network, publicPort, machine, backendPort);
//    }
// }
