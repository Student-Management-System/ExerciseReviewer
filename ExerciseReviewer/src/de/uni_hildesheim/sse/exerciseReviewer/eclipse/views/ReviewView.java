package de.uni_hildesheim.sse.exerciseReviewer.eclipse.views;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.IPackagesViewPart;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ViewPart;

import de.uni_hildesheim.sse.exerciseLib.Review;
import de.uni_hildesheim.sse.exerciseLib.ReviewException;
import de.uni_hildesheim.sse.exerciseReviewer.core.ReviewCommunication;
import de.uni_hildesheim.sse.exerciseReviewer.core.plugins.ServerAuthentication;
import de.uni_hildesheim.sse.exerciseReviewer.eclipse.ReviewUtils;
import de.uni_hildesheim.sse.exerciseReviewer.eclipse.decorators.
    ReviewLabelDecorator;
import de.uni_hildesheim.sse.exerciseSubmitter.Activator;
import de.uni_hildesheim.sse.exerciseSubmitter.configuration.IConfiguration;
import de.uni_hildesheim.sse.exerciseSubmitter.eclipse.util.GuiUtils;
import de.uni_hildesheim.sse.exerciseSubmitter.eclipse.util.GuiUtils.DialogType;
import de.uni_hildesheim.sse.exerciseSubmitter.eclipse.util.ISubmissionProject;
import de.uni_hildesheim.sse.exerciseSubmitter.eclipse.util.SwitchWorkspace;
import de.uni_hildesheim.sse.exerciseSubmitter.submission.
    CommunicationException;
import de.uni_hildesheim.sse.exerciseSubmitter.submission.IMessage;
import de.uni_hildesheim.sse.exerciseSubmitter.submission.IMessageListener;
import de.uni_hildesheim.sse.exerciseSubmitter.submission.IPathFactory;
import de.uni_hildesheim.sse.exerciseSubmitter.submission.Submission;
import de.uni_hildesheim.sse.exerciseSubmitter.submission.
    SubmissionCommunication;
import net.ssehub.exercisesubmitter.protocol.backend.NetworkException;
import net.ssehub.exercisesubmitter.protocol.frontend.Assessment;
import net.ssehub.exercisesubmitter.protocol.frontend.Assignment;
import net.ssehub.exercisesubmitter.protocol.frontend.ExerciseReviewerProtocol;

/**
 * A workbench view for reviewing submitted exercises. This view provides a
 * combined view on tasks and problems as well as an editor part to specify the
 * review.
 * 
 * @author Holger Eichelberger
 * @version 1.00
 * @since 1.00
 */
public class ReviewView extends ViewPart implements IPathFactory {

    /**
     * Stores the label prefix for the credits label.
     * 
     * @since 1.00
     */
    private static final String CREDITS_LABEL_TEXT_PREFIX = "Credits";

    /**
     * Stores the label postfix for the credits label.
     * 
     * @since 1.00
     */
    private static final String CREDITS_LABEL_TEXT_POSTFIX = ":";

    /**
     * Stores the simple label text for the credits label (combination of
     * {@link #CREDITS_LABEL_TEXT_PREFIX} and
     * {@link #CREDITS_LABEL_TEXT_POSTFIX}).
     * 
     * @since 1.00
     */
    private static final String CREDITS_LABEL_TEXT_SIMPLE =
        CREDITS_LABEL_TEXT_PREFIX + CREDITS_LABEL_TEXT_POSTFIX + "          ";

    /**
     * Stores the viewer displaying {@link #table}.
     * 
     * @since 1.00
     */
    private TableViewer viewer;

    /**
     * Stores view action to load all submitted exercises from the submission
     * server for review.
     * 
     * @since 1.00
     */
    private Action checkoutAction;

    /**
     * Stores view action to load all submitted exercises from the submission
     * server for review and replay.
     * 
     * @since 1.13
     */
    private Action checkoutReplayReviewAction;
    
    /**
     * Stores view action to submit all exercises to the submission
     * server.
     * 
     * @since 1.00
     */
    private Action submitAllAction;
    
    /**
     * Stores the general Java project (may be <b>null</b>) denoting the
     * project to be reviewed.
     * 
     * @since 1.00
     */
    private IProject iProject;

    /**
     * Stores the Eclipse Java project (may be <b>null</b>) denoting the
     * project to be reviewed.
     * 
     * @since 1.00
     */
    private IJavaProject iJavaProject;

    /**
     * Stores the task/exercise.
     * 
     * @since 1.00
     */
    private Assignment task;

