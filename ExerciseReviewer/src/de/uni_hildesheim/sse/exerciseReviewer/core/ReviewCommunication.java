package de.uni_hildesheim.sse.exerciseReviewer.core;

import java.util.List;

import de.uni_hildesheim.sse.exerciseLib.RealUser;
import de.uni_hildesheim.sse.exerciseLib.Review;
import de.uni_hildesheim.sse.exerciseLib.ReviewException;
import de.uni_hildesheim.sse.exerciseSubmitter.configuration.IConfiguration;
import de.uni_hildesheim.sse.exerciseSubmitter.submission.CommunicationException;
import de.uni_hildesheim.sse.exerciseSubmitter.submission.CommunicationInstanceListener;
import de.uni_hildesheim.sse.exerciseSubmitter.submission.EmptySubmissionInstanceListener;
import net.ssehub.exercisesubmitter.protocol.frontend.Assignment;

/**
 * Defines the interface of a review communication instance. Instances of
 * subclasses are used to describe concrete mechanisms for providing and storing
 * data on exercise reviews.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 2.1
 */
public abstract class ReviewCommunication {

    /**
     * Stores all available review instances.
     * 
     * @since 1.00
     */
    private static ReviewCommunication reviewInstance;
    
    /**
     * Stores the name of the user communicating with the server.
     * 
     * @since 1.00
     */
    private String username;

    /**
     * Stores the password of the user communicating with the server.
     * 
     * @since 1.00
     */
    private String password;

