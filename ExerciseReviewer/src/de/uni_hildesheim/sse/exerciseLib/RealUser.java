package de.uni_hildesheim.sse.exerciseLib;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Stores the data of a real user.
 * 
 * @author Holger Eichelberger
 * @since 1.06
 * @version 1.06
 */
public class RealUser implements Comparable<RealUser> {

    /**
     * Defines the separator string (tabulator).
     * 
     * @since 1.06
     */
    public static final String SEPARATOR = "\t";
    
    /**
     * Defines the default email to be considered as the 
     * unknown email to which emails will not be sent.
     * 
     * @since 1.06
     */
    public static final String UNSPECIFIED_EMAIL = "me@here.de";
    
    /**
     * Stores the name of the user.
     * 
     * @since 1.06
     */
    private String name;

    /**
     * Stores the email of the user.
     * 
     * @since 1.06
     */
    private String email;

    /**
     * Stores the name of the exercise 
     * submission group this user is member of.
     * 
     * @since 1.06
     */
    private String group;
    
    /**
     * Stores the related (os-specific) system 
     * account name.
     * 
     * @since 1.06
     */
    private String systemAccount;

    /**
     * Creates a new and empty real user object.
     * 
     * @since 1.08
     */
    public RealUser() {
    }
    
    /**
     * Creates a new real user object.
     * 
     * @param name
     *            the name of the user
     * @param email
     *            the email address of the user
     * @param group
     *            the name of the submission group of the user
     * @param systemAccount
     *            the related os-specific system account
     * 
     * @since 1.06
     */
    public RealUser(String name, String email, String group, 
        String systemAccount) {
        setData(name, email, group, systemAccount);
    }

    /**
     * Changes the data of the real user object.
     * 
     * @param name
     *            the name of the user
     * @param email
     *            the email address of the user
     * @param group
     *            the name of the submission group of the user
     * @param systemAccount
     *            the related os-specific system account
     * 
     * @since 1.08
     */
    public void setData(String name, String email, String group, 
        String systemAccount) {
        this.name = name;
        this.email = email;
        this.group = group;
        this.systemAccount = systemAccount;
    }

    /**
     * Returns the email of the user.
     * 
     * @return the email of the user
     * 
     * @since 1.06
     */
    public String getEmail() {
        return email;
    }
    
    /**
     * Returns if the email address of this user should be
     * ignored.
     * 
     * @return <code>true</code> if the email should be ignored,
     *         <code>false</code> else
     * 
     * @since 1.06
     */
    public boolean ignoreEmail() {
        return null == email || email.equals(UNSPECIFIED_EMAIL);
    }

    /**
     * Returns the name of the user.
     * 
     * @return the name of the user
     * 
     * @since 1.06
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the name of the exercise submission group 
     * this user is member of.
     * 
     * @return the name of the exercise submission group
     * 
     * @since 1.06
     */
    public String getGroup() {
        return group;
    }
    
    /**
     * Returns the name of the user's system
     * account (os-specific).
     * 
     * @return the name of the user's system
     * account (os-specific)
     * 
     * @since 1.06
     */
    public String getSystemAccount() {
        return systemAccount;
    }
    
    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     * 
     * @param user
     *            the object to be compared.
     * @return a negative integer, zero, or a positive integer as this
     *         object is less than, equal to, or greater than the specified
     *         object.
     * 
     * @throws ClassCastException
     *             if the specified object's type prevents it from being
     *             compared to this object.
     * 
     * @since 1.06
     */
    public int compareTo(RealUser user) {
        return name.compareTo(user.name);
    }

    /**
     * Reads all real users from a simple text file according to the following
     * structure.
     * <code>
     * submissionGroup &lt;tab&gt; user1 name 
     * [&lt;tab&gt; user 1 system account] 
     * &lt;tab&gt; user1 email ... for user n
     * </code>
     * Thereby, an email is recognized as email address if it contains 
     * a "@", otherwise the name of the system account is assumed.<br/>
     * 
     * This method is here for compatibility reasons.
     * 
     * @param usersFile
     *            the name of the file to be read
     * @return the users in <code>usersFile</code>
     * 
     * @throws IOException
     *             if any I/O error occurs
     * 
     * @since 1.06
     */
    public static List<RealUser> readRealUserList(File usersFile) 
        throws IOException {
        return readRealUserList(new FileReader(usersFile));
    }

