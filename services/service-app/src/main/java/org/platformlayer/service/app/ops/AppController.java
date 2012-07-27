package org.platformlayer.service.app.ops;

import java.util.List;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;
import org.platformlayer.ops.Bound;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.UniqueTag;
import org.platformlayer.ops.networks.HasPorts;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.ops.tree.OwnedItem;
import org.platformlayer.service.app.model.App;
import org.platformlayer.service.jetty.model.JettyContext;
import org.platformlayer.service.jetty.model.JettyService;

import com.google.common.collect.Lists;

public class AppController extends OpsTreeBase implements HasPorts {
	static final Logger log = Logger.getLogger(AppController.class);

	@Bound
	App model;

	@Handler
	public void handler() {
	}

	public static class JettyChildServer extends OwnedItem<JettyService> {
		@Bound
		App model;

		@Override
		protected JettyService buildItemTemplate() throws OpsException {
			Tag parentTag = Tag.buildParentTag(model.getKey());

			JettyService server = new JettyService();

			server.dnsName = model.dnsName;
			server.contexts = Lists.newArrayList();

			JettyContext context = new JettyContext();
			context.id = null;
			context.source = model.source;
			server.contexts.add(context);

			Tag uniqueTag = UniqueTag.build(model);
			server.getTags().add(uniqueTag);
			server.getTags().add(parentTag);

			server.key = PlatformLayerKey.fromId(model.getId());

			return server;
		}
	}

	@Override
	protected void addChildren() throws OpsException {
		addChild(JettyChildServer.class);
	}

	@Override
	public List<Integer> getPorts() {
		List<Integer> ports = Lists.newArrayList();
		ports.add(8080);
		return ports;
	}

}
