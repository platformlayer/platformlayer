package org.platformlayer.gwt.client;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;

public interface AppTemplates extends SafeHtmlTemplates {
	public static final AppTemplates INSTANCE = GWT.create(AppTemplates.class);

	@Template("<i class=\"{0}\"></i>")
	SafeHtml icon(String css);
}
