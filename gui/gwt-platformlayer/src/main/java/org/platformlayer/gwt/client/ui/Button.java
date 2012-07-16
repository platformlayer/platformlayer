package org.platformlayer.gwt.client.ui;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.ButtonBase;

public class Button extends ButtonBase {

	public Button() {
		super(Document.get().createPushButtonElement());
		setStyleName("gwt-Button");
	}

}
