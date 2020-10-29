package com.shallowinggg.palm;

/**
 * @author dingshimin
 */
public class StringUtil {

    private StringUtil() {}

    public static boolean isEmpty(CharSequence s) {
        return s == null || s.length() == 0;
    }

    public static boolean isNotEmpty(CharSequence s) {
        return !isEmpty(s);
    }

    public static boolean isBlank(CharSequence s) {
        if(isNotEmpty(s)) {
            int len = s.length();
            for(int i = 0; i < len; ++i) {
                if(!Character.isWhitespace(s.charAt(i))) {
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean isNotBlank(CharSequence s) {
        return !isBlank(s);
    }

}
