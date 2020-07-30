package de.uni_hildesheim.sse.exerciseReviewer.core.plugins;

import java.util.ArrayList;
import java.util.List;

import de.uni_hildesheim.sse.exerciseLib.RealUser;
import de.uni_hildesheim.sse.exerciseLib.Review;
import de.uni_hildesheim.sse.exerciseLib.ReviewException;
import de.uni_hildesheim.sse.exerciseReviewer.core.ReviewCommunication;
import de.uni_hildesheim.sse.exerciseReviewer.core.ReviewPlugin;
import de.uni_hildesheim.sse.exerciseSubmitter.Activator;
import de.uni_hildesheim.sse.exerciseSubmitter.configuration.IConfiguration;
import de.uni_hildesheim.sse.exerciseSubmitter.submission.CommunicationException;
import de.uni_hildesheim.sse.exerciseSubmitter.submission.CommunicationException.SubmissionPublicMessage;
import net.ssehub.exercisesubmitter.protocol.backend.NetworkException;
import net.ssehub.exercisesubmitter.protocol.frontend.Assessment;
import net.ssehub.exercisesubmitter.protocol.frontend.Assignment;
import net.ssehub.exercisesubmitter.protocol.frontend.ExerciseReviewerProtocol;
import net.ssehub.exercisesubmitter.protocol.frontend.User;

/**
 * Defines the interface of a review communication instance using a REST-based communication to the student management
 * system to retrieve course information and submit results.
 * 
 * @author El-Sharkawy
 * @since 2.1
 * @version 2.1
 */
public class StudentManagementCommunication extends ReviewCommunication {
    
    /**
     * Defines the associated plugin instance.
     * 
     * @since 2.1
     */
    public static final ReviewPlugin PLUGIN = new ReviewPlugin() {

        @Override
        public ReviewCommunication createInstance(String userName, String password) throws CommunicationException {
            return new StudentManagementCommunication(userName, password);
        }

        @Override
        public String getProtocol() {
            return "REST";
        }
    };

    private ExerciseReviewerProtocol mgmtProtocol;
    private List<RealUser> participants;
    private List<Review> reviews;
    
    private StudentManagementCommunication(String username, String password) throws CommunicationException {
        super(username, password);
        mgmtProtocol = (ExerciseReviewerProtocol) Activator.getProtocol();
        try {
            mgmtProtocol.login(username, password);
        } catch (NetworkException e) {
            throw new CommunicationException(CommunicationException.SubmissionPublicMessage.AUTHENTICATION_ERROR, e);
        }
    }

    @Override
    public List<String> getRealUsers(String projectName) throws CommunicationException {
        List<String> particpants = new ArrayList<>();
        Assessment assessment;
        try {
            assessment = mgmtProtocol.getAssessmentForSubmission(projectName);
        } catch (NetworkException e) {
            throw new CommunicationException(CommunicationException.SubmissionPublicMessage.
                    UNABLE_TO_CONTACT_STUDENT_MANAGEMENT_SERVER, e);
        }
        
        for (User user : assessment) {
            particpants.add(user.getFullName());
        }

        return particpants;
    }

    @Override
    public void submitReview(Assignment task, Review review) throws CommunicationException {
        double maxCredits = task.getPoints();
        if (review.getCredits() < 0 || (maxCredits > 0 && review.getCredits() > maxCredits)) {
            throw new ReviewException(SubmissionPublicMessage.INVALID_REVIEW_CREDITS,  new Throwable());
        }

        try {
            mgmtProtocol.submitAssessment(review.getAssessment());
            review.setSubmittedToServer();
        } catch (NetworkException e) {
            throw new CommunicationException(CommunicationException.SubmissionPublicMessage.
                UNABLE_TO_CONTACT_STUDENT_MANAGEMENT_SERVER, e);
        }
    }

    @Override
    public Review getReview(String task, String userName) throws CommunicationException {
        if (null == reviews || reviews.isEmpty()) {
            reloadReviews();
        }
        return reviews.stream()
            .filter(r -> userName.equals(r.getAssessment().getSubmitterName()))
            .findAny()
            .orElse(null);
    }

    @Override
    public boolean authenticateUser() {
        return true;
    }