    /**
     * Stores the label for the users who submitted the project.
     * 
     * @since 1.00
     */
    private Label labelUsers;

    /**
     * Stores the label for the project name.
     * 
     * @since 1.00
     */
    private Label labelProject;

    /**
     * Stores the label for the credit points.
     * 
     * @since 1.00
     */
    private Label labelCredits;

    /**
     * Stores the text input field for the input of the review text.
     * 
     * @since 1.00
     */
    private Text review;

    /**
     * Stores the text input field for the input of the credit value.
     * 
     * @since 1.00
     */
    private Text credits;

    /**
     * Stores the marker table.
     * 
     * @since 1.00
     */
    private Table table;

    /**
     * Stores the gather information button.
     * 
     * @since 1.00
     */
    private Button gatherButton;

    /**
     * Stores the submit button.
     * 
     * @since 1.00
     */
    private Button submit;

    /**
     * Stores the store button.
     * 
     * @since 1.00
     */
    private Button store;
    
    /**
     * Stores the maximum allowed number of credits.
     * 
     * @since 1.00
     */
    private double maxCredits;

    /**
     * Stores the relations between table items and markers.
     * 
     * @since 1.18
     */
    private Map<TableItem, IMarker> itemMap = new HashMap<TableItem, IMarker>();
    
    /**
     * The constructor of this view.
     * 
     * @since 1.00
     */
    public ReviewView() {
        GuiUtils.showSubmissionSuccess(false);
        Activator.setAsReviewer(true);
    }

    /**
     * Creates the view and initializes it and its GUI.
     * 
     * @param parent
     *            the parent UI element
     * 
     * @since 1.00
     */
    public void createPartControl(Composite parent) {
        parent.setLayout(new FillLayout());
        ScrolledComposite scrollPanel = 
            new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
        Composite panel = new Composite(scrollPanel, SWT.NONE);
        scrollPanel.setContent(panel);
        
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        panel.setLayout(gridLayout);

        table = new Table(panel, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);

        String[] columns = {"Type", "Description", "Resource", 
            "Path", "Line"};

        for (int i = 0; i < columns.length; i++) {
            TableColumn tc = new TableColumn(table, SWT.LEFT);
            tc.setText(columns[i]);
        }

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumn(i).pack();
        }
        Point p = table.computeSize(500, 200);

        viewer = new TableViewer(table);
        viewer.addSelectionChangedListener(new TaskOpenListener());
        GridData gridData = new GridData(SWT.BEGINNING, 
            SWT.NORMAL, false, false);
        gridData.widthHint = p.x;
        gridData.heightHint = p.y;
        table.setLayoutData(gridData);

        Group group = createEditGroup(panel);
        gridData = new GridData(SWT.BEGINNING, SWT.NORMAL, true, false);
        group.setLayoutData(gridData);

        makeActions();
        hookContextMenu();
        contributeToActionBars();

        ResourcesPlugin.getWorkspace().addResourceChangeListener(
            new ResourceChangeListener());
        // names can be obtained from getSite().getPage().get...References()
        getSite().getPage().addSelectionListener(JavaUI.ID_PACKAGES,
            new WorkbenchPartSelectionListener());

        panel.getShell().addListener(SWT.Deactivate, new EditorListener());
        panel.setSize(panel.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        ServerAuthentication.initialize();
        
        ReviewException ex = ReviewLabelDecorator.getLastException();
        if (null != ex) {
            GuiUtils.handleThrowable(ex);
        }
    }
    
    /**
     * Defines a listener to be informed when the task selection changes.
     * 
     * @author Holger Eichelberger
     * @since 1.18
     * @version 1.18
     */
    private class TaskOpenListener implements ISelectionChangedListener {

        /**
         * Called when the selection changes.
         * 
         * @param event the selection event
         * 
         * @since 1.18
         */
        public void selectionChanged(SelectionChangedEvent event) {
            TableItem[] selection = table.getSelection();
            if (null != selection && selection.length > 0) {
                TableItem selected = selection[0];
                try {
                    IMarker marker = itemMap.get(selected);
                    IResource resource = marker.getResource();
                    if (marker.exists() && resource instanceof IFile) {
                        IWorkbenchPage page = 
                            ReviewView.this.getSite().getPage();
                        try {
                            IDE.openEditor(page, marker, 
                                OpenStrategy.activateOnOpen());
                        } catch (PartInitException e) {
                        }
                    }
                } catch (Exception e) {
                } 
            }
        }
    }

