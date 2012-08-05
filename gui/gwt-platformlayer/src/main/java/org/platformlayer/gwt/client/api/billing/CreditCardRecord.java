package org.platformlayer.gwt.client.api.billing;

import com.google.gwt.core.client.JavaScriptObject;

public class CreditCardRecord extends JavaScriptObject {
	protected CreditCardRecord() {
	}

	public final native int id()
	/*-{ return this.id; }-*/;

	public final native int projectId()
	/*-{ return this.projectId; }-*/;

	public final native String expiration()
	/*-{ return this.expiration; }-*/;

	public final native String cardType()
	/*-{ return this.cardType; }-*/;

	public final native String lastFour()
	/*-{ return this.lastFour; }-*/;
}
