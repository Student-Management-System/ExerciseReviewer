package de.uni_hildesheim.sse.exerciseReviewer.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.uni_hildesheim.sse.exerciseReviewer.core.plugins.
    FileReviewCommunication;
import de.uni_hildesheim.sse.exerciseReviewer.core.plugins.StudentManagementCommunication;
import de.uni_hildesheim.sse.exerciseSubmitter.submission.
    CommunicationException;

/**
 * Defines the basic class for review plugins. This version
 * does not provide dynamic plugin loading, but this version is
 * prepared.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public abstract class ReviewPlugin {

    /**
     * Stores all registered plugin instances.
     * 
     * @since 1.00
     */
    private static final List<ReviewPlugin> PLUGINS = new ArrayList<ReviewPlugin>();

    /**
     * Register the default plugins.
     * 
     * @since 1.00
     */
    static {
        register(StudentManagementCommunication.PLUGIN);
        register(FileReviewCommunication.PLUGIN);
    }
    
    /**
     * Returns the name of the protocol implemented by this class. 
     * The string will be considered when reading the communication 
     * based submission configuration.
     * <i>Note:</i> Currently no mechanism for avoiding duplicate 
     * protocol names is realized.
     * 
     * @return the name of the implemented protocol
     * 
     * @since 1.00
     */
    public abstract String getProtocol();

    /**
     * Creates an instance of the described review communication
     * class.
     * 
     * @param userName the name of the user which will communicate with a
     *        concrete communication server
     * @param password the password of <code>username</code>
     * @return the created instance
     * 
     * @throws CommunicationException if any (wrapped) communication
     *         error occurs
     * 
     * @since 1.00
     */
    public abstract ReviewCommunication createInstance(String userName,
            String password) throws CommunicationException;

    /**
     * Registers a given plugin instance. An instance will not be registered
     * if one of the previously registered instances is of the same class.
     * 
     * @param plugin the plugin instance to be registered
     * @return <code>true</code> if the plugin was registered, 
     *         <code>false</code> else
     * 
     * @since 1.00
     */
    private static boolean register(ReviewPlugin plugin) {
        boolean found = false;
        for (ReviewPlugin pl : PLUGINS) {
            if (pl.getClass() == plugin.getClass()) {
                found = true;
                break;
            }
        }
        
        boolean result;
        if (found) {
            result = false;
        } else {
            PLUGINS.add(plugin);
            result = true;
        }
        return result;
    }

    /**
     * Returns all plugins as iterator.
     * 
     * @return all plugins as (unmodifiable) iterator
     * 
     * @since 1.00
     */
    public static Iterable<ReviewPlugin> getPlugins() {
        return Collections.unmodifiableList(PLUGINS);
    }

}
