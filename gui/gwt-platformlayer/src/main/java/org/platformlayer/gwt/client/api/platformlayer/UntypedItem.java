package org.platformlayer.gwt.client.api.platformlayer;

import java.util.List;

import org.platformlayer.gwt.client.commons.JsArrayToList;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class UntypedItem extends JavaScriptObject {
	protected UntypedItem() {
	}

	public final native JsArray<Tag> getTags0()
	/*-{ return this.tags; }-*/;

	public final List<Tag> getTags() {
		return JsArrayToList.wrap(getTags0());
	}

	// TODO: JSON looks like: key : { value: abc } Remove the value!
	public final native String getKey()
	/*-{ return this.key ? this.key.value : null; }-*/;

	// TODO: Actually a ManagedItemState
	public final native String getState()
	/*-{ return this.state; }-*/;

	// private final Element root;
	// private Tags tags;
	//
	// private PlatformLayerKey platformLayerKey;

	// public ElementInfo getRootElementInfo() {
	// String name = null;
	// String namespace = null;
	//
	// // xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ns16:networkConnection"
	// String xsiNs = "http://www.w3.org/2001/XMLSchema-instance";
	// String xsiType = root.getAttributeNS(xsiNs, "type");
	// if (xsiType != null) {
	// // String xsiType = xsiTypeNode.getValue();
	// String[] tokens = xsiType.split(":");
	// if (tokens.length == 1) {
	// namespace = null;
	// name = tokens[0];
	// } else if (tokens.length == 2) {
	// name = tokens[1];
	// namespace = mapNamespace(tokens[0]);
	// } else {
	// throw new IllegalStateException();
	// }
	// }
	//
	// if (Strings.isNullOrEmpty(name)) {
	// name = root.getLocalName();
	//
	// if (Strings.isNullOrEmpty(name)) {
	// name = null;
	// }
	// }
	//
	// if (Strings.isNullOrEmpty(namespace)) {
	// namespace = root.getNamespaceURI();
	//
	// if (Strings.isNullOrEmpty(namespace)) {
	// namespace = null;
	// }
	// }
	//
	// return new ElementInfo(namespace, name);
	// }

	// private String mapNamespace(String alias) {
	// String ns = "xmlns";
	// Element rootElement = root.getOwnerDocument().getDocumentElement();
	// String attributeValue = rootElement.getAttribute(ns + ":" + alias);
	// if (attributeValue != null) {
	// return attributeValue;
	// } else {
	// throw new IllegalArgumentException();
	// }
	// }

	// public PlatformLayerKey getPlatformLayerKey() {
	// if (platformLayerKey == null) {
	// Node element = findKeyElement(false);
	// if (element != null) {
	// platformLayerKey = PlatformLayerKey.parse(element.getTextContent());
	// }
	// }
	// return platformLayerKey;
	// }

}
