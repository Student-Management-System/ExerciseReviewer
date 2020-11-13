package de.uni_hildesheim.sse.exerciseReviewer.core.plugins;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_hildesheim.sse.exerciseLib.Exercise;
import de.uni_hildesheim.sse.exerciseLib.ExerciseData;
import de.uni_hildesheim.sse.exerciseLib.RealUser;
import de.uni_hildesheim.sse.exerciseLib.Review;
import de.uni_hildesheim.sse.exerciseLib.ReviewException;
import de.uni_hildesheim.sse.exerciseLib.User;
import de.uni_hildesheim.sse.exerciseLib.UserProvider;
import de.uni_hildesheim.sse.exerciseReviewer.core.ReviewCommunication;
import de.uni_hildesheim.sse.exerciseReviewer.core.ReviewPlugin;
import de.uni_hildesheim.sse.exerciseReviewer.core.ReviewPublicMessage;
import de.uni_hildesheim.sse.exerciseSubmitter.submission.CommonStuff;
import de.uni_hildesheim.sse.exerciseSubmitter.submission.
    CommunicationException;
import de.uni_hildesheim.sse.exerciseSubmitter.submission.
    CommunicationException.SubmissionPublicMessage;
import net.ssehub.exercisesubmitter.protocol.frontend.Assignment;

/**
 * Defines the interface of a review communication instance writing its data to
 * local files. In the user home the file <code>submissionReviewer.users</code>
 * is expected (see {@link #FileReviewCommunication(String, String)}. The file
 * <code>submissionReviews.tsv</code> will be generated/written.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.11
 */
