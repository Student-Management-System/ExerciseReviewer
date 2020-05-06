package de.uni_hildesheim.sse.exerciseReviewer.eclipse.views;

import java.io.File;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.*;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.SWT;

import de.uni_hildesheim.sse.exerciseSubmitter.Activator;
import de.uni_hildesheim.sse.exerciseSubmitter.configuration.IConfiguration;
import de.uni_hildesheim.sse.exerciseSubmitter.eclipse.util.GuiUtils;
import de.uni_hildesheim.sse.exerciseSubmitter.submission.
    CommunicationException;
import de.uni_hildesheim.sse.exerciseLib.Review;
import de.uni_hildesheim.sse.exerciseReviewer.core.ReviewCommunication;
import de.uni_hildesheim.sse.exerciseReviewer.core.plugins.ServerAuthentication;
import de.uni_hildesheim.sse.exerciseReviewer.eclipse.ReviewUtils;
import de.uni_hildesheim.sse.exerciseReviewer.eclipse.Utils;
import de.uni_hildesheim.sse.exerciseReviewer.eclipse.dialogs.EditTask;

/**
 * A workbench view for showing all review results.
 * 
 * @author Holger Eichelberger
 * @version 1.18
 * @since 1.18
 */
public class ResultView extends ViewPart {

    /**
     * Stores the parent shell of this view.
     *
     * @since 1.18
     */
    private Shell shell;
    
    /**
     * Stores the result table.
     * 
     * @since 1.18
     */
    private Table table;
    
    /**
     * Stores the viewer of {@link #table}.
     * 
     * @since 1.18
     */
    private Viewer viewer;
    
    /**
     * Stores the review communication instance.
     * 
     * @since 1.18
     */
    private ReviewCommunication comm;
    
    /**
     * Stores the refresh data action.
     * 
     * @since 1.18
     */
    private Action refreshAction;
    
    /**
     * Stores the load data action.
     * 
     * @since 1.18
     */
    private Action loadAction;
    
    /**
     * Stores the edit data action.
     * 
     * @since 1.18
     */
    private Action editAction;
    
    /**
     * Stores the merge-with-file action.
     * 
     * @since 1.18
     */
    private Action mergeAction;

    /**
     * The constructor of this view.
     * 
     * @since 1.18
     */
    public ResultView() {
        Activator.setAsReviewer(true);
    }

    /**
     * Creates the view and initializes it and its GUI.
     * 
     * @param parent
     *            the parent UI element
     * 
     * @since 1.18
     */
    public void createPartControl(Composite parent) {
        this.shell = parent.getShell();
        
        parent.setLayout(new FillLayout());
        ScrolledComposite scrollPanel = new ScrolledComposite(
            parent, SWT.H_SCROLL | SWT.V_SCROLL);
        Composite panel = new Composite(scrollPanel, SWT.NONE);
        scrollPanel.setContent(panel);

        try {
            comm = ReviewCommunication.getInstance(
                IConfiguration.INSTANCE, null);
        } catch (CommunicationException e) {
        }
        
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        panel.setLayout(gridLayout);

        Label fileLabel = new Label(panel, SWT.NONE);
        fileLabel.setText(null != comm 
            ? comm.getReviewInstanceInformation() : "");
        
        table = new Table(panel, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);

        String[] columns = {"Submission group", "Credits", "Review", 
            "submitted to server"};

        for (int i = 0; i < columns.length; i++) {
            TableColumn tc = new TableColumn(table, SWT.LEFT);
            tc.setText(columns[i]);
        }

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumn(i).pack();
        }
        Point p = table.computeSize(800, 200);

        viewer = new TableViewer(table);
        //viewer.addSelectionChangedListener(new TaskOpenListener());
        GridData gridData = new GridData(
            SWT.BEGINNING, SWT.NORMAL, false, false);
        gridData.widthHint = p.x;
        gridData.heightHint = p.y;
        table.setLayoutData(gridData);

/*        Group group = createEditGroup(panel);
        gridData = new GridData(SWT.BEGINNING, SWT.NORMAL, true, false);
        group.setLayoutData(gridData);*/

        makeActions();
        hookContextMenu();
        contributeToActionBars();

        ResourcesPlugin.getWorkspace().addResourceChangeListener(
            new ResourceChangeListener());
        // names can be obtained from getSite().getPage().get...References()
        /*getSite().getPage().addSelectionListener(JavaUI.ID_PACKAGES,
            new WorkbenchPartSelectionListener());*/

