package de.uni_hildesheim.sse.exerciseReviewer.eclipse.dialogs;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.uni_hildesheim.sse.exerciseReviewer.core.ReviewCommunication;
import de.uni_hildesheim.sse.exerciseReviewer.eclipse.ReviewUtils;
import de.uni_hildesheim.sse.exerciseSubmitter.eclipse.util.GuiUtils;
import de.uni_hildesheim.sse.exerciseSubmitter.eclipse.util.GuiUtils.DialogType;
import de.uni_hildesheim.sse.exerciseSubmitter.submission.
    CommunicationException;

/**
 * Provides an editor for user information.
 * 
 * @author Holger Eichelberger
 * @since 1.08
 * @version 1.08
 */
public class EditTask {

    /**
     * Stors if the data in this dialog was modified.
     * 
     * @since 1.08
     */
    private boolean modified = false;
    
    /**
     * Stores the shell of this dialog.
     * 
     * @since 1.08
     */
    private Shell sh;
    
    /**
     * Stores the review data.
     * 
     * @since 1.08
     */
    private ReviewCommunication comm;
    
    /**
     * Stores the list of tasks.
     * 
     * @since 1.08
     */
    private List tasks;

    /**
     * Stores the view of <code>tasks</code>.
     * 
     * @since 1.08
     */
    private ListViewer viewer;
    
    /**
     * Stores the input field for the name of the task.
     * 
     * @since 1.08
     */
    private Text name;

    /**
     * Stores the input field for the credits of the task.
     * 
     * @since 1.08
     */
    private Text credits;

    /**
     * Creates and opens the editor window.
     * 
     * @param shell the parent shell
     * @param comm the review communication carrying the data
     * 
     * @since 1.08
     */
    public EditTask(final Shell shell, ReviewCommunication comm) {
        this.comm = comm;
        sh = new Shell(shell);
        sh.setText("Edit Task");
        buildLabel();
        buildInput();

        Button ok = new Button(sh, SWT.None);
        ok.setText("Close");
        ok.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                sh.dispose();
            }
        });
        ok.setBounds(155, 202, 100, 25);

        Button remove = new Button(sh, SWT.None);
        remove.setText("Remove");
        remove.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                removeAction();
            }
        });
        remove.setBounds(350, 50, 60, 25);

        Button add = new Button(sh, SWT.None);
        add.setText("Add/Store");
        add.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                addAction();
            }
        });
        add.setBounds(350, 152, 60, 25);
        
        sh.setDefaultButton(ok);
        sh.pack();
        sh.setBounds(400, 25, 430, 260);
        sh.open();
        while (!sh.isDisposed()) {
            if (!sh.getDisplay().readAndDispatch()) {
                sh.getDisplay().sleep();
            }
        }
    }
    
    /**
     * Builds up the labels of the dialog.
     * 
     * @since 1.08
     */
    private void buildLabel() {
        
        // Label "Tasks"
        Label tasks = new Label(sh, SWT.None);
        tasks.setText("Tasks:");
        tasks.setBounds(12, 12, 140, 14);
        
        // Label "Name"
        Label name = new Label(sh, SWT.None);
        name.setText("Name:");
        name.setBounds(12, 142, 140, 14);
        
        // Label "System account"
        Label credits = new Label(sh, SWT.None);
        credits.setText("Maximum credits:");
        credits.setBounds(12, 172, 140, 14);
    }

    /**
     * Fills the task list.
     * 
     * @since 1.08
     */
    private void fillList() {
        tasks.removeAll();
        try {
            for (String task : comm.getAllKnownTasks()) {
                tasks.add(task);
            }
        } catch (CommunicationException e) {
            GuiUtils.handleThrowable(e);
        }
    }
    
    /**
     * Builds up the input elements of the dialog.
     * 
     * @since 1.08
     */
    private void buildInput() {
        
        tasks = new List(sh, SWT.BORDER);
        fillList();
        tasks.setBounds(160, 12, 180, 100);
        
        viewer = new ListViewer(tasks);
        viewer.addSelectionChangedListener(new TaskChangedListener());

        name = new Text(sh, SWT.BORDER);
        name.setBounds(160, 142, 180, 18);
        
        credits = new Text(sh, SWT.BORDER);
        credits.setBounds(160, 172, 180, 18);
    }
    
    /**
     * A listener to be notified when the task selection changed.
     * 
     * @author Holger Eichelberger
     * @since 1.08
     * @version 1.08
     */
    private class TaskChangedListener implements ISelectionChangedListener {

        /**
         * Called when a task changes.
         * 
         * @param event the selection event
         * 
         * @since 1.08
         */
        public void selectionChanged(SelectionChangedEvent event) {
            String[] selection = tasks.getSelection();
            if (selection.length > 0) {
                try {
                    credits.setText(String.valueOf(
                        comm.getMaximumCredits(selection[0])));
                    name.setText(selection[0]);
                } catch (CommunicationException e) {
                    GuiUtils.handleThrowable(e);
                }
            }
        }
        
    }

    /**
     * Called when "Remove" was pressed.
     * 
     * @since 1.08
     */
    private void removeAction() {
        String[] selection = tasks.getSelection();
        if (selection.length > 0) {
            if (ReviewUtils.getTaskFromWorkspace().equals(selection[0])) {
                GuiUtils.openDialog(DialogType.CONFIRMATION, 
                    "Cannot remove the task which is currently open " 
                    + "as workspace.");
            } else {
                if (GuiUtils.openDialog(DialogType.CONFIRMATION, 
                    "Really delete exercise task and all stored reviews?")) {
                    try {
                        comm.deleteTask(selection[0]);
                        fillList();
                        modified = true;
                    } catch (CommunicationException e) {
                        GuiUtils.handleThrowable(e);
                    }
                }
            }
        }
    }
    
    /**
     * Called when "Add/Store" was pressed.
     * 
     * @since 1.08
     */
    private void addAction() {
        boolean error = false;
        if (0 == name.getText().length()) {
            GuiUtils.openDialog(GuiUtils.DialogType.ERROR, "No name provided");
            error = true;
        } else {
            try {
                if (comm.getAllKnownTasks().contains(name.getText())) {
                    GuiUtils.openDialog(
                        GuiUtils.DialogType.ERROR, "Task exists!");
                    error = true;
                }
            } catch (CommunicationException e) {
                GuiUtils.handleThrowable(e);
                error = true;
            }
        }
        
        int creditsValue = 0;
        try {
            creditsValue = Integer.parseInt(credits.getText());
            if (creditsValue <= 0) {
                GuiUtils.openDialog(
                    GuiUtils.DialogType.ERROR, "Credits is not positive.");
                error = true;
            }
        } catch (NumberFormatException e) {
            GuiUtils.openDialog(
                GuiUtils.DialogType.ERROR, "Credits is not an integer.");
            error = true;
        }
        
        if (!error) {
            try {
                comm.modifyTask(name.getText(), creditsValue);
                fillList();
                modified = true;
            } catch (CommunicationException e) {
                GuiUtils.handleThrowable(e);
                error = true;
            }
        }
        
    }
    
    /**
     * Returns if data was modified in this dialog.
     * 
     * @return <code>true</code> if data was modified, 
     *         <code>false</code> else
     * 
     * @since 1.08
     */
    public boolean wasModified() {
        return modified;
    }
    
}
