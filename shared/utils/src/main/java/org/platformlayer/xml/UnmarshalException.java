package org.platformlayer.xml;

public class UnmarshalException extends Exception {
    private static final long serialVersionUID = 1L;

    private String xml;

    public UnmarshalException(String message, Exception e, String xml) {
        super(message, e);
        this.xml = xml;
    }

    @Override
    public String getMessage() {
        String message = super.getMessage();
        if (xml != null) {
            message += "\nXML: " + xml;
        }
        return message;
    }

}
