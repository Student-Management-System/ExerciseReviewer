package de.uni_hildesheim.sse.exerciseLib;

/**
 * Extends the submitter communication exception in order to 
 * provide more relevant data to the more experienced user
 * of the reviewer plugin.
 * 
 * @author Holger Eichelberger
 * @since 2.00
 * @version 2.00
 */
public class ReviewException extends 
    de.uni_hildesheim.sse.exerciseSubmitter.submission.CommunicationException {

    /**
     * Stores the version identifier for serialization.
     * 
     * @since 2.00
     */
    private static final long serialVersionUID = -1641914202116917858L;

    /**
     * Stores additional data to be provided to the user.
     * 
     * @since 2.00
     */
    private Object attachment = null;

    /**
     * Creates a new communication exception.
     * 
     * @param publicMessage the message instance
     *        to be provided to the user
     * @param throwable the throwable which is 
     *        responsible for the reason of this
     *        exception
     * 
     * @since 2.00
     */
    public ReviewException(PublicMessage publicMessage, 
        Throwable throwable) {
        super(publicMessage, throwable);
    }
    
    /**
     * Creates a new communication exception.
     * 
     * @param publicMessage the message instance
     *        to be provided to the user
     * @param throwable the throwable which is 
     *        responsible for the reason of this
     *        exception
     * @param attachment an additional attachment
     *        to provide more data on the related
     *        error to the experienced user
     * 
     * @since 2.00
     */
    public ReviewException(PublicMessage publicMessage, 
        Throwable throwable, Object attachment) {
        super(publicMessage, throwable);
        this.attachment = attachment;
    }
    
    /**
     * Returns the message of this exception, in this case
     * the text of the message to be made available to
     * the user.
     * 
     * @return the textual description
     * 
     * @since 2.00
     */
    public String getMessage() {
        String result = super.getMessage();
        boolean added = false;
        if (null != attachment) {
            result += " (" + attachment + ")";
            added = true;
        }
        if (null != getCause() && null != getCause().getMessage()) {
            result += " :" + getCause().getMessage();
            added = true;
        }
        if (!added) {
            result += ".";
        }
        return result;
    }

}
