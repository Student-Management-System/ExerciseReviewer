package de.uni_hildesheim.sse.exerciseLib;

import de.uni_hildesheim.sse.exerciseReviewer.core.ReviewCommunication;
import net.ssehub.exercisesubmitter.protocol.frontend.Assessment;

/**
 * Represents an individual (read-only) review.
 * 
 * @author El-Sharkawy
 * @author Holger Eichelberger
 * @since 1.00
 * @version 2.00
 */
public class Review {
    
    /**
     * Stores the review text.
     * 
     * @since 2.00
     */
    private Assessment review;

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
     * @param review the review text (sumitter name, review comment(s), and points)
     * 
     * @since 1.00
     */
    public Review(Assessment review) {
        this.review = review;
    }

    /**
     * Returns the assigned credits.
     * 
     * @return the assigned credits
     * 
     * @since 1.00
     */
    public double getCredits() {
        return review.getAchievedPoints();
    }

    /**
     * Returns the review text.
     * 
     * @return the review text
     * 
     * @since 1.00
     */
    public String getReview() {
        return review.getFullReviewComment() != null ? review.getFullReviewComment() : "";
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
        return review.getSubmitterName();
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
    
    /**
     * Returns the assessment to be submitted to the server.
     * @return The assessment to be submitted to the server.
     */
    public Assessment getAssessment() {
        return review;
    }

}