    @Override
    public double getMaximumCredits(String task) throws CommunicationException {
        Assignment assignment = null;
        try {
            assignment = mgmtProtocol.getReviewableAssignments().stream()
                .filter(a -> task.equals(a.getName()))
                .findFirst()
                .orElseThrow(() -> new CommunicationException(SubmissionPublicMessage.
                    COULD_NOT_FIND_REQUESTED_DATA_ON_STUDENT_MANAGEMENT_SERVER, new Throwable()));
        } catch (NetworkException e) {
            new CommunicationException(SubmissionPublicMessage.UNABLE_TO_CONTACT_STUDENT_MANAGEMENT_SERVER, e);
        }
        
        return assignment.getPoints();
    }

    @Override
    public List<RealUser> getAllKnownUsers() throws CommunicationException {
        return participants;
    }

    @Override
    public List<String> getAllKnownTasks() throws CommunicationException {
        List<String> tasks = new ArrayList<>();
        try {
            mgmtProtocol.getReviewableAssignments().stream()
                .forEach(a -> tasks.add(a.getName()));
        } catch (NetworkException e) {
            new CommunicationException(SubmissionPublicMessage.UNABLE_TO_CONTACT_STUDENT_MANAGEMENT_SERVER, e);
        }
        
        return tasks;
    }

    @Override
    public String getUserInstanceInformation() {
        return "Connected to Student Management Server via " + IConfiguration.INSTANCE.getProperty("stdmgmt.server");
    }

    @Override
    public String getReviewInstanceInformation() {
        return "Connected to Student Management Server via " + IConfiguration.INSTANCE.getProperty("stdmgmt.server");
    }

    @Override
    public void reloadUsers() throws CommunicationException {
        participants = new ArrayList<>();
        try {
        mgmtProtocol.loadParticipants().stream()
            .map(u -> toRealUser(u))
            .forEach(participants::add);
        } catch (NetworkException e) {
            new CommunicationException(SubmissionPublicMessage.UNABLE_TO_CONTACT_STUDENT_MANAGEMENT_SERVER, e);
        }
    }
    
    /**
     * Converts a {@link User} to a {@link RealUser}.
     * @param user The user to convert.
     * @return The converted user.
     */
    private RealUser toRealUser(User user) {
        RealUser realUser;
        if (user.getGroupName() == null) {
            realUser = new RealUser(user.getFullName(), user.getEMail(), "", user.getAccountName());            
        } else {
            realUser = new RealUser(user.getFullName(), user.getEMail(), user.getGroupName(), user.getAccountName());
        }
        
        return realUser;
    }

    @Override
    public void reloadReviews() {
        reviews = new ArrayList<>();
        mgmtProtocol.getAssessments().stream()
            .map(a -> new Review(a))
            .forEach(reviews::add);
    }

    @Override
    public String getDefaultDirectory() {
        return null;
    }

    @Override
    public boolean acceptsUsersAsFile() {
        /* 
         * Does not accept reloading via users file, but this is needed to load data into UI table, which is supported
         * by this communication.
         * Misleading naming
         */
        return true;
    }

    @Override
    public void loadUsers(String file) throws CommunicationException {
        reloadUsers();
    }

    @Override
    public boolean acceptsResultsAsFile() {
        /* 
         * Does not accept reloading via results file, but this is needed to load data into UI table, which is supported
         * by this communication.
         * Misleading naming
         */
        return true;
    }

    @Override
    public void loadResults(String file) throws CommunicationException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public int getReviewCount(String task) throws CommunicationException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean deleteUser(RealUser user) throws CommunicationException {
        return false;
    }

    @Override
    public boolean acceptsUserModification() {
        return false;
    }

    @Override
    public boolean acceptsTaskModification() {
        return false;
    }

    @Override
    public boolean acceptsTaskMerge() {
        return false;
    }

    @Override
    public void modifyUser(RealUser user) throws CommunicationException {
        // Eclipse won't be able to modify users of the student management system
    }

    @Override
    public boolean deleteTask(String task) throws CommunicationException {
        // Eclipse won't be able to delete tasks of the student management system
        return false;
    }

    @Override
    public void modifyTask(String task, int credits) throws CommunicationException {
        // Eclipse won't be able to delete tasks of the student management system
    }

    @Override
    public void mergeTasks(String file) throws CommunicationException {
         // Eclipse won't be able to delete tasks of the student management system
    }

}
