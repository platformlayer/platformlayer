package org.platformlayer.gwt.client.widgets;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Node;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FlowPanel;

public class Repeater<T> extends FlowPanel {
	final Cell<T> cell;

	private final DivElement tmpElem = Document.get().createDivElement();

	public Repeater(Cell<T> cell) {
		this.cell = cell;
	}

	int count;

	public void replaceAllChildren(Iterable<T> items) {
		// Is this the best way to clear??
		getElement().setInnerHTML("");

		count = 0;

		appendChildren(items);
	}

	public void appendChildren(Iterable<T> items) {
		// No idea if this is faster than e.g. building up one at a time!
		SafeHtmlBuilder html = new SafeHtmlBuilder();
		int i = count;
		for (T item : items) {
			html.appendHtmlConstant("<div __idx=\"" + i + "\">");

			// SafeHtmlBuilder cellBuilder = new SafeHtmlBuilder();
			Context context = new Context(i, 0, item);
			cell.render(context, item, html);

			html.appendHtmlConstant("</div>");

			i++;
			count++;
		}

		tmpElem.setInnerSafeHtml(html.toSafeHtml());

		Element myElement = getElement();

		while (true) {
			Node nextTempChild = tmpElem.getFirstChild();
			if (nextTempChild == null) {
				break;
			}
			myElement.appendChild(nextTempChild);
		}

	}
}
