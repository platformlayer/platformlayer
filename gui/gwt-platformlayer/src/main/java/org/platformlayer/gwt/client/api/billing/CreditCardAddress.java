package org.platformlayer.gwt.client.api.billing;

import com.google.gwt.core.client.JavaScriptObject;

public class CreditCardAddress extends JavaScriptObject {

	protected CreditCardAddress() {
	}

	public final native String firstName()
	/*-{ return this.firstName; }-*/;

	public final native void firstName(String firstName)
	/*-{ this.firstName = firstName; }-*/;

	public final native String lastName()
	/*-{ return this.lastName; }-*/;

	public final native void lastName(String lastName)
	/*-{ this.lastName = lastName; }-*/;

	public final native String company()
	/*-{ return this.company; }-*/;

	public final native void company(String company)
	/*-{ this.company = company; }-*/;

	public final native String streetAddress()
	/*-{ return this.streetAddress; }-*/;

	public final native void streetAddress(String streetAddress)
	/*-{ this.streetAddress = streetAddress; }-*/;

	public final native String extendedAddress()
	/*-{ return this.extendedAddress; }-*/;

	public final native void extendedAddress(String extendedAddress)
	/*-{ this.extendedAddress = extendedAddress; }-*/;

	public final native String locality()
	/*-{ return this.locality; }-*/;

	public final native void locality(String locality)
	/*-{ this.locality = locality; }-*/;

	public final native String region()
	/*-{ return this.region; }-*/;

	public final native void region(String region)
	/*-{ this.region = region; }-*/;

	public final native String postalCode()
	/*-{ return this.postalCode; }-*/;

	public final native void postalCode(String postalCode)
	/*-{ this.postalCode = postalCode; }-*/;

	public final native String countryCodeAlpha2()
	/*-{ return this.countryCodeAlpha2; }-*/;

	public final native void countryCodeAlpha2(String countryCodeAlpha2)
	/*-{ this.countryCodeAlpha2 = countryCodeAlpha2; }-*/;

}
