package org.platformlayer.service.solr.ops;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.Filter;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.templates.TemplateDataSource;
import org.platformlayer.service.solr.model.SolrCluster;
import org.platformlayer.service.solr.model.SolrSchemaField;
import org.platformlayer.service.solr.model.SolrServer;

import com.google.common.collect.Lists;

public class SolrTemplateData implements TemplateDataSource {
	static final Logger log = Logger.getLogger(SolrTemplateData.class);

	@Inject
	PlatformLayerHelpers platformLayer;

	SolrCluster cluster;

	public SolrCluster getCluster() throws OpsException {
		if (cluster == null) {
			cluster = OpsContext.get().getInstance(SolrCluster.class);
			if (cluster == null) {
				SolrServer server = getServer();
				String parent = server.getTags().findUnique(Tag.PARENT);
				if (parent != null) {
					cluster = platformLayer.getItem(PlatformLayerKey.parse(parent), SolrCluster.class);
				}
			}
		}
		return cluster;
	}

	SolrServer server;

	SolrServer getServer() {
		if (server == null) {
			server = OpsContext.get().getInstance(SolrServer.class);
		}
		return server;
	}

	public File getInstanceDir() {
		return new File(new File("/var/solr"), getInstanceKey());
	}

	public String getInstanceKey() {
		return "solr";
	}

	public List<SolrSchemaField> getFields() throws OpsException {
		SolrCluster cluster = getCluster();
		List<SolrSchemaField> fields = Lists.newArrayList();
		for (SolrSchemaField field : platformLayer.listItems(SolrSchemaField.class, Filter.byParent(cluster))) {
			fields.add(field);
		}
		return fields;
	}

	@Override
	public void buildTemplateModel(Map<String, Object> model) throws OpsException {
		model.put("instanceDir", getInstanceDir());
		model.put("fields", getFields());
		model.put("installDir", getInstallDir());
		model.put("installedWar", getInstalledWar());
		model.put("jvmArgs", getJvmArgs());
	}

	private String getJvmArgs() {
		StringBuilder sb = new StringBuilder();
		sb.append("-server ");

		// This is messy ... we want to leave lots of memory for mmap,
		// but also leave plenty for the JVM
		// TODO: What's best practice here?
		sb.append("-Xmx1024M ");
		return sb.toString();
	}

	public File getInstallDir() {
		return new File("/opt/apache-solr-3.6.0/apache-solr-3.6.0");
	}

	public File getInstalledWar() {
		return new File(getInstallDir(), "dist/apache-solr-3.6.0.war");
	}

}
