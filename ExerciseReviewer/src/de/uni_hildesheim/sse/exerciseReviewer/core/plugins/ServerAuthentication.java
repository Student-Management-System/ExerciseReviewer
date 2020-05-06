package de.uni_hildesheim.sse.exerciseReviewer.core.plugins;

import java.util.ArrayList;
import java.util.List;

import de.uni_hildesheim.sse.exerciseSubmitter.eclipse.util.GuiUtils;
import de.uni_hildesheim.sse.exerciseSubmitter.eclipse.util.ISubmissionProject;
import de.uni_hildesheim.sse.exerciseSubmitter.submission.
    CommunicationException;
import de.uni_hildesheim.sse.exerciseSubmitter.submission.
    SubmissionCommunication;

/**
 * Defines a handler for server authentications based on
 * submission server structures.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class ServerAuthentication extends 
    de.uni_hildesheim.sse.exerciseSubmitter.submission.ServerAuthentication {

    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private ServerAuthentication() {
    }
    
    /**
     * Initializes this class.
     * 
     * @since 1.00
     */
    public static void initialize() {
        setInstance(new ServerAuthentication());
    }
    
    /**
     * Is called in the case that a submission authentication might
     * be required.
     * 
     * @param comm the communication instance to be considered
     * @param submission <code>true</code> if user names
     *        for submissions should be returned, <code>false</code>
     *        if user names for replay should be returned
     * @return <code>true</code> if the authentication was successful, 
     *         <code>false</code> else
     * 
     * @since 1.00
     */
    @Override
    public boolean authenticate(SubmissionCommunication comm, 
        boolean submission) {
        List<String> users = comm.getUserNames();
        String task;
        if (submission) {
            task = "submit";
        } else {
            task = "replay";
        }
        Object[] result = GuiUtils.showListDialog("Reviewer authentication", 
            "Select the user to " + task + " for",
            users, false);
        if (null != result && result.length > 0) {
            comm.setUserNameForSubmission(result[0].toString());
            try {
                comm.reInitialize();
            } catch (CommunicationException exc) {
                GuiUtils.handleThrowable(exc);
            }
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Maps the specified projects according to the authentication represented
     * by this class. The authentication may change the concrete paths/names to 
     * be considered while submission/replay.
     * 
     * @param projects the projects to be mapped
     * @param comm the communication instance to be considered while doing the 
     *        mapping
     * @return the mapped projects
     * 
     * @since 2.00
     */
    @Override
    public List<ISubmissionProject> mapProjects(
        List<ISubmissionProject> projects, SubmissionCommunication comm) {
        String name = comm.getUserName(true);
        List<ISubmissionProject> result = new ArrayList<ISubmissionProject>();
        for (ISubmissionProject project : projects) {
            result.add(new MappedSubmissionProject(name, project));
        }
        return result;
    }
    
}
