package de.uni_hildesheim.sse.exerciseReviewer.eclipse.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.uni_hildesheim.sse.exerciseLib.RealUser;
import de.uni_hildesheim.sse.exerciseReviewer.eclipse.Utils;
import de.uni_hildesheim.sse.exerciseSubmitter.eclipse.util.GuiUtils;

/**
 * Provides an editor for user information.
 * 
 * @author Holger Eichelberger
 * @since 1.08
 * @version 1.08
 */
public class EditRealUser {

    /**
     * Stores the shell of this dialog.
     * 
     * @since 1.08
     */
    private Shell sh;
    
    /**
     * Stores the user object.
     * 
     * @since 1.08
     */
    private RealUser user;
    
    /**
     * Stores the input field for the name of the user.
     * 
     * @since 1.08
     */
    private Text name;

    /**
     * Stores the input field for the account of the user.
     * 
     * @since 1.08
     */
    private Text account;

    /**
     * Stores the input field for the email of the user.
     * 
     * @since 1.08
     */
    private Text email;

    /**
     * Stores the input field for the group of the user.
     * 
     * @since 1.08
     */
    private Text group;

    /**
     * Creates and opens the editor window.
     * 
     * @param shell the parent shell
     * @param user the user to be modified (may be <b>null</b> if
     *        a new user creation is requested).
     * 
     * @since 1.08
     */
    public EditRealUser(final Shell shell, RealUser user) {
        this.user = user;
        sh = new Shell(shell);
        if (null == user) {
            sh.setText("New User");
        } else {
            sh.setText("Edit User");
        }
        buildLabel();
        buildInput();

        Button ok = new Button(sh, SWT.None);
        ok.setText("Ok");
        ok.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                okayAction();
            }
        });
        ok.setBounds(45, 152, 100, 25);
        
        Button cancel = new Button(sh, SWT.None);
        cancel.setText("Cancel");
        cancel.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                EditRealUser.this.user = null;
                sh.close();
            }
        });
        cancel.setBounds(185, 152, 100, 25);
        
        sh.setDefaultButton(ok);
        sh.pack();
        sh.setBounds(700, 25, 360, 210);
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
        
        // Label "Group"
        Label group = new Label(sh, SWT.None);
        group.setText("Submission group:");
        group.setBounds(12, 12, 140, 14);
        
        // Label "Name"
        Label name = new Label(sh, SWT.None);
        name.setText("Name:");
        name.setBounds(12, 42, 140, 14);
        
        // Label "System account"
        Label account = new Label(sh, SWT.None);
        account.setText("System account (optional):");
        account.setBounds(12, 72, 140, 14);
        
        // Label "Email"
        Label email = new Label(sh, SWT.None);
        email.setText("Email (optional):");
        email.setBounds(12, 102, 140, 14);
    }
    
    /**
     * Builds up the input elements of the dialog.
     * 
     * @since 1.08
     */
    private void buildInput() {
        
        group = new Text(sh, SWT.BORDER);
        if (null != user) {
            group.setText(Utils.eliminateNull(user.getGroup()));
        }
        group.setBounds(160, 12, 180, 18);
        
        name = new Text(sh, SWT.BORDER);
        if (null != user) {
            name.setText(Utils.eliminateNull(user.getName()));
        }
        name.setBounds(160, 42, 180, 18);
        
        account = new Text(sh, SWT.BORDER);
        if (null != user) {
            account.setText(Utils.eliminateNull(user.getSystemAccount()));
        }
        account.setBounds(160, 72, 180, 18);
        
        email = new Text(sh, SWT.BORDER);
        if (null != user) {
            email.setText(Utils.eliminateNull(user.getEmail()));
        }
        email.setBounds(160, 102, 180, 18);
    }
    
    /**
     * Returns the result of the user actions.
     * 
     * @return the resulting user object or <b>null</b> 
     *     if the dialog was cancelled
     * 
     * @since 1.08
     */
    public RealUser getRealUser() {
        return user;
    }

    /**
     * Called when "Ok" was pressed.
     * 
     * @since 1.08
     */
    private void okayAction() {
        boolean error = false;
        if (0 == name.getText().length()) {
            GuiUtils.openDialog(GuiUtils.DialogType.ERROR, "No name provided");
            error = true;
        }
        
        if (0 == group.getText().length()) {
            GuiUtils.openDialog(GuiUtils.DialogType.ERROR, "No group provided");
            error = true;
        }
        if (!error) {
            String systemAccount = null;
            if (0 != account.getText().length()) {
                systemAccount = account.getText();
            }
            
            String emailAddress = RealUser.UNSPECIFIED_EMAIL;
            if (0 != email.getText().length()) {
                emailAddress = email.getText();
            }

            if (null == user) {
                user = new RealUser();
            } 
            user.setData(name.getText(), 
                emailAddress, 
                group.getText(), 
                systemAccount);
            sh.dispose();
        }
    }
    
}
