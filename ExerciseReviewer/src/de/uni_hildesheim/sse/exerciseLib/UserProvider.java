package de.uni_hildesheim.sse.exerciseLib;

import java.util.List;

import de.uni_hildesheim.sse.exerciseSubmitter.submission.
    CommunicationException;

/**
 * Describes an instance providing data on users to the exercise data 
 * implementation.
 * 
 * @author Holger Eichelberger
 * @since 1.10
 * @version 1.10
 */
public interface UserProvider {

    /**
     * Returns all users known and assigned to the related course.
     * 
     * @return all known and assigned users
     *
     * @throws CommunicationException if any error occurs
     * 
     * @since 1.10
     */
    public List<RealUser> getAllKnownUsers() 
        throws CommunicationException;
    
    /**
     * Returns the (repository) user having a given name.
     * 
     * @param name the name of the user
     * @return the user object or <b>null</b> if not found
     * 
     * @since 1.10
     */
    public User getSubmissionUser(String name);
    
    /**
     * Returns all (repository) users.
     * 
     * @return all repository users
     * 
     * @since 1.10
     */
    public Iterable<User> submissionUsers();
    
}
