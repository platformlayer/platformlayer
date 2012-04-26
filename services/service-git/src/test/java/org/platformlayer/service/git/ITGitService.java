package org.platformlayer.service.git;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import org.platformlayer.jobs.model.JobData;
import org.platformlayer.service.git.model.GitRepository;
import org.platformlayer.service.git.model.GitService;
import org.platformlayer.service.git.ops.GitServerController;
import org.platformlayer.tests.PlatformLayerApiTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

public class ITGitService extends PlatformLayerApiTest {

	@BeforeMethod
	public void beforeMethod() {
		reset();

		getTypedItemMapper().addClass(GitService.class);
	}

	@Test
	public void testCreateAndDeleteItem() throws Exception {
		String id = random.randomAlphanumericString(8);

		GitService service = new GitService();
		service.dnsName = id + ".test.platformlayer.org";
		service.ldapGroup = ;
		
		service = putItem(id, service);
		service = waitForHealthy(service);

		InetSocketAddress socketAddress = getEndpoint(service);
		Assert.assertFalse(isPortOpen(socketAddress));

		openFirewall(service, GitServerController.PORT);
		Assert.assertTrue(isPortOpen(socketAddress));

		String repoId = "repo" + id;
		GitRepository repo = new GitRepository();
		repo.name = repoId;
		repo = putItem(repoId, repo);
		repo = waitForHealthy(repo);

		// TODO: Make endpoint http://<ip>:<port>/<path>...
		String url = "http://" + socketAddress.getAddress().getHostAddress() + ":" + socketAddress.getPort() + "/solr";
		testGitRepo(url);

		GitService server = getItem(id + "-0", GitService.class);
		JobData configureJob = doConfigure(server);

		waitForJobComplete(configureJob);

		testSolrCustomField(url, customFieldKey);

		deleteItem(repo);
		deleteItem(service);
	}

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
