package de.uni_hildesheim.sse.exerciseReviewer.eclipse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import de.uni_hildesheim.sse.exerciseLib.Review;
import de.uni_hildesheim.sse.exerciseLib.ReviewException;
import de.uni_hildesheim.sse.exerciseReviewer.core.ReviewCommunication;
import de.uni_hildesheim.sse.exerciseReviewer.eclipse.decorators.
    ReviewLabelDecorator;
import de.uni_hildesheim.sse.exerciseSubmitter.Activator;
import de.uni_hildesheim.sse.exerciseSubmitter.configuration.IConfiguration;
import de.uni_hildesheim.sse.exerciseSubmitter.eclipse.actions.MessageListener;
import de.uni_hildesheim.sse.exerciseSubmitter.eclipse.util.GuiUtils;
import de.uni_hildesheim.sse.exerciseSubmitter.eclipse.util.ISubmissionProject;
import de.uni_hildesheim.sse.exerciseSubmitter.submission.
    CommunicationException;
import de.uni_hildesheim.sse.exerciseSubmitter.submission.
    CommunicationException.SubmissionPublicMessage;
import de.uni_hildesheim.sse.exerciseSubmitter.submission.
    SubmissionCommunication;
import net.ssehub.exercisesubmitter.protocol.frontend.Assignment;

