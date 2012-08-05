package org.platformlayer.gwt.client.api.billing;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;

public class CreditCardDetails extends JavaScriptObject {
	protected CreditCardDetails() {
	}

	public final String toJson() {
		String json = new JSONObject(this).toString();
		return json;
	}

	public final native String getCardholderName()
	/*-{ return this.cardholderName; }-*/;

	public final native void setCardholderName(String cardholderName)
	/*-{ this.cardholderName = cardholderName; }-*/;

	public final native String getExpirationMonth()
	/*-{ return this.expirationMonth; }-*/;

	public final native void setExpirationMonth(String expirationMonth)
	/*-{ this.expirationMonth = expirationMonth; }-*/;

	public final native String getExpirationYear()
	/*-{ return this.expirationYear; }-*/;

	public final native void setExpirationYear(String expirationYear)
	/*-{ this.expirationYear = expirationYear; }-*/;

	public final native String getCardNumber()
	/*-{ return this.cardNumber; }-*/;

	public final native void setCardNumber(String cardNumber)
	/*-{ this.cardNumber = cardNumber; }-*/;

	public final native String getCvv()
	/*-{ return this.cvv; }-*/;

	public final native void setCvv(String cvv)
	/*-{ this.cvv = cvv; }-*/;

	public final native CreditCardAddress getBillingAddress()
	/*-{ return this.billingAddress; }-*/;

	public final native void cvv(CreditCardAddress setBillingAddress)
	/*-{ this.billingAddress = billingAddress; }-*/;

}
