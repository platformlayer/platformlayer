package org.platformlayer.gwt.client.api.billing;

import com.google.gwt.core.client.JavaScriptObject;

public class ProjectBillingInfo extends JavaScriptObject {
	protected ProjectBillingInfo() {
	}

	public final native int id()
	/*-{ return this.id; }-*/;

	public final native String key()
	/*-{ return this.key; }-*/;

	public final native float balance()
	/*-{ return this.balance; }-*/;

	// @Column(name = "email_status")
	// public EmailStatus emailStatus;

	public final native String emailStatus()
	/*-{ return this.emailStatus; }-*/;

	public final native String cardStatus()
	/*-{ return this.cardStatus; }-*/;
}
