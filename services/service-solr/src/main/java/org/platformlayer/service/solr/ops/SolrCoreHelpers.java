package org.platformlayer.service.solr.ops;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.helpers.CurlRequest;
import org.platformlayer.ops.helpers.CurlResult;
import org.platformlayer.xml.XmlHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class SolrCoreHelpers {

	private static final Logger log = LoggerFactory.getLogger(SolrCoreHelpers.class);

	final OpsTarget target;
	final String coreKey;

	public SolrCoreHelpers(OpsTarget target, String coreKey) {
		this.target = target;
		this.coreKey = coreKey;
	}

	public static class SolrCoreStatus {
		final Document dom;

		public SolrCoreStatus(Document dom) {
			this.dom = dom;
		}

		public String getStartTime() throws OpsException {
			try {
				XPathFactory xPathfactory = XPathFactory.newInstance();
				XPath xpath = xPathfactory.newXPath();
				XPathExpression expr = xpath
						.compile("/response/lst[@name='status']/lst/date[@name='startTime']/text()");
				return (String) expr.evaluate(dom, XPathConstants.STRING);
			} catch (XPathExpressionException e) {
				throw new OpsException("Error reading value from XML", e);
			}
		}
	}

	public void reload() throws OpsException {
		execute("RELOAD");
	}

	public SolrCoreStatus getStatus() throws OpsException {
		CurlResult result = execute("STATUS");
		String xml = result.getBody();
		boolean namespaceAware = false;
		Document dom;
		try {
			dom = XmlHelper.parseXmlDocument(xml, namespaceAware);
		} catch (ParserConfigurationException e) {
			throw new OpsException("Error parsing XML output", e);
		} catch (SAXException e) {
			throw new OpsException("Error parsing XML output", e);
		} catch (IOException e) {
			throw new OpsException("Error parsing XML output", e);
		}
		return new SolrCoreStatus(dom);
	}

	private CurlResult execute(String action) throws OpsException {
		String url = "http://127.0.0.1:8080/solr/admin/cores?core=" + coreKey;

		url += "&action=" + action;

		CurlRequest request = new CurlRequest(url);
		CurlResult result = request.executeRequest(target);
		log.info("result: " + result);
		return result;
	}

}