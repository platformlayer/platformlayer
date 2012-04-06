package org.platformlayer.xml;

import com.google.common.base.Strings;

public class StringUtils {

    public static String uncapitalize(String v) {
        if (Strings.isNullOrEmpty(v)) {
            return v;
        }
        return Character.toLowerCase(v.charAt(0)) + v.substring(1);
    }

}
