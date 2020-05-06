package de.uni_hildesheim.sse.exerciseReviewer.core;

import de.uni_hildesheim.sse.exerciseSubmitter.submission.
    CommunicationException.PublicMessage;

/**
 * Defines specific public messages for the reviewer plugin.
 * 
 * @author Holger Eichelberger
 * @since 1.11
 * @version 1.11
 */
public enum ReviewPublicMessage implements PublicMessage {

    /**
     * Denotes the case that there is a problem with the 
     * data structures on the review server.
     * 
     * @since 1.11
     */
    INVALID_REVIEW_DATASTRUCTURE("Cannot handle the request "
        + "due to problems with the review server "
        + "datastructures"),

    /**
     * Denotes the case that there is a problem with the 
     * data structures on the review server - exercise not found.
     * 
     * @since 1.11
     */
    INVALID_REVIEW_DATASTRUCTURE_NO_EXERCISE("Cannot find exercise" 
        + " information. (e.g. check .tsv file)"),

    /**
     * Denotes the case that there is a problem with the 
     * data structures on the review server - user not found.
     * 
     * @since 1.11
     */
    INVALID_REVIEW_DATASTRUCTURE_NO_USER("Cannot find user information. "
        + "(e.g. check .users file)"),
        
    /**
     * Denotes the case that there is a problem with the 
     * data structures on the review server.
     * 
     * @since 1.11
     */
    INVALID_SYNTAX("Error in the review file syntax"),
        
    /**
     * Denotes the case that there is a problem with the 
     * data structures on the review server, in particular,
     * that the a user was not found.
     * 
     * @since 1.11
     */
    NO_USER_FOUND("While working on review "
        + "server data structures a given user cannot be retrieved. "
        + "May be that you mixed up a real users data set with a "
        + "(default) users data set");
    
    /**
     * Stores the message assigned to this message constant.
     * 
     * @since 1.11
     */
    private String message;

    /**
     * Creates a new public message constant.
     * 
     * @param message the message text
     * 
     * @since 1.11
     */
    private ReviewPublicMessage(String message) {
        this.message = message;
    }

    /**
     * Returns the message text.
     * 
     * @return the message text of this error message
     * 
     * @since 1.11
     */
    public String getMessage() {
        return message;
    }

}