        //panel.getShell().addListener(SWT.Deactivate, new EditorListener());
        panel.setSize(panel.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        ServerAuthentication.initialize();
        fillTable();
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
                            fillTable();
                        }

                    });
                } else if (event.getDelta() != null) {
                    Display.getDefault().syncExec(new Runnable() {

                        public void run() {
                            fillTable();
                        }

                    });
                }
            } else if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
                Display.getDefault().syncExec(new Runnable() {

                    public void run() {
                        fillTable();
                    }

                });
            }
        }
    }

    /**
     * Fills the table.
     * 
     * @since 1.18
     */
    private void fillTable() {
        table.removeAll();
        List<ReviewUtils.ProjectInfo> projects = ReviewUtils.getAllProjects();

        for (ReviewUtils.ProjectInfo info : projects) {
            TableItem item = new TableItem(table, 0);
            String userName = info.getEclipseProject().getName();
            item.setText(0, Utils.eliminateNull(userName));
            try {
                if (null != comm) {
                    Review review = comm.getReview(
                        info.getTask(), userName);
                    if (null != review) {
                        item.setText(1, String.valueOf(
                            review.getCredits()));
                        item.setText(2, Utils.eliminateNull(
                            review.getReview()));
                        item.setText(3, String.valueOf(
                            review.isSubmittedToServer()));
                    }
                }
            } catch (CommunicationException e) {
            }
        }
    }
    
    /**
     * Creates the individual action objects associated with this view.
     * 
     * @since 1.18
     */
    private void makeActions() {
        if (null == comm) {
            return;
        }
        
        editAction = new Action() {
            public void run() {
                new EditTask(shell, comm);
            }
        };
        editAction.setText("Modifies the exercise tasks");
        editAction
            .setToolTipText("Modifies the exercise tasks");
        ImageDescriptor imgDescriptor = 
            Activator.getImageDescriptor("icons/edit.gif");
        editAction.setImageDescriptor(imgDescriptor);
        editAction.setEnabled(comm.acceptsTaskModification());
        
        refreshAction = new Action() {
            public void run() {
                try {
                    comm.reloadReviews();
                } catch (CommunicationException e) {
                    GuiUtils.openDialog(
                        GuiUtils.DialogType.ERROR, e.getMessage());
                }
            }
        };
        refreshAction.setText("Refreshes the review data");
        refreshAction
            .setToolTipText("Refreshes the review data");
        imgDescriptor = 
            Activator.getImageDescriptor("icons/refresh.gif");
        refreshAction.setImageDescriptor(imgDescriptor);
        
        loadAction = new Action() {
            public void run() {
                try {
                    int count = comm.getReviewCount(
                        ReviewUtils.getTaskFromWorkspace());
                    if (count > 0) {
                        boolean overwrite = GuiUtils.openDialog(
                            GuiUtils.DialogType.CONFIRMATION, 
                            "There are stored reviews that will"
                            + " be overwritten. Continue?");
                        if (overwrite) {
                            count = 0;
                        }
                    }
                    if (0 == count) {
                        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
                        final String[] extensions = { "*.tsv" };
                        final String[] filterNames = { "Review files" };
                        dialog.setText("Load review data file");
        
                        dialog.setFilterExtensions(extensions);
                        dialog.setFilterNames(filterNames);
                        String defaultDirectory = comm.getDefaultDirectory();
                        if (null != defaultDirectory 
                            && defaultDirectory.length() > 0) {
                            dialog.setFilterPath(defaultDirectory);
                        }
                        dialog.open();
                        String file = dialog.getFileName();
                        if (null != file && file.length() > 0) {
                            comm.loadResults(dialog.getFilterPath() 
                                + File.separator + file);
                            fillTable();
                        }
                    }
                } catch (CommunicationException e) {
                    GuiUtils.handleThrowable(e);
                }
            }
        };
        loadAction.setText("Loads a new review result file");
        loadAction
            .setToolTipText("Loads and stores a new review result file");
        imgDescriptor = 
            Activator.getImageDescriptor("icons/load.gif");
        loadAction.setImageDescriptor(imgDescriptor);
        loadAction.setEnabled(comm.acceptsResultsAsFile());

    
        mergeAction = new Action() {
            public void run() {
                try {
                    FileDialog dialog = new FileDialog(shell, SWT.OPEN);
                    final String[] extensions = { "*.tsv" };
                    final String[] filterNames = { "Review files" };
                    dialog.setText("Merge review data file");
    
                    dialog.setFilterExtensions(extensions);
                    dialog.setFilterNames(filterNames);
                    String defaultDirectory = comm.getDefaultDirectory();
                    if (null != defaultDirectory 
                        && defaultDirectory.length() > 0) {
                        dialog.setFilterPath(defaultDirectory);
                    }
                    dialog.open();
                    String file = dialog.getFileName();
                    if (null != file && file.length() > 0) {
                        comm.mergeTasks(dialog.getFilterPath() 
                            + File.separator + file);
                        fillTable();
                    }
                } catch (CommunicationException e) {
                    GuiUtils.handleThrowable(e);
                }
            }
        };
        mergeAction.setText("Merges a review result file " 
            + "with the current data");
        mergeAction
            .setToolTipText("Merges a review result file with " 
            + "the current data");
        imgDescriptor = 
            Activator.getImageDescriptor("icons/merge.gif");
        mergeAction.setImageDescriptor(imgDescriptor);
        mergeAction.setEnabled(comm.acceptsTaskMerge());
    }
    
    /**
     * Hooks the context menu into the correct environment.
     * 
     * @since 1.18
     */
    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu");
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                ResultView.this.fillContextMenu(manager);
            }
        });
        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);
    }

    /**
     * Does the contribution to the relevant action bars.
     * 
     * @since 1.18
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
     * @since 1.18
     */
    private void fillLocalPullDown(IMenuManager manager) {
    }
    
    /**
     * Fills the local context menu.
     * 
     * @param manager
     *            the local menu manager
     * 
     * @since 1.18
     */
    private void fillContextMenu(IMenuManager manager) {
        if (null != editAction) {
            manager.add(editAction);
            manager.add(refreshAction);
            manager.add(loadAction);
            manager.add(mergeAction);
            manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        }
    }
  
    /**
     * Fills the local tool bar.
     * 
     * @param manager
     *            the local tool bar manager
     * 
     * @since 1.18
     */
    private void fillLocalToolBar(IToolBarManager manager) {
        if (null != editAction) {
            manager.add(editAction);
            manager.add(refreshAction);
            manager.add(loadAction);
            manager.add(mergeAction);
        }
    }

    /**
     * Passing the focus request to the viewer's control.
     * 
     * @since 1.18
     */
    public void setFocus() {
        viewer.getControl().setFocus();
    }

}