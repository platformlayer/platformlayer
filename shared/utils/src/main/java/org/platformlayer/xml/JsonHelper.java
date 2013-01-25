package org.platformlayer.xml;

import java.io.IOException;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.google.common.collect.Maps;

public class JsonHelper<T> {
	// TODO: Figure out why Jettison doesn't work
	public boolean JETTISON_IS_FUBAR = true;

	final HashMap<String, String> xmlNamespaceToJsonPrefix = Maps.newHashMap();

	final Class<T> clazz;

	public JsonHelper(Class<T> clazz) {
		super();
		this.clazz = clazz;
	}

	public static <T> JsonHelper<T> build(Class<T> clazz) {
		return new JsonHelper<T>(clazz);
	}

	public void addNamespaceMapping(String xmlNamespace, String jsonPrefix) {
		xmlNamespaceToJsonPrefix.put(xmlNamespace, jsonPrefix);
	}

	// public static Configuration buildConfiguration(String defaultNamespace) {
	// final HashMap<String, String> xmlNamespaceToJsonPrefix = Maps.newHashMap();
	// if (defaultNamespace != null) {
	// xmlNamespaceToJsonPrefix.put(defaultNamespace, "");
	// }
	//
	// return buildConfiguration(xmlNamespaceToJsonPrefix);
	// }
	//
	// public static Configuration buildConfiguration(Map<String, String> xmlNamespaceToJsonPrefix) {
	// Configuration configuration = new Configuration(xmlNamespaceToJsonPrefix);
	// return configuration;
	// }
	//
	// public static XMLStreamWriter buildStreamWriter(Writer writer, Configuration configuration)
	// throws XMLStreamException {
	// MappedXMLOutputFactory xof = new MappedXMLOutputFactory(configuration);
	// XMLStreamWriter xmlStreamWriter = xof.createXMLStreamWriter(writer);
	// return xmlStreamWriter;
	// }
	//
	// public static XMLStreamReader buildStreamReader(String json, Configuration configuration) throws
	// XMLStreamException {
	// MappedXMLInputFactory xif = new MappedXMLInputFactory(configuration);
	// XMLStreamReader xmlStreamReader = xif.createXMLStreamReader(new StringReader(json));
	// return xmlStreamReader;
	// }
	//
	// public static XMLStreamReader buildStreamReader(InputStream json, Configuration configuration)
	// throws XMLStreamException {
	// MappedXMLInputFactory xif = new MappedXMLInputFactory(configuration);
	// XMLStreamReader xmlStreamReader = xif.createXMLStreamReader(json);
	// return xmlStreamReader;
	// }
	//
	// public static Object unmarshal(XMLStreamReader xmlStreamReader, JaxbHelper jaxbHelper) throws JAXBException {
	// // if (JETTISON_IS_FUBAR) {
	// // // Jettison is screwed... it doesn't work unless we build a new stream reader from the xml text.
	// // // This is, of course, complete and utter nonsense
	// // String xml = XmlHelper.toXml(xmlStreamReader);
	// // xmlStreamReader = XmlHelper.buildXmlStreamReader(xml);
	// // }
	//
	// Object obj = jaxbHelper.unmarshal(xmlStreamReader);
	// return obj;
	// }
	//
	// public T unmarshal(String json) throws XMLStreamException, JAXBException, TransformerException, JSONException {
	// Configuration configuration = new Configuration(xmlNamespaceToJsonPrefix);
	//
	// XMLStreamReader xmlStreamReader = buildStreamReader(json, configuration);
	//
	// return unmarshal(xmlStreamReader);
	// }
	//
	// public T unmarshal(InputStream inputStream) throws XMLStreamException, JAXBException, TransformerException,
	// JSONException {
	// Configuration configuration = new Configuration(xmlNamespaceToJsonPrefix);
	//
	// XMLStreamReader xmlStreamReader = buildStreamReader(inputStream, configuration);
	//
	// return unmarshal(xmlStreamReader);
	// }
	//
	// private T unmarshal(XMLStreamReader xmlStreamReader) throws JAXBException, XMLStreamException,
	// TransformerException {
	// JaxbHelper jaxbHelper = JaxbHelper.get(clazz);
	// Object obj = unmarshal(xmlStreamReader, jaxbHelper);
	// return CastUtils.checkedCast(obj, clazz);
	// }
	//
	// public String marshal(T object, boolean formatted) throws XMLStreamException, JAXBException,
	// TransformerException,
	// JSONException {
	// Configuration writeConfig = new Configuration(xmlNamespaceToJsonPrefix);
	// MappedXMLOutputFactory xof = new MappedXMLOutputFactory(writeConfig);
	//
	// StringWriter stringWriter = new StringWriter();
	//
	// JaxbHelper jaxbHelper = JaxbHelper.get(clazz);
	// XMLStreamWriter xmlStreamWriter = xof.createXMLStreamWriter(stringWriter);
	// jaxbHelper.marshal(object, formatted, xmlStreamWriter);
	//
	// return stringWriter.toString();
	// }
	//

	public String marshal(T object, boolean formatted) throws JsonProcessingException, IOException {
		ObjectMapper mapper = JsonHelper.buildObjectMapper(null, formatted);

		return mapper.writeValueAsString(object);
	}

	// public void marshal(T object, boolean formatted, OutputStream outputStream) throws XMLStreamException,
	// JAXBException, TransformerException, JSONException {
	// Configuration writeConfig = new Configuration(xmlNamespaceToJsonPrefix);
	// MappedXMLOutputFactory xof = new MappedXMLOutputFactory(writeConfig);
	//
	// XMLStreamWriter xmlStreamWriter = xof.createXMLStreamWriter(outputStream);
	//
	// JaxbHelper jaxbHelper = JaxbHelper.get(clazz);
	// jaxbHelper.marshal(object, formatted, xmlStreamWriter);
	// }
	//
	// public String toStringHelper(T object) {
	// String json;
	// try {
	// json = marshal(object, true);
	// } catch (Exception e) {
	// return "<Errror: " + e.toString() + ">";
	// }
	// return json;
	// }

	public void addDefaultNamespace() {
		JaxbHelper jaxbHelper = JaxbHelper.get(clazz);
		addNamespaceMapping(jaxbHelper.getPrimaryNamespace(), "");
	}

	/**
	 * Accepts a 'bare' JSON format, and wraps in the appropriate outer element
	 */
	public String wrapJson(String json) {
		String xmlElementName = JaxbHelper.getXmlElementName(clazz);
		String jsonClassName = xmlElementName; // Character.toLowerCase(jsonClassName.charAt(0)) +
												// jsonClassName.substring(1);

		if (!json.startsWith("{")) {
			json = "{ " + json + " }";
		}
		json = "{\"" + jsonClassName + "\": " + json + " }";

		return json;
	}

	public static ObjectMapper buildObjectMapper(TypeFactory typeFactory, boolean formatted) {
		ObjectMapper mapper = new ObjectMapper();

		if (typeFactory == null) {
			typeFactory = TypeFactory.defaultInstance();
		}

		if (formatted) {
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
		}

		// Favor JAXB annotations
		AnnotationIntrospector jaxbIntrospector = new JaxbAnnotationIntrospector(typeFactory);
		AnnotationIntrospector jacksonIntrospector = new JacksonAnnotationIntrospector();
		AnnotationIntrospector introspector = new AnnotationIntrospectorPair(jaxbIntrospector, jacksonIntrospector);
		mapper.setAnnotationIntrospector(introspector);

		return mapper;

	}
}
