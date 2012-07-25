package org.platformlayer.gwt.client.item;

import org.platformlayer.gwt.client.api.platformlayer.Tag;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class TagCell extends AbstractCell<Tag> {
	public static interface CellTemplates extends SafeHtmlTemplates {
		public static final CellTemplates INSTANCE = GWT.create(CellTemplates.class);

		@Template("<span>{0}</span><span>{1}</span>")
		SafeHtml line(String key, String value);
	}

	@Override
	public void render(Context context, Tag value, SafeHtmlBuilder builder) {
		builder.append(CellTemplates.INSTANCE.line(value.getKey(), value.getValue()));
	}
}