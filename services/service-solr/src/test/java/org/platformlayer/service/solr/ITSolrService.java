package org.platformlayer.service.solr;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.platformlayer.PlatformLayerUtils;
import org.platformlayer.core.model.Tag;
import org.platformlayer.jobs.model.JobData;
import org.platformlayer.service.network.v1.NetworkConnection;
import org.platformlayer.service.solr.model.SolrCluster;
import org.platformlayer.service.solr.model.SolrSchemaField;
import org.platformlayer.service.solr.model.SolrServer;
import org.platformlayer.service.solr.ops.SolrConstants;
import org.platformlayer.tests.PlatformLayerApiTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

public class ITSolrService extends PlatformLayerApiTest {

	@BeforeMethod
	public void beforeMethod() {
		reset();

		getTypedItemMapper().addClass(SolrServer.class);
	}

	@Test
	public void testCreateAndDeleteItem() throws Exception {
		String id = random.randomAlphanumericString(8);

		SolrCluster create = new SolrCluster();
		create.dnsName = id + ".test.platformlayer.org";

		SolrCluster created = putItem(id, create);

		SolrCluster healthy = waitForHealthy(created);

		List<String> endpoints = PlatformLayerUtils.findEndpoints(healthy.getTags());

		if (endpoints.size() != 1) {
			throw new IllegalStateException("Expected exactly one endpoint");
		}

		System.out.println("Found endpoint: " + endpoints.get(0));

		InetSocketAddress socketAddress = parseSocketAddress(endpoints.get(0));

		Assert.assertFalse(isPortOpen(socketAddress));

		NetworkConnection firewallRule = new NetworkConnection();
		firewallRule.setSourceCidr("0.0.0.0/0");
		firewallRule.setDestItem(created.getKey());
		firewallRule.setPort(SolrConstants.API_PORT);

		firewallRule = putItem(id, firewallRule);

		waitForHealthy(firewallRule);

		Assert.assertTrue(isPortOpen(socketAddress));

		// TODO: Make endpoint http://<ip>:<port>/<path>...

		String url = "http://" + socketAddress.getAddress().getHostAddress() + ":" + socketAddress.getPort() + "/solr";
		testSolr(url);

		String customFieldKey = "customfield1";

		SolrSchemaField field = new SolrSchemaField();
		field.name = customFieldKey;
		field.type = "text_general";
		field.getTags().add(Tag.buildParentTag(created.getKey()));

		// TODO: Our scoping of keys is problematic now...
		// If two clusters both have the same key "customfield1", they can't have the same ID
		field = putItem(id + "-" + customFieldKey, field);
		waitForHealthy(field);

		// Currently, we need to do a manual configure operation...
		// TODO: trigger this automatically

		SolrServer server = getItem(id + "-0", SolrServer.class);
		JobData configureJob = doConfigure(server);

		waitForJobComplete(configureJob);

		testSolrCustomField(url, customFieldKey);

		deleteItem(created);
	}

	// // @Test
	// public void test2() throws Exception {
	// String url = "http://15.185.171.172:8080/solr/";
	// // testSolr(url);
	//
	// String customFieldKey = "customfield1";
	// testSolrCustomField(url, customFieldKey);
	// }

	// @Test
	// public void test3() throws Exception {
	// String id = "lLkaoeK5";
	//
	// SolrCluster cluster = getItem(id, SolrCluster.class);
	//
	// List<String> endpoints = PlatformLayerUtils.findEndpoints(cluster.getTags());
	//
	// if (endpoints.size() != 1) {
	// throw new IllegalStateException("Expected exactly one endpoint");
	// }
	//
	// System.out.println("Found endpoint: " + endpoints.get(0));
	//
	// InetSocketAddress socketAddress = parseSocketAddress(endpoints.get(0));
	//
	// String url = "http://" + socketAddress.getAddress().getHostAddress() + ":" + socketAddress.getPort() + "/solr";
	// // testSolr(url);
	//
	// String customFieldKey = "customfield" + random.nextLong();
	//
	// SolrSchemaField field = new SolrSchemaField();
	// field.name = customFieldKey;
	// field.type = "text_general";
	// field.getTags().add(Tag.buildParentTag(cluster.getKey()));
	//
	// // TODO: Our scoping of keys is problematic now...
	// // If two clusters both have the same key "customfield1", they can't have the same ID
	// field = putItem(id + "-" + customFieldKey, field);
	// waitForHealthy(field);
	//
	// SolrServer server = getItem(id + "-0", SolrServer.class);
	// JobData configureJob = doConfigure(server);
	//
	// waitForJobComplete(configureJob);
	//
	// Thread.sleep(5000);
	//
	// testSolrCustomField(url, customFieldKey);
	// }

	private void testSolr(String url) throws SolrServerException, IOException {
		CommonsHttpSolrServer client = new CommonsHttpSolrServer(url);

		int docCount = 1000;

		// Add some documents
		{
			for (int i = 0; i < docCount; i++) {
				SolrInputDocument doc = new SolrInputDocument();
				doc.addField("id", i);
				doc.addField("name_t", "document" + i);
				doc.addField("value_i", i);
				doc.addField("units_i", i % 10);
				doc.addField("content_t", random.randomText(40, 2000));
				client.add(doc);
			}

			client.commit();
		}

		// Query the documents
		{
			SolrQuery query = new SolrQuery();
			query.setQuery("units_i:2");
			query.addSortField("value_i", SolrQuery.ORDER.asc);
			query.setRows(Integer.MAX_VALUE);

			QueryResponse response = client.query(query);
			SolrDocumentList results = response.getResults();
			Assert.assertEquals(results.getNumFound(), docCount / 10);
			for (int i = 0; i < results.size(); i++) {
				SolrDocument doc = results.get(i);

				int docId = (i * 10) + 2;
				Assert.assertEquals(doc.get("id"), String.valueOf(docId));
				Assert.assertEquals(doc.get("units_i"), 2);
				Assert.assertEquals(doc.get("name_t"), "document" + docId);
			}
		}
	}

	private void testSolrCustomField(String url, String field) throws SolrServerException, IOException {
		CommonsHttpSolrServer client = new CommonsHttpSolrServer(url);

		int docCount = 10;

		List<String> fieldValues = Lists.newArrayList();

		// Add some documents
		{
			for (int i = 0; i < docCount; i++) {
				SolrInputDocument doc = new SolrInputDocument();
				doc.addField("id", i);
				doc.addField("value_i", i);

				String fieldValue = random.randomText(40, 2000);
				doc.addField(field, fieldValue);
				fieldValues.add(fieldValue);

				client.add(doc);
			}

			client.commit();
		}

		// Query the documents
		{
			SolrQuery query = new SolrQuery();
			query.setQuery("value_i:[* TO 9]");
			query.addSortField("value_i", SolrQuery.ORDER.asc);
			query.setRows(Integer.MAX_VALUE);

			QueryResponse response = client.query(query);
			SolrDocumentList results = response.getResults();
			Assert.assertEquals(results.getNumFound(), 10);
			for (int i = 0; i < results.size(); i++) {
				SolrDocument doc = results.get(i);

				int docId = i;
				Assert.assertEquals(doc.get("id"), String.valueOf(docId));
				Assert.assertEquals(doc.get("value_i"), docId);
				Assert.assertEquals(doc.get(field), fieldValues.get(i));
			}
		}
	}
}
