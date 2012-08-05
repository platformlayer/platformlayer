package org.platformlayer.gwt.client.widgets;

import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.user.client.ui.HTMLPanel;

public class ControlGroup extends HTMLPanel implements LeafValueEditor<String> {

	EditorElement editorElement;

	public ControlGroup(String html) {
		super(html);

		setStyleName("control-group");
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
