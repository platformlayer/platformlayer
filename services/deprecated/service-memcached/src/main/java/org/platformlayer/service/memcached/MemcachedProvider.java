package org.platformlayer.service.memcached;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.openstack.OpenstackException;
import org.openstack.compute.OpenstackComputeClient;
import org.openstack.image.OpenstackImageClient;
import org.platformlayer.inject.ObjectInjector;
import org.platformlayer.ops.OpsConfig;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.ServiceProviderBase;
import org.platformlayer.ops.UserInfo;
import org.platformlayer.service.memcached.model.MemcachedService;
import org.platformlayer.service.memcached.ops.MemcachedServiceController;
import org.platformlayer.xaas.Service;
import org.platformlayer.xaas.model.Managed;
import org.platformlayer.xaas.model.ServiceAuthorization;
import org.platformlayer.xaas.services.ModelClass;

import com.google.common.collect.Lists;

@Service("memcached")
public class MemcachedProvider extends ServiceProviderBase {

}