    /**
     * Implements a general listener to be called when anything is
     * activated/deactivated in order to update the GUI.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private class EditorListener implements Listener {

        /**
         * Called in the case of an event.
         * 
         * @param event
         *            the event to be handled
         * 
         * @since 1.00
         */
        public void handleEvent(final Event event) {
            updateUI(event.widget);
        }

    }

    /**
     * Implements a resource change listener in order to make an update of the
     * view when a resource is modified, ...
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private class ResourceChangeListener implements IResourceChangeListener {

        /**
         * Is called when a resource is changed.
         * 
         * @param event
         *            the description of the change
         * 
         * @since 1.00
         */
        public void resourceChanged(final IResourceChangeEvent event) {
            if (event.getType() == IResourceChangeEvent.POST_BUILD
                || event.getType() == IResourceChangeEvent.POST_CHANGE
                || event.getType() == IResourceChangeEvent.PRE_DELETE) {
                if (event.getResource() != null) {
                    Display.getDefault().syncExec(new Runnable() {

                        public void run() {
                            updateUI(getProject(event.getResource()));
                        }

                    });
                } else if (event.getDelta() != null) {
                    Display.getDefault().syncExec(new Runnable() {

                        public void run() {
                            getProject(event.getDelta());
                            updateUI(getProject(event.getDelta()));
                        }

                    });
                }
            } else if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
                Display.getDefault().syncExec(new Runnable() {

                    public void run() {
                        clearEditor();
                    }

                });
            }
        }
    }

    /**
     * Returns the project from the given resource.
     * 
     * @param resource
     *            the resource to be considered
     * @return the first valid reference to a project instance
     * 
     * @since 1.00
     */
    private IProject getProject(IResource resource) {
        IContainer container =
            ResourcesPlugin.getWorkspace().getRoot().getContainerForLocation(
                resource.getLocation());
        if (null != container) {
            while (null != container && !(container instanceof IProject)) {
                container = container.getParent();
            }
            if (container instanceof IProject) {
                return (IProject) container;
            }
        }
        return null;
    }

    /**
     * Returns the project of the modified elements stored in 
     * <code>delta</code>.
     * 
     * @param delta
     *            change information on changed resources
     * @return the first valid reference to a project instance
     * 
     * @since 1.00
     */
    private IProject getProject(IResourceDelta delta) {
        IResourceDelta[] children = delta.getAffectedChildren();
        IContainer container = null;
        for (int i = 0; null == container && i < children.length; i++) {
            container =
                ResourcesPlugin.getWorkspace().getRoot()
                    .getContainerForLocation(
                        children[i].getResource().getLocation());
            if (null == container) {
                IProject project = getProject(children[i]);
                if (null != project) {
                    return project;
                }
            }
        }
        if (null != container) {
            while (null != container && !(container instanceof IProject)) {
                container = container.getParent();
            }
            if (container instanceof IProject) {
                return (IProject) container;
            }
        }
        return null;
    }

    /**
     * Implements a selection listener to receive information on when a project
     * is selected.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private class WorkbenchPartSelectionListener implements ISelectionListener {

        /**
         * Is called when a workbench part is selected.
         * 
         * @param part
         *            describes the part of the workbench in which the selection
         *            was changed
         * @param selection
         *            describes the selection
         * 
         * @since 2.0
         */
        public void selectionChanged(IWorkbenchPart part, 
            ISelection selection) {
            updateUI(part);
        }
    }

    /**
     * Updates the user interface due to the selection of a given workbench
     * part. This method tries to extract Eclipse Java projects.
     * 
     * @param part
     *            the selected workbench part
     * 
     * @since 1.00
     */
    private void updateUI(IWorkbenchPart part) {
        if (part instanceof IPackagesViewPart) {
            IPackagesViewPart pvp = (IPackagesViewPart) part;
            TreeViewer tv = pvp.getTreeViewer();
            Tree tree = tv.getTree();
            TreeItem[] item = tree.getSelection();
            for (int i = 0; i < item.length; i++) {
                if (item[i].getData() instanceof IJavaProject) {
                    updateUI((IJavaProject) item[i].getData());
                } else if (item[i].getData() instanceof IProject) {
                    updateUI((IProject) item[i].getData());
                } else if (item[i].getData() instanceof IFile) {
                    IFile file = (IFile) item[i].getData();
                    if (null != file.getProject()) {
                        updateUI(file.getProject());
                    }
                } else if (item[i].getData() instanceof IJavaElement) {
                    IJavaElement javaElement = (IJavaElement) item[i].getData();
                    if (null != javaElement.getJavaProject()) {
                        updateUI(javaElement.getJavaProject());                 
                    }
                }
            }
        }
    }

    /**
     * Updates the user interface due to the selection of an arbitrary UI
     * widget. This method tries to extract general Eclipse projects.
     * 
     * @param widget
     *            the widget to be considered
     * 
     * @since 1.00
     */
    private void updateUI(Widget widget) {
        for (IWorkbenchPage page : getSite().getWorkbenchWindow().getPages()) {
            for (IEditorReference editor : page.getEditorReferences()) {
                IEditorPart part = editor.getEditor(false);
                if (null != part) {
                    if (widget == part.getEditorSite().getShell()) {
                        if (part.getEditorInput() instanceof FileEditorInput) {
                            FileEditorInput input =
                                (FileEditorInput) part.getEditorInput();
                            for (IProject project : ResourcesPlugin
                                .getWorkspace().getRoot().getProjects()) {
                                String file = input.getFile().toString();
                                if (file.startsWith("L/" + project.getName()
                                    + "/")) {
                                    file = file.substring(project.getName()
                                        .length() + 3);
                                }
                                if (null != project.findMember(file)) {
                                    updateUI(project);
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Updates the user interface due to the selection of an general Java
     * project. This method calls {@link #clearEditor()},
     * {@link #retrieveProjectData(IPath, String)},
     * {@link #setProblemMarker(IMarker[])}, {@link #setTaskMarker(IMarker[])}.
     * 
     * @param project
     *            the selected project as data source
     * 
     * @since 1.00
     */
    private void updateUI(IProject project) {
        if (null != project && project != iProject) {
            clearEditor();
            iProject = project;
            retrieveProjectData(project.getLocation(), project.getName());
            try {
                setProblemMarker(project.findMarkers(IMarker.PROBLEM, true,
                    IResource.DEPTH_INFINITE));
            } catch (CoreException ce) {
            }
            try {
                setTaskMarker(project.findMarkers(IMarker.TASK, true,
                    IResource.DEPTH_INFINITE));
            } catch (CoreException ce) {
            }
        }
    }
    
    /**
     * Retrieves relevant data from the specified project.
     * 
     * @param path
     *            the root directory of the project
     * @param userName
     *            the name of the user (here equal to the name of the project)
     * 
     * @since 1.00
     */
    private void retrieveProjectData(IPath path, String userName) {
        task = ReviewUtils.getTaskFromPath(path);
        if (null != task) {
            labelProject.setText(task.getName() + " (" + userName + ")");
            try {
                ReviewCommunication comm = ReviewCommunication.getInstance(IConfiguration.INSTANCE, null);
                maxCredits = task.getPoints();
                StringBuilder builder = new StringBuilder("");
                List<String> realUsers = comm.getRealUsers(userName);
                if (null != realUsers) {
                    for (String user : comm.getRealUsers(userName)) {
                        if (builder.length() > 0) {
                            builder.append(", ");
                        }
                        builder.append(user);
                    }
                    labelUsers.setText(builder.toString());
                } else {
                    labelUsers.setText(userName);
                }
                if (maxCredits > 0) {
                    labelCredits.setText(CREDITS_LABEL_TEXT_PREFIX + "(" + maxCredits + ")"
                        + CREDITS_LABEL_TEXT_POSTFIX);
                }
                Review review = comm.getReview(task.getName(), userName);
                if (null != review) {
                    this.review.setText(review.getReview());
                    this.credits.setText(String.valueOf(review.getCredits()));
                }
            } catch (CommunicationException ex) {
            }
            submit.setEnabled(true);
            store.setEnabled(true);
            gatherButton.setEnabled(true);
        }
    }

    /**
     * Updates the user interface due to the selection of an Eclipse Java
     * project. This method calls 
     * {@link #retrieveProjectData(IPath, String)} and
     * {@link #setProblemMarker(IMarker[])}, {@link #setTaskMarker(IMarker[])}.
     * 
     * @param project
     *            the selected project as data source
     * 
     * @since 1.00
     */
    private void updateUI(IJavaProject project) {
        if (null != project && iJavaProject != project) {
            clearEditor();
            iJavaProject = project;
            retrieveProjectData(project.getResource().getLocation(), project.getElementName());
            try {
                setProblemMarker(project.getResource().findMarkers(
                    IMarker.PROBLEM, true, IResource.DEPTH_INFINITE));
            } catch (CoreException ce) {
            }
            try {
                setTaskMarker(project.getResource().findMarkers(IMarker.TASK,
                    true, IResource.DEPTH_INFINITE));
            } catch (CoreException ce) {
            }
        }
    }

    /**
     * Clears the editor part of the view.
     * 
     * @since 1.00
     */
    private void clearEditor() {
        iProject = null;
        iJavaProject = null;
        task = null;
        maxCredits = -1;
        labelUsers.setText("");
        labelProject.setText("");
        review.setText("");
        credits.setText("");
        table.removeAll();
        itemMap.clear();
        submit.setEnabled(false);
        store.setEnabled(false);
        gatherButton.setEnabled(false);
        labelCredits.setText(CREDITS_LABEL_TEXT_SIMPLE);
    }

    /**
     * Sets the given problem marker. This method calls
     * {@link #fillMarkerItem(IMarker, TableItem)}.
     * 
     * @param marker
     *            the markers to be transferred to the view
     * 
     * @since 1.00
     */
    private void setProblemMarker(IMarker[] marker) {
        for (IMarker m : marker) {
            TableItem item = new TableItem(table, 0);
            item.setText(0, "Problem");
            item.setText(1, m.getAttribute(IMarker.MESSAGE, ""));
            fillMarkerItem(m, item);
        }
    }

    /**
     * Sets the given task marker. This method calls
     * {@link #fillMarkerItem(IMarker, TableItem)}.
     * 
     * @param marker
     *            the markers to be transferred to the view
     * 
     * @since 1.00
     */
    private void setTaskMarker(IMarker[] marker) {
        for (IMarker m : marker) {
            String message = m.getAttribute(IMarker.MESSAGE, "");
            if (message.length() > 8 && message.startsWith("REVIEW ")) {
                message = message.substring(7);
                TableItem item = new TableItem(table, 0);
                item.setText(0, "Review Issue");
                item.setText(1, message);
                fillMarkerItem(m, item);
            }
        }
    }

    /**
     * Fills a table item with the generic data from the given marker.
     * 
     * @param marker
     *            the marker as information source
     * @param item
     *            the table item as information sink
     * 
     * @since 1.00
     */
    private void fillMarkerItem(IMarker marker, TableItem item) {
        String name = marker.getResource().getName();
        item.setText(2, name);
        String path = marker.getResource().getFullPath().toString();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.endsWith(name)) {
            path = path.substring(0, path.length() - name.length());
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        item.setText(3, path);
        item.setText(4, marker.getAttribute(IMarker.LOCATION, ""));
        itemMap.put(item, marker);
    }

    /**
     * Creates the edit part of the view form.
     * 
     * @param parent
     *            the parent element (view)
     * @return the newly create group element
     * 
     * @since 1.00
     */
    private Group createEditGroup(Composite parent) {
        Group group = new Group(parent, 0);
        group.setText("Review Data");
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        group.setLayout(gridLayout);

        Label label = new Label(group, 0);
        label.setText("Exercise:");
        GridData gridData = new GridData(GridData.BEGINNING);
        gridData.horizontalSpan = 1;
        label.setLayoutData(gridData);

        labelProject = new Label(group, 0);
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gridData.horizontalSpan = 1;
        labelProject.setLayoutData(gridData);

        label = new Label(group, 0);
        label.setText("User(s):");
        gridData = new GridData(GridData.BEGINNING);
        gridData.horizontalSpan = 1;
        label.setLayoutData(gridData);

        labelUsers = new Label(group, 0);
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gridData.horizontalSpan = 1;
        labelUsers.setLayoutData(gridData);

        Label labelReview = new Label(group, 0);
        labelReview.setText("Review:");
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gridData.horizontalSpan = 1;
        labelReview.setLayoutData(gridData);

        ButtonSelectionListener buttonListener = new ButtonSelectionListener();
        gatherButton = new Button(group, SWT.PUSH);
        gatherButton.setText("Gather from Markers");
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
        gridData.horizontalSpan = 1;
        gatherButton.setLayoutData(gridData);
        gatherButton.addSelectionListener(buttonListener);
        review = new Text(group, SWT.WRAP | SWT.BORDER);
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gridData.heightHint = 120;
        gridData.widthHint = 300;
        gridData.horizontalSpan = 2;
        review.setLayoutData(gridData);

        labelCredits = new Label(group, 0);
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gridData.horizontalSpan = 1;
        labelCredits.setLayoutData(gridData);
        labelCredits.setText(CREDITS_LABEL_TEXT_SIMPLE);

        credits = new Text(group, SWT.BORDER);
        credits.setTextLimit(5);
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
        gridData.horizontalSpan = 1;
        credits.setLayoutData(gridData);

        store = new Button(group, SWT.PUSH);
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gridData.horizontalSpan = 1;
        store.setLayoutData(gridData);
        store.setText("Store");
        store.addSelectionListener(buttonListener);
        store.setEnabled(false);

        submit = new Button(group, SWT.PUSH);
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
        gridData.horizontalSpan = 1;
        submit.setLayoutData(gridData);
        submit.setText("Submit");
        submit.addSelectionListener(buttonListener);
        submit.setVisible(Boolean.valueOf(IConfiguration.INSTANCE.getProperty(
            "review.resubmitExercise", "true")));

        submit.setEnabled(false);
        gatherButton.setEnabled(false);

        return group;
    }

    /**
     * Implements a common button selection listener.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private class ButtonSelectionListener implements SelectionListener {

        /**
         * Is called if the widget is selected as default.
         * 
         * @param event
         *            an object describing the event
         * 
         * @since 1.00
         */
        public void widgetDefaultSelected(SelectionEvent event) {
        }

        /**
         * Is called if the widget is selected.
         * 
         * @param event
         *            an object describing the event
         * 
         * @since 1.00
         */
        public void widgetSelected(SelectionEvent event) {
            if (event.getSource() == gatherButton) {
                gatherButtonSelected(event);
            } else if (event.getSource() == submit) {
                submitButtonSelected(event, true);
            } else if (event.getSource() == store) {
                submitButtonSelected(event, false);
            }
        }

    }

    /**
     * Is called, when the gather information button is selected/pressed.
     * 
     * @param event
     *            the event denoting the selection event
     * 
     * @since 1.00
     */
    private void gatherButtonSelected(SelectionEvent event) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < table.getItemCount(); i++) {
            TableItem item = table.getItem(i);
            builder.append(item.getText(0));
            builder.append("(");
            builder.append(item.getText(3));
            builder.append("/");
            builder.append(item.getText(2));
            String loc = item.getText(4);
            if (loc.length() > 0) {
                if (loc.startsWith("line ")) {
                    loc = loc.substring(5);
                }
                builder.append(":");
                builder.append(loc);
            }
            builder.append(") ");
            builder.append(item.getText(1));
            builder.append("\n");
        }
        ISubmissionProject project = obtainSubmissionProject();
        String log = null;
        try {
            SubmissionCommunication replayComm = GuiUtils.getFirstReplayConnection(IConfiguration.INSTANCE, null);
            if (null != replayComm) {
                log = replayComm.getSubmissionLog(task, project.getName());
            }
        } catch (CommunicationException e) {
            // for now, don't bother the user
            e.printStackTrace();
            //GuiUtils.handleThrowable(e);
        }
        if (null != log) {
            IMessageListener listener = new IMessageListener() {
                
                @Override
                public void notifyMessage(IMessage message) {
                    builder.append("\n");
                    builder.append("Here goes the submission log:\n");
                    if (null != message.getFile()) {
                        builder.append(message.getFile());
                        if (message.getLine() > 0) {
                            builder.append(":");
                            builder.append(message.getLine());
                        }
                        builder.append(" ");
                    }
                    if (null != message.getType()) {
                        builder.append(message.getType().name());
                        builder.append(" ");
                    }
                    if (null != message.getTool() 
                        && message.getTool().length() > 0) {
                        builder.append(" by ");
                        builder.append(message.getTool());
                        builder.append(" ");
                    }
                    builder.append(message.getMessage());
                    builder.append("\n");
                }
            };
            Submission.getUnparsedMessage(log, listener);
        }

        review.setText(builder.toString());
    }
    
    /**
     * Obtains the actual submission project.
     * 
     * @return the submission project
     */
    private ISubmissionProject obtainSubmissionProject() {
        ISubmissionProject project = null;
        if (null != iJavaProject) {
            project = ISubmissionProject
                .createSubmissionProject(iJavaProject);
        } else if (null != iProject) {
            project =
                ISubmissionProject.createSubmissionProject(iProject);
        }
        return project;
    }

    /**
     * Is called, when the submit button is selected/pressed.
     * 
     * @param event
     *            the event denoting the selection event
     * @param submitToRepository submit the review to a server
     * 
     * @since 1.00
     */
    private void submitButtonSelected(SelectionEvent event, boolean submitToRepository) {
        try {
            double creditValue = Double.parseDouble(credits.getText());
            if (creditValue < 0) {
                GuiUtils.openDialog(DialogType.ERROR, "Credit value is negative!");
            } else if (maxCredits > 0 && creditValue > maxCredits) {
                GuiUtils.openDialog(DialogType.ERROR, "Credit value exceeds maximum value (" + maxCredits + ")!");
            } else if (review.getText().length() == 0 ) {
                GuiUtils.openDialog(DialogType.ERROR, "Review text is empty!");
            } else {
                ReviewUtils.saveAllDirtyEditors();
                String reviewText = review.getText();
                ISubmissionProject project = obtainSubmissionProject();
                try {
                    // Not nice here, but the tool uses only one protocol instance -> Thus, it can be used that way
                    Assessment assessment = ((ExerciseReviewerProtocol) Activator.getProtocol())
                        .getAssessmentForSubmission(project.getName());
                    if (null != assessment) {
                        Review review = new Review(assessment);
                        review.getAssessment().setAchievedPoints(creditValue);
                        review.getAssessment().setFullReviewComment(reviewText);
                       
                        ReviewCommunication comm = ReviewCommunication.getInstance(IConfiguration.INSTANCE, null);
                        if (submitToRepository) {
                            if (ReviewUtils.submitProject(project, task)) {
                                review.setSubmittedToServer();
                            }
                        }
                        comm.submitReview(task, review);
                        if (Boolean.valueOf(IConfiguration.INSTANCE.getProperty("review.storeInExercise", "true"))) {
                            ReviewUtils.writeReviewToFile(project, review, maxCredits,
                                comm.getRealUsers(review.getUserName()));
                            refreshProject();
                        }
                        ReviewUtils.updateDecorator();
                    }
                } catch (CommunicationException | NetworkException e) {
                    GuiUtils.handleThrowable(e);
                }
            }
        } catch (NumberFormatException e) {
            GuiUtils.openDialog(DialogType.ERROR, "Credit input is not a number!");
        }
    }
    
    /**
     * Refreshes the currently selected (reviewed) project.
     * 
     * @since 1.00
     */
    private void refreshProject() {
        try {
            if (null != iJavaProject) {
                iJavaProject.getResource().
                    refreshLocal(IResource.DEPTH_INFINITE, null);
            }
            if (null != iProject) {
                iProject.refreshLocal(IResource.DEPTH_INFINITE, null);
            }
        } catch (CoreException ce) {
        }
        
    }

    /**
     * Called internally from an action when the replay all exercises command is
     * issued.
     * 
     * @param alsoForReplay if <code>true</code> also the tasks marked for
     *        replay only (not review) will be made available, if 
     *        <code>false</code> only those for review will be shown
     * 
     * @since 1.00
     */
    private void replayAllButtonSelected(boolean alsoForReplay) {
        SubmissionCommunication replayComm = GuiUtils.getFirstReplayConnection(IConfiguration.INSTANCE, null);
        if (null == replayComm) {
            GuiUtils.openDialog(DialogType.ERROR, "No repository connection allows replay of submissions!");
        } else {
            List<Assignment> replayList = new ArrayList<Assignment>(replayComm.getSubmissionsForReview());
            if (alsoForReplay) {
                replayList.addAll(replayComm.getSubmissionsForReplay());
            }
            Object[] tasks = GuiUtils.showListDialog("Complete assignment replay",
                "Select the entire assignment for replay", replayList, false);
            if (tasks.length == 1) {
                Assignment task = (Assignment) tasks[0];
                IPath root = ResourcesPlugin.getWorkspace().getRoot().getLocation();
                File rootPath = new File(root.toString());
                if (root.lastSegment().equals(task.getName())) {
                    // Goal reached -> Check out submissions and start correction
                    IConfiguration.INSTANCE.setAsssignment(task);
                    IConfiguration.INSTANCE.store();   
                    GuiUtils.runEntireReplay("Replaying entire task '" + task.getName() + "'", replayComm, rootPath,
                        task, this, ResourcesPlugin.getWorkspace().getRoot());
                    try {
                        ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
                        ((ExerciseReviewerProtocol) Activator.getProtocol()).loadAssessments(task);
                    } catch (CoreException e) {
                    } catch (NetworkException e) {
                        GuiUtils.openDialog(DialogType.ERROR, "Could not load assessments from server");
                    }
                    ReviewUtils.updateDecorator();
                } else {
                    // Wrong workspace -> Switch workspace but do not check out submissions, yet
                    if (GuiUtils.openDialog(DialogType.CONFIRMATION,
                        "This is the wrong workspace. Shall we switch to an appropriate (new) one (without "
                            + "preferences)?")) {
                        boolean done = SwitchWorkspace.switchWorkspace(root.removeLastSegments(1).toString()
                            + File.separator + task.getName());
                        if (!done) {
                            GuiUtils.openDialog(
                                DialogType.CONFIRMATION,
                                "However, eclipse was not able to create the new workspace. You must create a new " 
                                + "workspace with name '" + task.getName() + "' and copy your preferences thereby, "
                                + "switch to the new workspace and execute this action on the selected task again.");
                        }
                    }
                }
            }
        }
    }

    /**
     * Creates the <code>subdirectory</code> in the specified
     * <code>path</code>.
     * 
     * @param path
     *            the target path
     * @param subdirectory
     *            the subdirectory to be created
     * @return the created directory as File object
     * 
     * @since 1.00
     */
    public File createPath(File path, String subdirectory) {

        File result = new File(path, subdirectory);
        
        // getProject always returns something...
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().
            getProjects();
        boolean found = false;
        for (int p = 0; p < projects.length; p++) {
            found = projects[p].getName().equals(subdirectory);
        }
        if (!found) {
            try {
                IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
                IProject project = root.getProject(subdirectory);
                project.create(null);
                project.open(null);
                
                IProjectDescription description = project.getDescription();
                // description.setLocation(new Path(result.getAbsolutePath()));
                project.move(description, true, null);
            } catch (CoreException e) {
                e.printStackTrace(System.out);
            }
        }
        return result;
    }

    /**
     * Hooks the context menu into the correct environment.
     * 
     * @since 1.00
     */
    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu");
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                ReviewView.this.fillContextMenu(manager);
            }
        });
        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);
    }

    /**
     * Does the contribution to the relevant action bars.
     * 
     * @since 1.00
     */
    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalPullDown(bars.getMenuManager());
        fillLocalToolBar(bars.getToolBarManager());
    }

    /**
     * Fills the local pull down manager.
     * 
     * @param manager
     *            the local pull down menu manager
     * 
     * @since 1.00
     */
    private void fillLocalPullDown(IMenuManager manager) {
        manager.add(checkoutAction);
        manager.add(submitAllAction);
        manager.add(checkoutReplayReviewAction);
    }

    /**
     * Fills the local context menu.
     * 
     * @param manager
     *            the local menu manager
     * 
     * @since 1.00
     */
    private void fillContextMenu(IMenuManager manager) {
        manager.add(checkoutAction);
        manager.add(submitAllAction);
        manager.add(checkoutReplayReviewAction);
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    /**
     * Fills the local tool bar.
     * 
     * @param manager
     *            the local tool bar manager
     * 
     * @since 1.00
     */
    private void fillLocalToolBar(IToolBarManager manager) {
        manager.add(checkoutAction);
        manager.add(submitAllAction);
        manager.add(checkoutReplayReviewAction);
    }

    /**
     * Creates the individual action objects associated with this view.
     * 
     * @since 1.00
     */
    private void makeActions() {
        checkoutAction = new Action() {
            public void run() {
                replayAllButtonSelected(false);
            }
        };
        checkoutAction.setText("Retrieve all solutions for review");
        checkoutAction
            .setToolTipText("Loads all specific solutions for a task/exercise "
                + "from the server for review");
        ImageDescriptor imgDescriptor = 
            Activator.getImageDescriptor("icons/replay.gif");
        checkoutAction.setImageDescriptor(imgDescriptor);

        submitAllAction = new Action() {
            public void run() {
                ReviewUtils.submitAllProjects();
            }
        };
        submitAllAction.setText("Submit all solutions");
        submitAllAction
            .setToolTipText("Submits all reviewed solutions");
        imgDescriptor = 
            Activator.getImageDescriptor("icons/submit.gif");
        submitAllAction.setImageDescriptor(imgDescriptor);
        
        checkoutReplayReviewAction = new Action() {
            public void run() {
                replayAllButtonSelected(true);
            }
        };
        checkoutReplayReviewAction.setText(
            "Retrieve all solutions (replay and review)");
        checkoutReplayReviewAction
            .setToolTipText("Loads all specific solutions for a task/exercise "
                + "from the server");
        imgDescriptor = 
            Activator.getImageDescriptor("icons/replayAndReview.gif");
        checkoutReplayReviewAction.setImageDescriptor(imgDescriptor);
    }

    /**
     * Passing the focus request to the viewer's control.
     * 
     * @since 1.00
     */
    public void setFocus() {
        viewer.getControl().setFocus();
    }
}