/**
 * Some utilities for handling reviews in eclipse.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class ReviewUtils {

    /**
     * Stores the communication instances in the case of a 
     * burst of submissions (reviewer mode, submit all).
     * 
     * @since 1.00
     */
    private static List<SubmissionCommunication> burstInstances = null;
    
    /**
     * Prevents this class from being instantiated.
     * 
     * @since 1.00
     */
    private ReviewUtils() {
    }
    
    /**
     * Updates the decorator.
     * 
     * @since 1.00
     */
    public static void updateDecorator() {
        PlatformUI.getWorkbench().getDecoratorManager().update(ReviewLabelDecorator.DECORATOR_ID);
    }
    
    /**
     * Extracts the task name from the path of a project.
     * 
     * @param path the path of a project
     * @return the task name
     * 
     * @since 1.00
     */
    public static Assignment getTaskFromPath(IPath path) {
        int pos = path.segmentCount() - 2;
        String taskName;
        if (pos >= 0) {
            taskName = path.segment(pos);
        } else {
            taskName = "";
        }
        
        SubmissionCommunication com = null;
        if (SubmissionCommunication.getInstancesCount() == 0) {
            // Eclipse may load this on start up before the activator is loaded (e.g, to decorate projects)
            // Init communication on the fly.
            try {
                com = SubmissionCommunication.getInstances(IConfiguration.INSTANCE, IConfiguration.INSTANCE.getUserName(),
                    true, null).get(0);
            } catch (CommunicationException e) {
                Activator.log(e.getMessage(), e);
            }
        } else {
            com = SubmissionCommunication.getInstance(0);
        }
        
//        SubmissionCommunication com = SubmissionCommunication.getInstance(0);
        return SubmissionCommunication.searchForAssignment(taskName, com.getSubmissionsForReview(),
            com.getAvailableForSubmission(), com.getSubmissionsForReplay());
    }
    
    /**
     * Returns the name of the task from the name of the workspace.
     * 
     * @return the name of the task
     * 
     * @since 1.08
     */
    public static String getTaskFromWorkspace() {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        return workspace.getRoot().getRawLocation().lastSegment();
    }
    
    /**
     * Information on a particular project.
     * 
     * @author Holger Eichelberger
     * @since 1.08
     * @version 1.08
     */
    public static class ProjectInfo {
        
        /**
         * Stores the assigned task.
         * 
         * @since 1.08
         */
        private Assignment task;

        /**
         * Stores the assigned submission project.
         * 
         * @since 1.08
         */
        private ISubmissionProject submissionProject;

        /**
         * Stores the assigned Eclipse project.
         * 
         * @since 1.08
         */
        private IProject eclipseProject;
        
        /**
         * Creates a new project information instance.
         * 
         * @param task The assignment under correction
         * @param submissionProject the submission project
         * @param eclipseProject the related Eclipse project
         * 
         * @since 1.08
         */
        private ProjectInfo(Assignment task, ISubmissionProject submissionProject, IProject eclipseProject) {
            this.task = task;
            this.submissionProject = submissionProject;
            this.eclipseProject = eclipseProject;
        }
        
        /**
         * Returns the (assigned) task.
         * 
         * @return The (assigned) task.
         * 
         * @since 1.08
         */
        public Assignment getTask() {
            return task;
        }
        
        /**
         * Returns the related Eclipse project.
         * 
         * @return the related Eclipse project
         * 
         * @since 1.08
         */
        public IProject getEclipseProject() {
            return eclipseProject;
        }
        
        /**
         * Returns the related submission project.
         * 
         * @return the related submission project
         * 
         * @since 1.08
         */
        public ISubmissionProject getSubmissionProject() {
            return submissionProject;
        }
    }

    /**
     * Returns all projects that may be submitted.
     * 
     * @return the list of all projects
     * 
     * @since 1.08
     */
    public static List<ProjectInfo> getAllProjects() {
        List<ProjectInfo> result = new ArrayList<ProjectInfo>();
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        burstInstances = new ArrayList<SubmissionCommunication>();
        for (int i = 0; i < projects.length; i++) {                
            Assignment task = null;
            ISubmissionProject sProject = null;
            if (projects[i] instanceof IJavaProject) {
                IJavaProject project = (IJavaProject) projects[i];
                task = getTaskFromPath(project.getResource().getLocation());
                sProject = ISubmissionProject.createSubmissionProject(project);
            } else {
                task = getTaskFromPath(projects[i].getLocation());
                sProject = ISubmissionProject.createSubmissionProject(projects[i]);
            }
            if (task != null && sProject != null) {
                result.add(new ProjectInfo(task, sProject, projects[i]));
            }
        }
        return result;
    }
    
    /**
     * Submits all projects in the workspace.
     * 
     * @since 1.00
     */
    public static void submitAllProjects() {
        saveAllDirtyEditors();
        List<ProjectInfo> allProjects = getAllProjects();
        try {
            ReviewCommunication comm = ReviewCommunication.getInstance(IConfiguration.INSTANCE, null);
            burstInstances = new ArrayList<SubmissionCommunication>();
            for (ProjectInfo project : allProjects) {                

                Review review = comm.getReview(project.getTask().getName(), project.getSubmissionProject().getName());
                if (null != review) {
                    if (submitProject(project.getSubmissionProject(), project.getTask())) {
                        review.setSubmittedToServer();
                        comm.submitReview(project.getTask(), review);
                    }
                }
            }
            burstInstances = null;
        } catch (CommunicationException e) {
            GuiUtils.handleThrowable(e);
        }
        updateDecorator();
    }

    /**
     * Submits a project to the repository server.
     * 
     * @param project the project to be submitted
     * @param task the name of the task (workspace)
     * @return <code>true</code> if the project was
     *         submitted, <code>false</code> in the
     *         case of errors
     * 
     * @since 1.00
     */
    public static boolean submitProject(ISubmissionProject project, Assignment task) {
        
        boolean result = false;
        if (Boolean.valueOf(IConfiguration.INSTANCE.getProperty("review.resubmitExercise", "true"))) {
            boolean doIt = true;
//            String groupName = IConfiguration.INSTANCE.getGroupName();
//            if (IConfiguration.INSTANCE.isExplicitGroupNameEnabled()) {
//                IConfiguration.INSTANCE.setGroupName(project.getName());
//                String gn = IConfiguration.INSTANCE.getGroupName();
//                if (null == gn || 0 == gn.length()) {
//                    GuiUtils.openDialog(DialogType.ERROR, 
//                        "Empty group name not allowed!");
//                    doIt = false;
//                }
//            }
            if (doIt) {
                List<SubmissionCommunication> commLinks;
                if (null == burstInstances || burstInstances.isEmpty()) {
                    commLinks = GuiUtils.validateConnections(IConfiguration.INSTANCE, project.getName());
                    if (null != burstInstances) {
                        burstInstances.addAll(commLinks);
                    }
                } else {
                    for (SubmissionCommunication comm : burstInstances) {
                        comm.setUserNameForSubmission(project.getName());
//                        comm.setExplicitTargetFolder(IConfiguration.INSTANCE.getExplicitFolderName());
                    }
                    commLinks = burstInstances;
                }
                
                MessageListener messageListener = new MessageListener();
                for (SubmissionCommunication comm : commLinks) {
                    if (null != project) {
                        // use the reviewer user name for communication.
                        GuiUtils.submit(messageListener, project, comm, task);
                        result = true;
                    }
                }
            }
//            IConfiguration.INSTANCE.setGroupName(groupName);
        }
        return result;
    }
    
    /**
     * Writes a given review to a file in the specified project on the local
     * file system.
     * 
     * @param project the project to write the file for
     * @param review the review object
     * @param maxCredits the maximum credits for the task
     * @param userNames the submitting users
     * @throws CommunicationException a wrapped exception in the 
     *         case of errors
     * 
     * @since 1.08
     */
    public static void writeReviewToFile(ISubmissionProject project, 
        Review review, double maxCredits, List<String> userNames) 
        throws CommunicationException {
        String path = project.getPath();
        File file = new File(path, "review.txt");
        try {
            PrintWriter out = new PrintWriter(new FileWriter(file));
            out.print("Review of '");
            out.print(getTaskFromPath(project.getResource().getLocation()));
            out.println("'\n");
            out.println("Submitter: ");
            if (null == userNames) {
                out.println(review.getUserName());
            } else {
                boolean first = true;
                for (String s : userNames) {
                    if (!first) {
                        out.print(", ");
                    }
                    out.print(s);
                    first = false;
                }
                out.println("\n");
            }
            out.print("Reviewer: ");
            out.println(IConfiguration.INSTANCE.getUserName());
            out.print("Credits: ");
            out.print(review.getCredits());
            out.print(" of ");
            out.println(maxCredits);
            out.println();
            out.println();
            out.println("Review comments:");
            out.println(review.getReview());
            out.close();
            project.refreshTopLevel();
        } catch (IOException e) {
            throw new ReviewException(
                SubmissionPublicMessage.FILE_IO_ERROR, e);
        }
    }

    /**
     * Saves all dirty editors.
     * 
     * @since 1.13
     */
    public static final void saveAllDirtyEditors() {
        final IWorkbench wb = PlatformUI.getWorkbench();
        int count = 0;
        for (IWorkbenchPage page : wb.getActiveWorkbenchWindow().getPages()) {
            count += page.getDirtyEditors().length;
        }
        if (count > 0) {
            IRunnableWithProgress saveRunnable = new IRunnableWithProgress() {
                
                public void run(IProgressMonitor monitor)
                    throws InvocationTargetException, InterruptedException {

                    for (IWorkbenchPage page 
                        : wb.getActiveWorkbenchWindow().getPages()) {
                        for (IEditorPart part : page.getDirtyEditors()) {
                            part.doSave(null);
                        }
                    }
                }
                
            };
            
            ISchedulingRule schedulingRule = 
                ResourcesPlugin.getWorkspace().getRoot();
            IProgressService progressService = wb.getProgressService();
            try {
                progressService.runInUI(progressService, saveRunnable, 
                    schedulingRule);
            } catch (Exception e) {
                GuiUtils.handleThrowable(e);
            }
        }
    }
    
}
