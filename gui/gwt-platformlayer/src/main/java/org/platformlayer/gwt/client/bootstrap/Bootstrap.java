package org.platformlayer.gwt.client.bootstrap;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.HeadElement;
import com.google.gwt.dom.client.LinkElement;
import com.google.gwt.dom.client.ScriptElement;

public class Bootstrap {
	BootstrapResources resources = GWT.create(BootstrapResources.class);

	static boolean injected = false;

	public static void ensureInjected() {
		if (!injected) {
			// BootstrapResources resources = GWT.create(BootstrapResources.class);

			String base = GWT.getModuleBaseURL();

			HeadElement head = getHead();

			LinkElement link = createLinkElement(base + "bootstrap/less/bootstrap.less");
			head.appendChild(link);

			ScriptElement script = createScriptElement(base + "less/less-1.3.0.min.js");
			head.appendChild(script);

			injected = true;
		}
	}

	private static ScriptElement createScriptElement(String href) {
		ScriptElement script = Document.get().createScriptElement();

		script.setAttribute("type", "text/javascript");
		script.setAttribute("language", "javascript");
		script.setAttribute("src", href);

		return script;
	}

	static LinkElement createLinkElement(String href) {
		LinkElement link = Document.get().createLinkElement();
		link.setAttribute("rel", "stylesheet/less");
		link.setAttribute("href", href);

		return link;
	}

	static HeadElement getHead() {
		Element element = Document.get().getElementsByTagName("head").getItem(0);
		assert element != null : "HTML Head element required";
		HeadElement head = HeadElement.as(element);
		return head;
	}
}
