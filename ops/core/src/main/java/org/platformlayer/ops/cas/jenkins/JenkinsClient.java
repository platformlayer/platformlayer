package org.platformlayer.ops.cas.jenkins;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.platformlayer.PlatformLayerClientBase;
import org.platformlayer.xml.XmlHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.fathomdb.io.IoUtils;
import com.google.common.collect.Lists;

public class JenkinsClient {
	static final Logger log = LoggerFactory.getLogger(JenkinsClient.class);

	final URI baseUrl;
	final HttpClient httpClient;

	public JenkinsClient(HttpClient httpClient, URI baseUrl) {
		this.baseUrl = baseUrl;
		this.httpClient = httpClient;
	}

	String get(URI url) throws JenkinsException {
		HttpGet method = new HttpGet(url);
		InputStream is = null;
		try {
			HttpResponse response = httpClient.execute(method);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != 200) {
				throw new JenkinsException("Unexpected status code from Jenkins: " + statusCode, statusCode);
			}
			is = response.getEntity().getContent();
			return IoUtils.readAll(is);
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
			Element child = findChildElement(parent, childKey);
			if (child == null) {
				throw new IllegalArgumentException("Child element not found: " + childKey);
			}
			return child;
		}

		protected Element findChildElement(Element parent, String childKey) {
			Node child = XmlHelper.findUniqueChild(parent, childKey, false);
			return (Element) child;
		}
	}

	class FingerprintInfo extends JenkinsInfo {
		public FingerprintInfo(Element element) {
			super(element);
		}

		public BuildId getOriginalBuild() {
			Element original = findChildElement(getRoot(), "original");
			if (original == null) {
				return null;
			}

			String name = getChildElementContents(original, "name");
			String number = getChildElementContents(original, "number");

			return new BuildId(name, Integer.valueOf(number));
		}

		public String getFileName() {
			return getChildElementContents(getRoot(), "fileName");
		}

		public String getHash() {
			return getChildElementContents(getRoot(), "hash");
		}

		public BuildId getFirstUsage() {
			Element usage = findChildElement(getRoot(), "usage");
			if (usage == null) {
				return null;
			}

			String name = getChildElementContents(usage, "name");
			Element ranges = findChildElement(usage, "ranges");
			if (ranges == null) {
				return null;
			}
			Element range = findChildElement(ranges, "range");
			if (range == null) {
				return null;
			}
			String number = getChildElementContents(range, "start");

			return new BuildId(name, Integer.valueOf(number));
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

			for (Element element : findElements("artifact")) {
				artifacts.add(new ArtifactInfo(element));
			}
			return artifacts;
		}

		/**
		 * Note that fingerprints are only returned if you ask for them!
		 */
		public List<FingerprintInfo> getFingerprints() {
			List<FingerprintInfo> fingerprints = Lists.newArrayList();

			for (Element element : findElements("fingerprint")) {
				fingerprints.add(new FingerprintInfo(element));

			}
			return fingerprints;
		}

		private List<Element> findElements(String elementName) {
			List<Element> elements = Lists.newArrayList();
			NodeList childNodes = getRoot().getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node childNode = childNodes.item(i);
				if (childNode.getNodeType() != Node.ELEMENT_NODE) {
					continue;
				}

				String nodeName = childNode.getNodeName();
				if (nodeName.equals(elementName)) {
					elements.add((Element) childNode);
				}
			}
			return elements;
		}

		URI getBaseUrl() {
			return baseUrl;
		}

		public ArtifactInfo findArtifactByFileName(String fileName) {
			for (ArtifactInfo artifact : getArtifacts()) {
				if (artifact.getFileName().equals(fileName)) {
					return artifact;
				}
			}
			return null;
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

		try {
			String xml = get(apiXmlUrl);
			Document dom = parse(xml);
			return new FingerprintInfo(dom.getDocumentElement());
		} catch (JenkinsException e) {
			if (404 == e.getHttpStatusCode()) {
				log.debug("Jenkins returned 404 for " + apiXmlUrl);
				return null;
			}
			throw new JenkinsException("Error resolving artifact", e);
		}
	}

	private BuildInfo findBuildInfo(String relativeUrl, String treeFilter) throws JenkinsException {
		URI buildBaseUrl = baseUrl.resolve(relativeUrl);
		String apiPath = "api/xml";
		if (treeFilter != null) {
			apiPath += "?tree=" + PlatformLayerClientBase.urlEncode(treeFilter);
		}
		URI apiXmlUrl = buildBaseUrl.resolve(apiPath);
		String xml = get(apiXmlUrl);
		Document dom = parse(xml);

		return new BuildInfo(dom, buildBaseUrl);
	}

	public BuildInfo findBuildInfo(BuildId build) throws JenkinsException {
		String jobKey = build.getJobKey();
		if (jobKey.contains(":")) {
			// A maven module; use the parent

			String parent = jobKey.substring(0, jobKey.indexOf('/'));
			jobKey = parent;
		}
		String relativeUrl = "job/" + jobKey + "/" + build.getNumber() + "/";

		return findBuildInfo(relativeUrl, null);
	}

	public BuildInfo findPromotedBuild(String jobKey, String promotionKey, String treeFilter) throws JenkinsException {
		String relativeUrl = "job/" + jobKey + "/" + promotionKey + "/";

		return findBuildInfo(relativeUrl, treeFilter);
	}

	@Override
	public String toString() {
		return "JenkinsClient [baseUrl=" + baseUrl + "]";
	}

	public URI getBaseUrl() {
		return baseUrl;
	}

}