public class FileReviewCommunication 
    extends ReviewCommunication implements UserProvider {
    
    /**
     * Defines the associated plugin instance.
     * 
     * @since 1.00
     */
    public static final ReviewPlugin PLUGIN = new ReviewPlugin() {

        /**
         * Creates an instance of the described submission communication class.
         * 
         * @param username
         *            the name of the user which will communicate with a
         *            concrete communication server
         * @param password
         *            the password of <code>username</code>
         * @return the created instance
         * 
         * @throws CommunicationException
         *             if any (wrapped) communication error occurs
         * 
         * @since 1.00
         */
        @Override
        public ReviewCommunication createInstance(String userName,
            String password) throws CommunicationException {
            return new FileReviewCommunication(userName, password);
        }

        /**
         * Returns the name of the protocol implemented by this class. The
         * string will be considered when reading the communication based
         * submission configuration. <i>Note:</i> Currently no meachanism for
         * avoiding duplicate protocol names is realized.
         * 
         * @return the name of the implemented protocol
         * 
         * @since 1.00
         */
        @Override
        public String getProtocol() {
            return "file";
        }
    };

    /**
     * Stores the users according to their name.
     * 
     * @since 1.00
     */
    private Map<String, User> users = new HashMap<String, User>();

    /**
     * Stores the exercise data structure.
     * 
     * @since 1.00
     */
    private ExerciseData exerciseData;

    /**
     * The list of the real (known and assigned) users.
     * 
     * @since 1.00
     */
    private List<RealUser> userList;
    
    /**
     * Creates a new review communication instance. To be called by the
     * {@link #PLUGIN} instance. Reads the users file and the exercise and
     * review table.
     * 
     * @param username
     *            the name of the user which will communicate with a concrete
     *            review server
     * @param password
     *            the password of <code>username</code>
     * 
     * @throws CommunicationException
     *             if any (wrapped) communication error occurs
     * 
     * @since 1.00
     */
    private FileReviewCommunication(String username, String password)
        throws CommunicationException {
        super(username, password);
        reloadUsers(); // and therefore the exercises
    }
    
    /**
     * Reloads the user data.
     * 
     * @throws CommunicationException if any error occurred
     * 
     * @since 1.08
     */
    public void reloadUsers() throws CommunicationException {
        try {
            userList = RealUser.readRealUserList(
                new File(getUsersFileName()));
            for (RealUser realUser : userList) {
                if (null != realUser.getGroup()) {
                    User user = users.get(realUser.getGroup());
                    if (null == user) {
                        user = new User(realUser.getGroup());
                        users.put(user.getUserName(), user);
                    }
                    user.addRealUser(realUser);
                }
            }
        } catch (IOException ioe) {
            throw new ReviewException(
                SubmissionPublicMessage.FILE_IO_ERROR, ioe);
        }
        reloadReviews();
    }

    /**
     * Reloads the reviews.
     * 
     * @throws CommunicationException if any error occurred
     * 
     * @since 1.08
     */
    public void reloadReviews() throws CommunicationException {
        exerciseData = new ExerciseData(this);
        try {
            exerciseData.load(new FileReader(getReviewsFileName()));
        } catch (FileNotFoundException ioe) {
            // thats ok
        } catch (IOException ioe) {
            throw new ReviewException(
                SubmissionPublicMessage.FILE_IO_ERROR, ioe);
        }        
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
    public List<String> getRealUsers(String userName)
        throws CommunicationException {
        User user = users.get(userName);
        List<String> result = null;
        if (null != user) {
            result = user.getRealUserNames();
        }
        return result;
    }

    /**
     * Submits a review.
     * 
     * @param task
     *            the task identifier describing the reviewed task/exercise
     * @param review
     *            the review to be submitted
     * @throws CommunicationException
     *             if any communication/validation error occurs
     * 
     * @since 1.00
     */
    public void submitReview(Assignment task, Review review) throws CommunicationException {
        double maxCredits = task.getPoints();
        if (review.getCredits() < 0 || (maxCredits > 0 && review.getCredits() > maxCredits)) {
            throw new ReviewException(SubmissionPublicMessage.INVALID_REVIEW_CREDITS,  new Throwable());
        }
        if (null == users.get(review.getUserName())) {
            throw new ReviewException(ReviewPublicMessage.INVALID_REVIEW_DATASTRUCTURE_NO_USER, new Throwable());
        }

        Exercise exercise = exerciseData.getExercise(task.getName());
        if (null == exercise) {
            throw new ReviewException(ReviewPublicMessage.INVALID_REVIEW_DATASTRUCTURE_NO_EXERCISE, new Throwable());
        } else {
            exercise.addReview(review);
            storeExercises();
        }
    }
    
    /**
     * Stores all exercises.
     * 
     * @throws CommunicationException if an error occurs
     * 
     * @since 1.08
     */
    private void storeExercises() throws CommunicationException {
        try {
            exerciseData.store(new FileWriter(getReviewsFileName()), false);
        } catch (IOException ioe) {
            throw new ReviewException(
                SubmissionPublicMessage.FILE_IO_ERROR, ioe);
        }
        try {
            exerciseData.store(new FileWriter(
                getReviewsRealUsersFileName()), true);
        } catch (IOException ioe) {
            throw new ReviewException(
                SubmissionPublicMessage.FILE_IO_ERROR, ioe);
        }        
    }

    /**
     * Returns the name of the users file.
     * 
     * @return the name of the users file
     * 
     * @since 1.00
     */
    private static String getUsersFileName() {
        return getUserHome() + "submissionReviewer.users";
    }

    /**
     * Returns the location of the user home directory.
     * 
     * @return the location of the user home directory
     * 
     * @since 1.00
     */
    private static String getUserHome() {
        return System.getProperty("user.home") + File.separator;
    }

    /**
     * Returns the name of the reviews file.
     * 
     * @return the name of the reviews file
     * 
     * @since 1.00
     */
    private static String getReviewsFileName() {
        return getUserHome() + "submissionReviews.tsv";
    }

    /**
     * Returns the name of the real users reviews file.
     * 
     * @return the name of the real users reviews file
     * 
     * @since 1.00
     */
    private static String getReviewsRealUsersFileName() {
        return getUserHome() + "submissionRealUsersReviews.tsv";
    }
    
    /**
     * Returns the assigned default directory.
     * 
     * @return the assigned default directory, may be empty or <b>null</b>
     * 
     * @since 1.08
     */
    public String getDefaultDirectory() {
        return getUserHome();
    }
    
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
    public Review getReview(String task, String userName)
        throws CommunicationException {
        return exerciseData.getReview(task, userName);
    }

    /**
     * Authenticates the user by his/her stored data.
     * 
     * @return <code>true</code>, if the user was authenticated,
     *         <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean authenticateUser() {
        return true;
    }

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
    public double getMaximumCredits(String task) 
        throws CommunicationException {
        Exercise exercise = exerciseData.getExercise(task);
        double result;
        if (null == exercise) {
            result = -1;
        } else {
            result = Math.abs(exercise.getMaxCredits());
        }
        return result;
    }

    /**
     * Returns all users known and assigned to the related course.
     * 
     * @return all known and assigned users
     * 
     * @throws CommunicationException if any error occurs
     * 
     * @since 1.08
     */
    public List<RealUser> getAllKnownUsers() throws CommunicationException {
        List<RealUser> users = new ArrayList<RealUser>();
        users.addAll(userList);
        return users;
    }
    
    /**
     * Returns the (repository) user having a given name.
     * 
     * @param name the name of the user
     * @return the user object or <b>null</b> if not found
     * 
     * @since 1.10
     */
    public User getSubmissionUser(String name) {
        return users.get(name);
    }
    
    /**
     * Returns all (repository) users.
     * 
     * @return all repository users
     * 
     * @since 1.10
     */
    public Iterable<User> submissionUsers() {
        return users.values();
    }
    
    /**
     * Returns arbitrary descriptive information on the users.
     * 
     * @return arbitrary descriptive information
     * 
     * @since 1.08
     */
    public String getUserInstanceInformation() {
        return "File: " + getUsersFileName();
    }

    /**
     * Returns arbitrary descriptive information on the reviews.
     * 
     * @return arbitrary descriptive information
     * 
     * @since 1.08
     */
    public String getReviewInstanceInformation() {
        File rFile = new File(getReviewsFileName());
        String rFileText = "";
        if (!rFile.exists()) {
            rFileText = " (not found)";
        }
        File ruFile = new File(getReviewsRealUsersFileName());
        String ruFileText = "";
        if (!ruFile.exists()) {
            ruFileText = " (not found)";
        }
        return "Files: " + getReviewsFileName() + rFileText + ", " 
            + getReviewsRealUsersFileName() + ruFileText;
    }

    /**
     * Returns if this communication instance accepts a complete users
     * specification as file.
     * 
     * @return <code>true</code> if it accepts, <code>false</code> else
     * 
     * @since 1.08
     */
    public boolean acceptsUsersAsFile() {
        return true;
    }
    
    /**
     * Loads the users from a file. This method should be called only if
     * {@link #acceptsUsersAsFile()} returns <code>true</code>.
     * 
     * @param file the file to be loaded
     * @throws CommunicationException if any error occurs
     * 
     * @since 1.08
     */
    public void loadUsers(String file) throws CommunicationException {
        if (!file.equals(getUsersFileName())) {
            File source = new File(file);
            File target = new File(getUsersFileName());
            try {
                CommonStuff.copy(source, target, false);
            } catch (IOException e) {
                throw new ReviewException(
                    SubmissionPublicMessage.FILE_IO_ERROR, e);
            }
        }
        reloadUsers();
    }

    /**
     * Returns if this communication instance accepts a complete result
     * specification as file.
     * 
     * @return <code>true</code> if it accepts, <code>false</code> else
     * 
     * @since 1.08
     */
    public boolean acceptsResultsAsFile() {
        return true;
    }

    /**
     * Loads the results from a file. This method should be called only if
     * {@link #acceptsResultsAsFile()} returns <code>true</code>.
     * 
     * @param file the file to be loaded
     * @throws CommunicationException if any error occurs
     * 
     * @since 1.08
     */
    public void loadResults(String file) throws CommunicationException {
        if (!file.equals(getReviewsFileName())) {
            File source = new File(file);
            File target = new File(getReviewsFileName());
            try {
                CommonStuff.copy(source, target, false);
            } catch (IOException e) {
                throw new ReviewException(
                    SubmissionPublicMessage.FILE_IO_ERROR, e);
            }
        }
        reloadReviews();
    }

    /**
     * Returns the number of reviews for the given <code>task</code>.
     * 
     * @param task the task the number of reviews should be returned for
     * @return the number of reviews
     * @throws CommunicationException if any error occurs
     * 
     * @since 1.08
     */
    public int getReviewCount(String task) throws CommunicationException {
        Exercise exercise = exerciseData.getExercise(task);
        int result;
        if (null == exercise) {
            result = 0;
        } else {
            result = exercise.getReviewCount();
        }
        return result;
    }
    
    /**
     * Deletes a given user.
     * 
     * @param user the user to be deleted
     * @return <code>true</code> if the user was found and deleted, 
     *         <code>false</code> if the user was not found
     * @throws CommunicationException if any (wrapped) exception occurred
     * 
     * @since 1.08
     */
    public boolean deleteUser(RealUser user) throws CommunicationException {
        boolean done = userList.remove(user);
        if (done) {
            File target = new File(getUsersFileName());
            try {
                RealUser.writeRealUserList(userList, target);
                reloadUsers();
            } catch (IOException e) {
                userList.add(user);
                done = false;
                throw new ReviewException(
                    SubmissionPublicMessage.FILE_IO_ERROR, e);
            }
        }
        return done;
    }
    
    /**
     * Returns if general user modifications are permitted.
     * 
     * @return <code>true</code> if general user modifications are permitted,
     *         <code>false</code> else
     * 
     * @since 1.08
     */
    public boolean acceptsUserModification() {
        return true;
    }

    /**
     * Returns if general task modifications are permitted.
     * 
     * @return <code>true</code> if general user modifications are permitted,
     *         <code>false</code> else
     * 
     * @since 1.08
     */
    public boolean acceptsTaskModification() {
        return true;
    }
    
    /**
     * Modifies, i.e. adds a new or modifies an existing user.
     * Therefore first existing users are searched, i.e. if a 
     * reference-equal user object exists. If not, the new user
     * is added.
     * 
     * @param user the user to be modified
     * @throws CommunicationException if any (wrapped) exception occurred
     * 
     * @since 1.08
     */
    public void modifyUser(RealUser user) throws CommunicationException {
        int found = -1;
        for (int i = 0; found < 0 && i < userList.size(); i++) {
            if (userList.get(i) == user) {
                found = i;
            }
        }
        if (found < 0) {
            userList.add(user);
        }

        try {
            File target = new File(getUsersFileName());
            RealUser.writeRealUserList(userList, target);
            reloadUsers();
        } catch (IOException e) {
            if (found < 0) {
                userList.remove(user);
            }
            throw new ReviewException(
                SubmissionPublicMessage.FILE_IO_ERROR, e);
        }
    }
    
    /**
     * Returns all known tasks.
     * 
     * @return all known tasks
     * 
     * @throws CommunicationException if any error occurs
     * 
     * @since 1.08
     */
    public List<String> getAllKnownTasks() throws CommunicationException {
        return exerciseData.getAllExcerciseTasks();
    }

    /**
     * Deletes an existing task.
     * 
     * @param task the task to be deleted
     * @return <code>true</code> if the task was deleted, 
     *         <code>false</code> else
     * @throws CommunicationException if any error occurs
     * 
     * @since 1.08
     */
    public boolean deleteTask(String task) throws CommunicationException {
        boolean done = exerciseData.removeExerciseTask(task);
        if (done) {
            storeExercises();
        }
        return done;
    }

    /**
     * Modifies an existing or creates a new task.
     * 
     * @param task the task to be considered
     * @param credits the number of credits to be stored
     * @throws CommunicationException if any error occurs
     * 
     * @since 1.08
     */
    public void modifyTask(String task, int credits) 
        throws CommunicationException {
        Exercise exercise = exerciseData.getExercise(task);
        if (null != exercise) {
            exercise.setMaxCredits(credits);
            storeExercises();
        } else {
            exercise = new Exercise(task, credits);
            exerciseData.addExercise(exercise);
            storeExercises();
        }
    }
    
    /**
     * Returns if task information can be merged.
     * 
     * @return <code>true</code> if task merges are permitted,
     *         <code>false</code> else
     * 
     * @since 1.08
     */
    public boolean acceptsTaskMerge() {
        return true;
    }

    /**
     * Merges with the tasks a file. This method should be called only if
     * {@link #acceptsTaskMerge()()} returns <code>true</code>.
     * 
     * @param file the file to be merged
     * @throws CommunicationException if any error occurs
     * 
     * @since 1.08
     */
    public void mergeTasks(String file) throws CommunicationException {
        ExerciseData externalData = new ExerciseData(this);
        try {
            externalData.load(new FileReader(file));
            if (exerciseData.mergeWith(externalData)) {
                storeExercises();
            }
        } catch (FileNotFoundException ioe) {
            // thats ok
        } catch (IOException ioe) {
            throw new ReviewException(
                ReviewPublicMessage.INVALID_REVIEW_DATASTRUCTURE,
                ioe);
        } 
    }

}
