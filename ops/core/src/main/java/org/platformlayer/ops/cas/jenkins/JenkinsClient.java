package org.platformlayer.ops.cas.jenkins;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.openstack.utils.Io;
import org.platformlayer.IoUtils;
import org.platformlayer.xml.XmlHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;

public class JenkinsClient {
	final URI baseUrl;
	final HttpClient httpClient;

	public JenkinsClient(HttpClient httpClient, URI baseUrl) {
		this.baseUrl = baseUrl;
		this.httpClient = httpClient;
	}

	String get(URI url) throws JenkinsException {
		HttpMethod method = new GetMethod(url.toString());
		InputStream is = null;
		try {
			int statusCode = httpClient.executeMethod(method);
			if (statusCode != 200) {
				throw new JenkinsException("Unexpected status code from Jenkins: " + statusCode);
			}
			is = method.getResponseBodyAsStream();
			return Io.readAll(is);
		} catch (IOException e) {
			throw new JenkinsException("Error making request to Jenkins", e);
		} finally {
			IoUtils.safeClose(is);
			method.releaseConnection();
		}
	}

	Document parse(String xml) throws JenkinsException {
		try {
			return XmlHelper.parseXmlDocument(xml, true);
		} catch (ParserConfigurationException e) {
			throw new JenkinsException("Error parsing xml from Jenkins", e);
		} catch (SAXException e) {
			throw new JenkinsException("Error parsing xml from Jenkins", e);
		} catch (IOException e) {
			throw new JenkinsException("Error parsing xml from Jenkins", e);
		}
	}

	class JenkinsInfo {
		final Element root;

		public JenkinsInfo(Element root) {
			this.root = root;
		}

		public JenkinsInfo(Document dom) {
			this(dom.getDocumentElement());
		}

		protected Element getRoot() {
			return root;
		}

		protected String getChildElementContents(Element parent, String childKey) {
			Element node = getChildElement(parent, childKey);
			return XmlHelper.getNodeContents(node);
		}

		protected Element getChildElement(Element parent, String childKey) {
			Node child = XmlHelper.findUniqueChild(parent, childKey);
			if (child == null) {
				throw new IllegalArgumentException("Child element not found: " + childKey);
			}
			return (Element) child;
		}
	}

	class FingerprintInfo extends JenkinsInfo {
		public FingerprintInfo(Document dom) {
			super(dom);
		}

		public BuildId getOriginalBuild() {
			Element element = getChildElement(getRoot(), "original");

			String name = getChildElementContents(element, "name");
			String number = getChildElementContents(element, "number");

			return new BuildId(name, Integer.valueOf(number));
		}

		public String getFileName() {
			return getChildElementContents(getRoot(), "fileName");
		}

	}

	class BuildInfo extends JenkinsInfo {
		private final URI baseUrl;

		public BuildInfo(Document dom, URI baseUrl) {
			super(dom);
			this.baseUrl = baseUrl;
		}

		class ArtifactInfo extends JenkinsInfo {
			public ArtifactInfo(Element root) {
				super(root);
			}

			public String getDisplayPath() {
				return getChildElementContents(getRoot(), "displayPath");
			}

			public String getFileName() {
				return getChildElementContents(getRoot(), "fileName");
			}

			public String getRelativePath() {
				return getChildElementContents(getRoot(), "relativePath");
			}

			public URI getArtifactUrl() {
				return BuildInfo.this.getBaseUrl().resolve("artifact/" + getRelativePath());
			}
		}

		public List<ArtifactInfo> getArtifacts() {
			List<ArtifactInfo> artifacts = Lists.newArrayList();

			NodeList childNodes = getRoot().getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node childNode = childNodes.item(i);
				if (childNode.getNodeType() != Node.ELEMENT_NODE) {
					continue;
				}

				String nodeName = childNode.getNodeName();
				if (nodeName.equals("artifact")) {
					artifacts.add(new ArtifactInfo((Element) childNode));
				}
			}
			return artifacts;
		}

		URI getBaseUrl() {
			return baseUrl;
		}

	}

	class BuildId {
		final String jobKey;
		final int number;

		public BuildId(String jobKey, int number) {
			this.jobKey = jobKey;
			this.number = number;
		}

		public String getJobKey() {
			return jobKey;
		}

		public int getNumber() {
			return number;
		}
	}

	public FingerprintInfo findByFingerprint(String hash) throws JenkinsException {
		URI fingerprintBaseUrl = baseUrl.resolve("fingerprint/" + hash + "/");
		URI apiXmlUrl = fingerprintBaseUrl.resolve("api/xml");

		String xml = get(apiXmlUrl);
		Document dom = parse(xml);

		return new FingerprintInfo(dom);
	}

	public BuildInfo findBuildInfo(BuildId build) throws JenkinsException {
		String relativeUrl = "job/" + build.getJobKey() + "/" + build.getNumber() + "/";

		URI buildBaseUrl = baseUrl.resolve(relativeUrl);
		URI apiXmlUrl = buildBaseUrl.resolve("api/xml");
		String xml = get(apiXmlUrl);
		Document dom = parse(xml);

		return new BuildInfo(dom, buildBaseUrl);
	}

}
