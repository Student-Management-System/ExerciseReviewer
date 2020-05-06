package de.uni_hildesheim.sse.exerciseLib;

/**
 * Represents an individual (read-only) review.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class Review {
    
    /**
     * Stores the review text.
     * 
     * @since 1.00
     */
    private String review;

    /**
     * Stores the assigned credits.
     * 
     * @since 1.00
     */
    private double credits;
    
    /**
     * Stores the user name of the user
     * who submitted the assigned exercise/task.
     * 
     * @since 1.00
     */
    private String userName;
    
    /**
     * Stores, if this review was submitted to
     * a server after the last modification.
     * 
     * @since 1.00
     */
    private boolean isSubmittedToServer;
    
    /**
     * Creates a new review object.
     * 
     * @param userName the user
     *        who submitted the assigned exercise/task
     * @param credits the assigned credits
     * @param review the review text
     * 
     * @since 1.00
     */
    public Review(String userName, double credits, String review) {
        this.userName = userName;
        this.review = review;
        this.credits = credits;
    }

    /**
     * Returns the assigned credits.
     * 
     * @return the assigned credits
     * 
     * @since 1.00
     */
    public double getCredits() {
        return credits;
    }

    /**
     * Returns the review text.
     * 
     * @return the review text
     * 
     * @since 1.00
     */
    public String getReview() {
        return review;
    }

    /**
     * Returns the user name of the user(s) who
     * submitted the assigned exercise.
     * 
     * @return the user name
     * 
     * @since 1.00
     */
    public String getUserName() {
        return userName;
    }
    
    /**
     * Notifies this review that it is submitted to
     * a server. Note, that an appropriate method in
     * {@link ReviewCommunication} like 
     * {@link ReviewCommunication#submitReview(String, Review)}
     * must be called in order to store the change.
     * 
     * @since 1.00
     */
    public void setSubmittedToServer() {
        isSubmittedToServer = true;
    }

    /**
     * Returns if this review instance was submitted
     * to a server.
     * 
     * @return <code>true</code> if the instance was
     *    submitted to a server, <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean isSubmittedToServer() {
        return isSubmittedToServer;
    }

}
