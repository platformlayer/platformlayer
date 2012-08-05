package org.platformlayer.gwt.client.widgets;

import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.user.client.ui.HTMLPanel;

public class ToEditor extends HTMLPanel implements LeafValueEditor<String> {

	EditorElement editorElement;

	public ToEditor(String html) {
		super(html);
	}

	private EditorElement getEditorElement() {
		if (editorElement == null) {
			editorElement = EditorElement.findEditorElement(getElement());
			assert editorElement != null;
		}
		return editorElement;
	}

	@Override
	public void setValue(String value) {
		getEditorElement().setValue(value);
	}

	@Override
	public String getValue() {
		return getEditorElement().getValue();
	}

}
