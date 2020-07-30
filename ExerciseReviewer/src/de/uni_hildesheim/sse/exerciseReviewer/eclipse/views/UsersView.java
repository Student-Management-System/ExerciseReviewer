package de.uni_hildesheim.sse.exerciseReviewer.eclipse.views;

import java.io.File;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;

import de.uni_hildesheim.sse.exerciseLib.RealUser;
import de.uni_hildesheim.sse.exerciseReviewer.core.ReviewCommunication;
import de.uni_hildesheim.sse.exerciseReviewer.core.plugins.ServerAuthentication;
import de.uni_hildesheim.sse.exerciseReviewer.eclipse.Utils;
import de.uni_hildesheim.sse.exerciseReviewer.eclipse.dialogs.EditRealUser;
import de.uni_hildesheim.sse.exerciseSubmitter.Activator;
import de.uni_hildesheim.sse.exerciseSubmitter.configuration.IConfiguration;
import de.uni_hildesheim.sse.exerciseSubmitter.eclipse.util.GuiUtils;
import de.uni_hildesheim.sse.exerciseSubmitter.submission.
    CommunicationException;

/**
 * A workbench view for showing and modifying assigned users.
 * 
 * @author Holger Eichelberger
 * @version 1.18
 * @since 1.18
 */
public class UsersView extends ViewPart {

    /**
     * Stores the parent shell of this dialog.
     * 
     * @since 1.18
     */
    private Shell shell;

    /**
     * Stores the users table.
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
     * Stores the review data instance.
     * 
     * @since 1.18
     */
    private ReviewCommunication comm;

    /**
     * Stores the delete user action.
     * 
     * @since 1.18
     */
    private Action deleteAction;

    /**
     * Stores the add user action.
     * 
     * @since 1.18
     */
    private Action addAction;

    /**
     * Stores the edit user action.
     * 
     * @since 1.18
     */
    private Action editAction;

    /**
     * Stores the load data action.
     * 
     * @since 1.18
     */
    private Action loadAction;

    /**
     * Stores the refresh data action.
     * 
     * @since 1.18
     */
    private Action refreshAction;
    
