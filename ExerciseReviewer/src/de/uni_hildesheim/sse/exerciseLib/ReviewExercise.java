package de.uni_hildesheim.sse.exerciseLib;

/**
 * Stores the combination of a review and an exercise [convenience class].
 * 
 * @author Holger Eichelberger
 * @since 1.10
 * @version 1.10
 */
public class ReviewExercise implements Comparable<ReviewExercise> {
    
    /**
     * Stores an exercise.
     * 
     * @since 1.10
     */
    private Exercise exercise;
    
    /**
     * Stores the related review.
     * 
     * @since 1.10
     */
    private Review review;
    
    /**
     * Creates a new instance.
     * 
     * @param exercise the exercise to be considered
     * @param review the related review
     * 
     * @since 1.00
     */
    public ReviewExercise(Exercise exercise, Review review) {
        this.exercise = exercise;
        this.review = review;
    }
    
    /**
     * Returns the name (identification) of this exercise.
     * 
     * @return the name of this exercise
     * 
     * @since 1.00
     */
    public String getName() {
        return exercise.getName();
    }
    
    /**
     * Returns the maximum number of credits.
     * 
     * @return the maximum number of credits for this exercise
     * 
     * @since 1.00
     */
    public double getMaxCredits() {
        return exercise.getMaxCredits();
    }
    
    /**
     * Returns the assigned credits.
     * 
     * @return the assigned credits
     * 
     * @since 1.00
     */
    public double getCredits() {
        return review.getCredits();
    }
    
    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     * 
     * @param review the object to be compared.
     * @return a negative integer, zero, or a positive integer as this
     *         object is less than, equal to, or greater than the specified
     *         object.
     * 
     * @since 1.10
     */
    public int compareTo(ReviewExercise review) {
        return getName().compareTo(review.getName());
    }
    
};
