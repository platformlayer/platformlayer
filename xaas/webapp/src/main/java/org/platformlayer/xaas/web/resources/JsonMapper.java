package org.platformlayer.xaas.web.resources;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.xaas.web.jaxrs.JaxbContextHelper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonMapper {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(JsonMapper.class);

	@Inject
	JaxbContextHelper jaxbContextHelper;

	@Inject
	ObjectMapper objectMapper;

	// private Action readAction(String json) throws XMLStreamException, JAXBException {
	// JAXBContext jaxbContext = jaxbContextHelper.getJaxbContext(Action.class);
	//
	// final HashMap<String, String> xmlNamespaceToJsonPrefix = Maps.newHashMap();
	// xmlNamespaceToJsonPrefix.put("http://platformlayer.org/core/v1.0", "core");
	// for (ServiceInfo provider : serviceProviderDictionary.getAllServices()) {
	// String xmlNs = provider.namespace;
	// String jsonNs = xmlNs;
	//
	// jsonNs = jsonNs.replace("http://platformlayer.org/service/", "");
	// jsonNs = jsonNs.replace("/v1.0", "");
	//
	// jsonNs = jsonNs.replace("http://", "");
	// jsonNs = jsonNs.replace("/", ".");
	//
	// xmlNamespaceToJsonPrefix.put(xmlNs, jsonNs);
	// }
	//
	// Configuration configuration = JsonHelper.buildConfiguration(xmlNamespaceToJsonPrefix);
	// XMLStreamReader xmlStreamReader = JsonHelper.buildStreamReader(json, configuration);
	//
	// // if (log.isDebugEnabled()) {
	// // String xml = XmlHelper.toXml(xmlStreamReader);
	// // log.debug("XML = " + xml);
	// // xmlStreamReader = XmlHelper.buildXmlStreamReader(xml);
	// // }
	//
	// Object item = XmlHelper.unmarshal(jaxbContext, xmlStreamReader);
	//
	// Action typedItem = CastUtils.checkedCast(item, Action.class);
	// return typedItem;
	// }

	public <T> T readItem(Class<T> javaClass, String json) throws JsonProcessingException, IOException {
		// JAXBContext jaxbContext = jaxbContextHelper.getJaxbContext(javaClass);

		// ElementInfo elementInfo = XmlHelper.getXmlElementInfo(javaClass);
		// if (elementInfo == null) {
		// throw new IllegalStateException("Cannot determine XML info for: " + javaClass);
		// }

		// final HashMap<String, String> xmlNamespaceToJsonPrefix = Maps.newHashMap();
		// xmlNamespaceToJsonPrefix.put(elementInfo.namespace, "");
		// xmlNamespaceToJsonPrefix.put("http://platformlayer.org/core/v1.0", "core");

		// boolean wrap = false;
		// if (wrap) {
		// try {
		// JSONObject jsonObject = new JSONObject(json);
		//
		// JSONObject wrapped = new JSONObject();
		// wrapped.put(elementInfo.elementName, jsonObject);
		//
		// json = wrapped.toString();
		// } catch (JSONException e) {
		// throw new IllegalArgumentException("Error parsing JSON", e);
		// }
		// }

		T item = objectMapper.readValue(json, javaClass);
		return item;

		// JAXBContext jaxbContext = jaxbContextHelper.getJaxbContext(javaClass);
		//
		// Configuration configuration = JsonMapper.buildConfiguration(xmlNamespaceToJsonPrefix);
		// XMLStreamReader xmlStreamReader = JsonMapper.buildStreamReader(json, configuration);
		//
		// if (log.isDebugEnabled()) {
		// String xml = XmlHelper.toXml(xmlStreamReader);
		// log.debug("XML = " + xml);
		// xmlStreamReader = XmlHelper.buildXmlStreamReader(xml);
		// }
		//
		// Object item = XmlHelper.unmarshal(jaxbContext, xmlStreamReader);
		//
		// T typedItem = CastUtils.checkedCast(item, javaClass);
		// return typedItem;
	}
}