    /**
     * Creates a new review communication instance. To be called by an
     * appropriate {@link ReviewPlugin} instance.
     * 
     * @param username
     *            the name of the user which will communicate with a concrete
     *            review server/mechanism
     * @param password
     *            the password of <code>username</code>
     * 
     * @since 1.0
     */
    protected ReviewCommunication(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Returns the real names of the users mapped to the given user name.
     * 
     * @param userName
     *            a user name (possibly) mapped to multiple real users
     * @return the real user names to <code>userName</code>,
     *         <code>userName</code> if no mapping was found
     * @throws CommunicationException
     *             if any communication error occurs
     * 
     * @since 1.00
     */
    public abstract List<String> getRealUsers(String userName) throws CommunicationException;

    /**
     * Submits a review.
     * 
     * @param task
     *            the task identifier describing the reviewed task/exercise
     * @param review the review to be submitted
     * @throws CommunicationException
     *             if any communication/validation error occurs
     * 
     * @since 1.00
     */
    public abstract void submitReview(Assignment task, Review review) throws CommunicationException;

    /**
     * Returns the specified review for the given task.
     * 
     * @param task
     *            the task identifier describing the reviewed task/exercise
     * @param userName
     *            the user name uniquely identifying an individual user or a
     *            user group (see {@link #getRealUsers(String)}
     * @return the stored review (or <b>null</b>)
     * @throws CommunicationException
     *             if any communication/validation error occurs
     * 
     * @since 1.00
     */
    public abstract Review getReview(String task, String userName) throws CommunicationException;
    
    /**
     * Returns the concrete review communication instance available for the
     * specified user. On the first call, {@link #reviewInstance} will be
     * initialized according to the existing plugins 
     * ({@link #authenticateUser()}).
     * 
     * @param configuration
     *            the local user configuration
     * @param listener
     *            an optional listener for the notification on the progress of
     *            the execution
     * @return the review communication instance
     * @throws CommunicationException
     *             thrown if any error occurs
     * 
     * @since 1.00
     */
    public static final ReviewCommunication getInstance(IConfiguration configuration,
        CommunicationInstanceListener listener) throws CommunicationException {
        
        return getInstance(configuration.getUserName(), configuration.getPassword(), listener);
    }

    /**
     * Returns the concrete review communication instance available for the
     * specified user. On the first call, {@link #reviewInstance} will be
     * initialized according to the existing plugins 
     * ({@link #authenticateUser()}).
     * 
     * @param userName
     *            the user name of the user to be connected to the server(s)
     * @param password
     *            the password of <code>userName</code>
     * @param listener
     *            an optional listener for the notification on the progress of
     *            the execution
     * @return the review communication instance
     * @throws CommunicationException
     *             thrown if any error occurs
     * 
     * @since 1.00
     */
    public static final ReviewCommunication getInstance(String userName, String password,
        CommunicationInstanceListener listener) throws CommunicationException {

        // use the dummy listener if none is provided
        if (null == listener) {
            listener = new EmptySubmissionInstanceListener();
        }

        // do an update if instances are present
        if (null != reviewInstance) {
            return reviewInstance;
        }

        // otherwise read configuration and do plugin instantiation
        listener.notifyContactingStarted();
        listener.notifyNumberOfServers(3);
        try {
            int step = 1;
            String protocol = IConfiguration.INSTANCE.getProperty("review", "");
            listener.doStep("Contacting review mechanism", step++);
            ReviewCommunication comm = null;
            for (ReviewPlugin plugin : ReviewPlugin.getPlugins()) {
                if (plugin.getProtocol().equalsIgnoreCase(protocol)) {
                    comm = plugin.createInstance(userName, password);
                }
            }
            if (null == comm) {
                throw new ReviewException(CommunicationException.SubmissionPublicMessage.
                    PLUGIN_NOT_HANDLED, new Throwable());
            } else {
                listener.doStep("Validating user data for review", step++);
                if (!comm.authenticateUser()) {
                    throw new ReviewException(CommunicationException.SubmissionPublicMessage.
                        AUTHENTICATION_ERROR, new Throwable());
                }
                reviewInstance = comm;
            }
            listener.doStep("Finished contacting review mechanism", step++);
            listener.notifyContactingFinished(false);
        } catch (CommunicationException exception) {
            reviewInstance = null;
            listener.notifyContactingFinished(true);
            throw exception;
        }
        return reviewInstance;
    }

    /**
     * Returns the password required for the communication with the review
     * collection mechanism.
     * 
     * @return the password
     * 
     * @since 1.00
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns the user name required for the communication with the review
     * collection mechanism.
     * 
     * @return the user name
     * 
     * @since 1.00
     */
    public String getUsername() {
        return username;
    }

    /**
     * Authenticates the user by his/her stored data.
     * 
     * @return <code>true</code>, if the user was authenticated,
     *         <code>false</code> else
     * 
     * @since 1.00
     */
    public abstract boolean authenticateUser();

    /**
     * Returns the maximum number of credits for the specified task/exercise.
     * 
     * @param task
     *            the task/exercise to return the number of credits for
     * @return the maximum number of credits, to be ignored if <code>-1</code>
     * 
     * @throws CommunicationException if any error occurs
     * 
     * @since 1.00
     */
    public abstract double getMaximumCredits(String task) throws CommunicationException;

    /**
     * Returns all users known and assigned to the related course.
     * 
     * @return all known and assigned users
     *
     * @throws CommunicationException if any error occurs
     * 
     * @since 1.08
     */
    public abstract List<RealUser> getAllKnownUsers() 
        throws CommunicationException;

    /**
     * Returns all known tasks.
     * 
     * @return all known tasks
     * 
     * @throws CommunicationException if any error occurs
     * 
     * @since 1.08
     */
    public abstract List<String> getAllKnownTasks() throws CommunicationException;
    
    /**
     * Returns arbitrary descriptive information on the users.
     * 
     * @return arbitrary descriptive information
     * 
     * @since 1.08
     */
    public abstract String getUserInstanceInformation();

    /**
     * Returns arbitrary descriptive information on the reviews.
     * 
     * @return arbitrary descriptive information
     * 
     * @since 1.08
     */
    public abstract String getReviewInstanceInformation();

    /**
     * Reloads the user data.
     * 
     * @throws CommunicationException if any error occurred
     * 
     * @since 1.08
     */
    public abstract void reloadUsers() throws CommunicationException;

    /**
     * Reloads the reviews.
     * 
     * @throws CommunicationException if any error occurred
     * 
     * @since 1.08
     */
    public abstract void reloadReviews() throws CommunicationException;

    /**
     * Returns the assigned default directory.
     * 
     * @return the assigned default directory, may be empty or <b>null</b>
     * 
     * @since 1.08
     */
    public abstract String getDefaultDirectory();

    /**
     * Returns if this communication instance accepts a complete users
     * specification as file.
     * 
     * @return <code>true</code> if it accepts, <code>false</code> else
     * 
     * @since 1.08
     */
    public abstract boolean acceptsUsersAsFile();
    
    /**
     * Loads the users from a file. This method should be called only if
     * {@link #acceptsUsersAsFile()} returns <code>true</code>.
     * 
     * @param file the file to be loaded
     * @throws CommunicationException if any error occurs
     * 
     * @since 1.08
     */
    public abstract void loadUsers(String file) throws CommunicationException;

    /**
     * Returns if this communication instance accepts a complete result
     * specification as file.
     * 
     * @return <code>true</code> if it accepts, <code>false</code> else
     * 
     * @since 1.08
     */
    public abstract boolean acceptsResultsAsFile();

    /**
     * Loads the results from a file. This method should be called only if
     * {@link #acceptsResultsAsFile()} returns <code>true</code>.
     * 
     * @param file the file to be loaded
     * @throws CommunicationException if any error occurs
     * 
     * @since 1.08
     */
    public abstract void loadResults(String file) throws CommunicationException;
    
    /**
     * Returns the number of reviews for the given <code>task</code>.
     * 
     * @param task the task the number of reviews should be returned for
     * @return the number of reviews
     * @throws CommunicationException if any error occurs
     * 
     * @since 1.08
     */
    public abstract int getReviewCount(String task) throws CommunicationException;
    
    /**
     * Deletes a given user. This method should be called only if
     * {@link #acceptsUserModification()} returns <code>true</code>.
     * 
     * @param user the user to be deleted
     * @return <code>true</code> if the user was found and deleted, 
     *         <code>false</code> if the user was not found
     * @throws CommunicationException if any (wrapped) exception occurred
     * 
     * @since 1.08
     */
    public abstract boolean deleteUser(RealUser user) throws CommunicationException;

    /**
     * Returns if general user modifications are permitted.
     * 
     * @return <code>true</code> if general user modifications are permitted,
     *         <code>false</code> else
     * 
     * @since 1.08
     */
    public abstract boolean acceptsUserModification();

    /**
     * Returns if general task modifications are permitted.
     * 
     * @return <code>true</code> if general user modifications are permitted,
     *         <code>false</code> else
     * 
     * @since 1.08
     */
    public abstract boolean acceptsTaskModification();

    /**
     * Returns if task information can be merged.
     * 
     * @return <code>true</code> if task merges are permitted, <code>false</code> else
     * 
     * @since 1.08
     */
    public abstract boolean acceptsTaskMerge();
    
    /**
     * Modifies, i.e. adds a new or modifies an existing user. This method 
     * should be called only if
     * {@link #acceptsUserModification()} returns <code>true</code>.
     * 
     * @param user the user to be modified
     * @throws CommunicationException if any (wrapped) exception occurred
     * 
     * @since 1.08
     */
    public abstract void modifyUser(RealUser user) throws CommunicationException;
    
    /**
     * Deletes an existing task. This method should be called only if
     * {@link #acceptsTaskModification()} returns <code>true</code>.
     * 
     * @param task the task to be deleted
     * @return <code>true</code> if the task was deleted, 
     *         <code>false</code> else
     * @throws CommunicationException if any error occurs
     * 
     * @since 1.08
     */
    public abstract boolean deleteTask(String task) throws CommunicationException;

    /**
     * Modifies an existing or creates a new task. This method should 
     * be called only if {@link #acceptsTaskModification()} returns 
     * <code>true</code>.
     * 
     * @param task the task to be considered
     * @param credits the number of credits to be stored
     * @throws CommunicationException if any error occurs
     * 
     * @since 1.08
     */
    public abstract void modifyTask(String task, int credits) throws CommunicationException;

    /**
     * Merges with the tasks a file. This method should be called only if
     * {@link #acceptsTaskMerge()} returns <code>true</code>.
     * 
     * @param file the file to be merged
     * @throws CommunicationException if any error occurs
     * 
     * @since 1.08
     */
    public abstract void mergeTasks(String file) throws CommunicationException;

}