    /**
     * The constructor of this view.
     * 
     * @since 1.18
     */
    public UsersView() {
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
        shell = parent.getShell();
        
        parent.setLayout(new FillLayout());
        ScrolledComposite scrollPanel = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
        Composite panel = new Composite(scrollPanel, SWT.NONE);
        scrollPanel.setContent(panel);

        try {
            comm = ReviewCommunication.getInstance(IConfiguration.INSTANCE, null);
        } catch (CommunicationException e) {
        }
        
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        panel.setLayout(gridLayout);

        Label fileLabel = new Label(panel, SWT.NONE);
        fileLabel.setText(null != comm ? comm.getUserInstanceInformation() : "");
        
        table = new Table(panel, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);

        String[] columns = {"Submission group", "User (name and firstname)", "System account (optional)", "email"};

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
        GridData gridData = new GridData(SWT.BEGINNING, 
            SWT.NORMAL, false, false);
        gridData.widthHint = p.x;
        gridData.heightHint = p.y;
        table.setLayoutData(gridData);

/*        Group group = createEditGroup(panel);
        gridData = new GridData(SWT.BEGINNING, SWT.NORMAL, true, false);
        group.setLayoutData(gridData);*/

        makeActions();
        hookContextMenu();
        contributeToActionBars();

        /*ResourcesPlugin.getWorkspace().addResourceChangeListener(
            new ResourceChangeListener());*/
        // names can be obtained from getSite().getPage().get...References()
        /*getSite().getPage().addSelectionListener(JavaUI.ID_PACKAGES,
            new WorkbenchPartSelectionListener());*/

        //panel.getShell().addListener(SWT.Deactivate, new EditorListener());
        panel.setSize(panel.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        ServerAuthentication.initialize();
        fillTable();
    }
    
    /**
     * Fills the table.
     * 
     * @since 1.18
     */
    private void fillTable() {
        table.removeAll();
        List<RealUser> users;
        try {
            if (null != comm) {
                comm.reloadUsers();
                users = comm.getAllKnownUsers();
                for (RealUser user : users) {
                    TableItem item = new TableItem(table, 0);
                    setTableItem(item, user);
                }
            }
        } catch (CommunicationException e) {
        }
    }
    
    /**
     * Changes a given table item with the data from the given user.
     * 
     * @param item the item to be modified
     * @param user the user carrying the new data
     * 
     * @since 1.18
     */
    private void setTableItem(TableItem item, RealUser user) {
        item.setText(0, Utils.eliminateNull(user.getGroup()));
        item.setText(1, Utils.eliminateNull(user.getName()));
        item.setText(2, Utils.eliminateNull(user.getSystemAccount()));
        item.setText(3, Utils.eliminateNull(user.getEmail()));
        item.setData(user);
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
        
        deleteAction = new Action() {
            public void run() {
                TableItem[] selection = table.getSelection();
                for (int i = 0; i < selection.length; i++) {
                    try {
                        comm.deleteUser((RealUser) selection[i].getData());
                        fillTable();
                    } catch (CommunicationException e) {
                        GuiUtils.handleThrowable(e);
                    }
                }
            }
        };
        deleteAction.setText("Deletes the selected user");
        deleteAction
            .setToolTipText("Deletes the selected user");
        ImageDescriptor imgDescriptor = 
            Activator.getImageDescriptor("icons/delete.gif");
        deleteAction.setImageDescriptor(imgDescriptor);
        deleteAction.setEnabled(comm.acceptsUserModification());

        addAction = new Action() {
            public void run() {
                EditRealUser edit = new EditRealUser(shell, null);
                if (null != edit.getRealUser()) {
                    try {
                        comm.modifyUser(edit.getRealUser());
                        fillTable();
                    } catch (CommunicationException e) {
                        GuiUtils.handleThrowable(e);
                    }
                }
            }
        };
        addAction.setText("Adds a new user");
        addAction
            .setToolTipText("Adds a new user");
        imgDescriptor = 
            Activator.getImageDescriptor("icons/add.gif");
        addAction.setImageDescriptor(imgDescriptor);
        addAction.setEnabled(comm.acceptsUserModification());
        
        editAction = new Action() {
            public void run() {
                TableItem[] item = table.getSelection();
                for (int i = 0; i < item.length; i++) {
                    EditRealUser edit = new EditRealUser(shell, 
                        (RealUser) item[i].getData());
                    if (null != edit.getRealUser()) {
                        try {
                            comm.modifyUser(edit.getRealUser());
                            setTableItem(item[i], edit.getRealUser());
                            //table.update();
                        } catch (CommunicationException e) {
                            GuiUtils.handleThrowable(e);
                        }
                    }
                }
            }
        };
        editAction.setText("Modifies the selected user");
        editAction
            .setToolTipText("Modifies the selected user");
        imgDescriptor = 
            Activator.getImageDescriptor("icons/edit.gif");
        editAction.setImageDescriptor(imgDescriptor);
        editAction.setEnabled(comm.acceptsUserModification());

        loadAction = new Action() {
            public void run() {
                FileDialog dialog = new FileDialog(shell, SWT.OPEN);
                final String[] extensions = { "*.users" };
                final String[] filterNames = { "User files" };
                dialog.setText("Load user data file");

                dialog.setFilterExtensions(extensions);
                dialog.setFilterNames(filterNames);
                String defaultDirectory = comm.getDefaultDirectory();
                if (null != defaultDirectory && defaultDirectory.length() > 0) {
                    dialog.setFilterPath(defaultDirectory);
                }
                dialog.open();
                String file = dialog.getFileName();
                if (null != file && file.length() > 0) {
                    try {
                        comm.loadUsers(dialog.getFilterPath() 
                            + File.separator + file);
                        fillTable();
                    } catch (CommunicationException e) {
                        GuiUtils.handleThrowable(e);
                    }
                }
            }
        };
        loadAction.setText("Loads a new user file");
        loadAction
            .setToolTipText("Loads and stores a new user file");
        imgDescriptor = 
            Activator.getImageDescriptor("icons/load.gif");
        loadAction.setImageDescriptor(imgDescriptor);
        loadAction.setEnabled(comm.acceptsUsersAsFile());
        
        refreshAction = new Action() {
            public void run() {
                try {
                    comm.reloadUsers();
                } catch (CommunicationException e) {
                    GuiUtils.handleThrowable(e);
                }
            }
        };
        refreshAction.setText("Refreshes the user file");
        refreshAction
            .setToolTipText("Refreshes the user file");
        imgDescriptor = 
            Activator.getImageDescriptor("icons/refresh.gif");
        refreshAction.setImageDescriptor(imgDescriptor);
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
                UsersView.this.fillContextMenu(manager);
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
        if (null != deleteAction) {
            manager.add(deleteAction);
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
        if (null != deleteAction) {
            manager.add(addAction);
            manager.add(editAction);
            manager.add(deleteAction);
            manager.add(loadAction);
            manager.add(refreshAction);
        }
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
        if (null != deleteAction) {
            manager.add(addAction);
            manager.add(editAction);
            manager.add(deleteAction);
            manager.add(loadAction);
            manager.add(refreshAction);
            manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
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