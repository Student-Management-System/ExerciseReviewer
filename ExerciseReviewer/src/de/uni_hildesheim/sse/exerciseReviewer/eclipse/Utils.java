package de.uni_hildesheim.sse.exerciseReviewer.eclipse;

/**
 * General utilities.
 * 
 * @author Holger Eichelberger
 * @since 1.08
 * @version 1.08
 */
public class Utils {

    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.08
     */
    private Utils() {
    }
    
    /**
     * Eliminates <b>null</b> by replacing it with the empty string.
     * 
     * @param text the text to be considered
     * @return the empty string or <code>text</code>
     * 
     * @since 1.00
     */
    public static String eliminateNull(String text) {
        String result = "";
        if (null != text) {
            result = text;
        } 
        return result;
    }

}
