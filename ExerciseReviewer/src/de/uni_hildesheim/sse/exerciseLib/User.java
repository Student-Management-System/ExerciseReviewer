package de.uni_hildesheim.sse.exerciseLib;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Represents a (repository) user name which may map to multiple users.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class User implements Comparable<User> {

    /**
     * Stores the mapped real users.
     * 
     * @since 1.00
     */
    private List<RealUser> users = new ArrayList<RealUser>();

    /**
     * Stores the user name.
     * 
     * @since 1.00
     */
    private String userName;

    /**
     * Creates a new user object.
     * 
     * @param userName
     *            the user name
     * 
     * @since 1.00
     */
    public User(String userName) {
        this.userName = userName;
    }        
    
    /**
     * Returns the user name.
     * 
     * @return the user name
     * 
     * @since 1.00
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Adds a real user to this user (group).
     * 
     * @param user
     *            the real user to be added
     * 
     * @since 1.00
     */
    public void addRealUser(RealUser user) {
        for (RealUser u : users) {
            if (u.getEmail().equals(user.getEmail())
                && u.getName().equals(user.getName())) {
                return;
            }
        }
        users.add(user);
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
     * @since 2.0
     */
    public int compareTo(User user) {
        return userName.compareTo(user.userName);
    }
    
    /**
     * Returns a list of the real user names of this 
     * user object.
     * 
     * @return the list of real user names
     * 
     * @since 1.00
     */
    public List<String> getRealUserNames() {
        List<String> result = new ArrayList<String>();
        for (RealUser user : users) {
            result.add(user.getName());
        }
        return result;
    }
    
    /**
     * Returns an iterator over all real users stored
     * in one (mapped) user.
     * 
     * @return an iterator over all real users.
     * 
     * @since 1.00
     */
    public Iterator<RealUser> getRealUsers() {
        return users.iterator();
    }

}