    /**
     * Reads all real users from a simple text file according to the following
     * structure.
     * <code>
     * submissionGroup &lt;tab&gt; user1 name 
     * [&lt;tab&gt; user 1 system account] 
     * &lt;tab&gt; user1 email ... for user n
     * </code>
     * Thereby, an email is recognized as email address if it contains 
     * a "@", otherwise the name of the system account is assumed.
     * 
     * @param usersFile
     *            the name of the file to be read
     * @return the users in <code>usersFile</code>
     * 
     * @throws IOException
     *             if any I/O error occurs
     * 
     * @since 1.06
     */
    public static List<RealUser> readRealUserList(Reader usersFile)
        throws IOException {
        List<RealUser> result = new ArrayList<RealUser>();
        LineNumberReader lnis =
            new LineNumberReader(usersFile);
        String line;
        do {
            line = lnis.readLine();
            if (null != line) {
                StringTokenizer tokens = new StringTokenizer(line, 
                    SEPARATOR);
                if (tokens.hasMoreTokens()) {
                    String group = tokens.nextToken();
                    while (tokens.hasMoreTokens()) {
                        String name = nextTokenIfExists(tokens);
                        if (null != name) {
                            String systemAccount = nextTokenIfExists(tokens);
                            String email = nextTokenIfExists(tokens);
                            result.add(new RealUser(name, 
                                email, group, systemAccount)); 
                        }
                    }
                }
            }
        } while (line != null);
        lnis.close();
        return result;
    }

    /**
     * Reads the next token if it exists.
     * 
     * @param tokens the next token
     * @return the next token or <b>null</b>
     */
    private static String nextTokenIfExists(StringTokenizer tokens) {
        String result = null;
        if (tokens.hasMoreTokens()) {
            result = tokens.nextToken();
        }
        return result;
    }

    /**
     * Writes all given real users to a simple text file according 
     * to the following structure.
     * <code>
     * submissionGroup &lt;tab&gt; user1 name 
     * [&lt;tab&gt; user 1 system account] 
     * &lt;tab&gt; user1 email ... for user n
     * </code>
     * Thereby, an email is recognized as email address if it contains 
     * a "@", otherwise the name of the system account is assumed. <br/>
     * 
     * This method is here for compatibility reasons.
     * 
     * @param users the user list to be written
     * @param usersFile the file to be read
     * 
     * @throws IOException
     *             if any I/O error occurs
     * 
     * @since 1.06
     */
    public static void writeRealUserList(List<RealUser> users, 
        File usersFile) throws IOException {
        writeRealUserList(users, new FileWriter(usersFile));
    }

    /**
     * Writes all given real users to a simple text file according 
     * to the following structure.
     * <code>
     * submissionGroup &lt;tab&gt; user1 name 
     * [&lt;tab&gt; user 1 system account] 
     * &lt;tab&gt; user1 email ... for user n
     * </code>
     * Thereby, an email is recognized as email address if it contains 
     * a "@", otherwise the name of the system account is assumed.
     * 
     * @param users the user list to be written
     * @param usersFile the file to be written
     * 
     * @throws IOException
     *             if any I/O error occurs
     * 
     * @since 1.06
     */
    public static void writeRealUserList(List<RealUser> users, 
        Writer usersFile) throws IOException {
        
        Map<String, List<RealUser>> sortedUsers = rehashUsers(users);
        List<String> groups = getAllGroups(sortedUsers);

        // write data
        PrintWriter out = new PrintWriter(usersFile);
        for (String group : groups) {
            List<RealUser> groupUsers = sortedUsers.get(group);
            if (null != groupUsers) {
                out.print(group);
                out.print(SEPARATOR);
                for (RealUser user : groupUsers) {
                    out.print(user.getName());
                    out.print(SEPARATOR);
                    String sysAcc = user.getSystemAccount();
                    if (null != sysAcc && sysAcc.length() > 0) {
                        out.print(sysAcc);
                        out.print(SEPARATOR);
                    }
                    out.print(user.getEmail());
                    out.print(SEPARATOR);
                }
                out.println();
            }
        }
        out.close();
    }

    /**
     * Hashes all users so that group names are combined with assigned users.
     * 
     * @param users the user list to be considered
     * @return a map of group names and assigned users
     * 
     * @since 1.08
     */
    private static Map<String, List<RealUser>> rehashUsers(
        List<RealUser> users) {
        Map<String, List<RealUser>> sortedUsers = 
            new HashMap<String, List<RealUser>>();
        for (RealUser user : users) {
            List<RealUser> group = sortedUsers.get(user.getGroup());
            if (null == group) {
                group = new ArrayList<RealUser>();
                sortedUsers.put(user.getGroup(), group);
            }
            group.add(user);
        }
        return sortedUsers;
    }
    
    /**
     * Returns all groups in the given sorted users map.
     * 
     * @param sortedUsers a map of groups and assigned users.
     * @return the sorted list of all groups
     * 
     * @since 1.08
     */
    private static List<String> getAllGroups(Map<String, 
        List<RealUser>> sortedUsers) {
        List<String> groups = new ArrayList<String>();
        groups.addAll(sortedUsers.keySet());
        Collections.sort(groups);
        return groups;
    }
    
    /**
     * Returns all groups assigned to users in <code>users</code>.
     * 
     * @param users the user list to be considered
     * @return the list of all groups
     * 
     * @since 1.08
     */
    public static List<String> getAllGroups(List<RealUser> users) {
        return getAllGroups(rehashUsers(users));
    }

}