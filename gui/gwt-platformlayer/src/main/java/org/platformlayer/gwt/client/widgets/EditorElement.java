package org.platformlayer.gwt.client.widgets;

import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.user.client.Element;

abstract class EditorElement {
	public abstract String getValue();

	public abstract void setValue(String value);

	public static EditorElement findEditorElement(Element parent) {
		// TODO: Handle multiple elements??
		NodeList<com.google.gwt.dom.client.Element> inputElements = parent.getElementsByTagName("input");
		if (inputElements.getLength() != 0) {
			assert inputElements.getLength() == 1;
			return toEditor(inputElements.getItem(0));
		}

		NodeList<com.google.gwt.dom.client.Element> selectElements = parent.getElementsByTagName("select");
		if (selectElements.getLength() != 0) {
			assert selectElements.getLength() == 1;
			return toEditor(selectElements.getItem(0));
		}

		assert false;
		return null;
	}

	static class InputEditorElement extends EditorElement {
		final InputElement element;

		public InputEditorElement(InputElement element) {
			super();
			this.element = element;
		}

		@Override
		public String getValue() {
			return element.getValue();
		}

		@Override
		public void setValue(String value) {
			element.setValue(value);
		}
	}

	static class SelectEditorElement extends EditorElement {
		final SelectElement element;

		public SelectEditorElement(SelectElement element) {
			super();
			this.element = element;
		}

		@Override
		public String getValue() {
			return element.getValue();
		}

		@Override
		public void setValue(String value) {
			element.setValue(value);
		}
	}

	private static EditorElement toEditor(com.google.gwt.dom.client.Element element) {
		String tagName = element.getTagName();
		tagName = tagName.toLowerCase();
		if (tagName.equals("input")) {
			// String inputType = element.getAttribute("type");
			InputElement inputElement = element.cast();
			return new InputEditorElement(inputElement);
		}
		if (tagName.equals("select")) {
			// String inputType = element.getAttribute("type");
			SelectElement selectElement = element.cast();
			return new SelectEditorElement(selectElement);
		}
		throw new UnsupportedOperationException();
	}

}