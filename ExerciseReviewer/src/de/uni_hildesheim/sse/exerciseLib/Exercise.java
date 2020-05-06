package de.uni_hildesheim.sse.exerciseLib;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Represents an exercise with assigned reviews.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class Exercise implements Comparable<Exercise> {

    /**
     * Stores the reviews according to the assigned user names.
     * 
     * @since 1.00
     */
    private Map<String, Review> reviews = new HashMap<String, Review>();

    /**
     * Stores the name (identification) of the exercise.
     * 
     * @since 1.00
     */
    private String name;

    /**
     * Stores the maximum credit points for this exercise/task.
     * 
     * @since 1.00
     */
    private double maxCredits;

    /**
     * Creates a new exercise instance.
     * 
     * @param name
     *            the name/identification of this exercise
     * @param maxCredits
     *            the maximum number of credits
     * 
     * @since 1.00
     */
    public Exercise(String name, double maxCredits) {
        this.name = name;
        this.maxCredits = maxCredits;
    }

    /**
     * Returns the name (identification) of this exercise.
     * 
     * @return the name of this exercise
     * 
     * @since 1.00
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the maximum number of credits.
     * 
     * @return the maximum number of credits for this exercise
     * 
     * @since 1.00
     */
    public double getMaxCredits() {
        return maxCredits;
    }

    /**
     * Assigns a given review to this exercise.
     * 
     * @param review
     *            the review to be assigned
     * 
     * @since 1.00
     */
    public void addReview(Review review) {
        reviews.put(review.getUserName(), review);
    }

    /**
     * Returns a review for the specified user name.
     * 
     * @param userName
     *            the user the review should be returned for
     * @return the assigned review (or <b>null</b> in the case that the
     *         review cannot be found)
     * 
     * @since 1.00
     */
    public Review getReview(String userName) {
        return reviews.get(userName);
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     * 
     * @param exercise
     *            the object to be compared.
     * @return a negative integer, zero, or a positive integer as this
     *         object is less than, equal to, or greater than the specified
     *         object.
     * 
     * @throws ClassCastException
     *             if the specified object's type prevents it from being
     *             compared to this object.
     * 
     * @since 2.0
     */
    public int compareTo(Exercise exercise) {
        return name.compareTo(exercise.name);
    }
    
    /**
     * Returns the number of reviews stored for this exercise.
     * 
     * @return the number of reviews
     * 
     * @since 1.08
     */
    public int getReviewCount() {
        return reviews.size();
    }
    
    /**
     * Changes the maximum number of credits.
     * 
     * @param credits the new number of credits
     * 
     * @since 1.08
     */
    public void setMaxCredits(int credits) {
        this.maxCredits = credits;
    }
    
    /**
     * Returns the user-review mappings.
     * 
     * @return the user-review mappings
     * 
     * @since 1.10
     */
    Iterator<Map.Entry<String, Review>> userReviewMappings() {
        return reviews.entrySet().iterator();
    }

}

