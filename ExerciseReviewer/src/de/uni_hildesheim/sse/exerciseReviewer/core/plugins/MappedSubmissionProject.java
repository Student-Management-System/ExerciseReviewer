package de.uni_hildesheim.sse.exerciseReviewer.core.plugins;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import de.uni_hildesheim.sse.exerciseSubmitter.eclipse.util.ISubmissionProject;

/**
 * Implements a project with mapped name for reviewer submissions/replay.
 * 
 * @author Holger Eichelberger
 * @since 2.00
 * @version 2.00
 */
class MappedSubmissionProject extends ISubmissionProject {

    /**
     * Stores the submission project to be mapped.
     * 
     * @since 2.00
     */
    private ISubmissionProject project;

    /**
     * Stores mapped name.
     * 
     * @since 2.00
     */
    private String name;
    
    /**
     * Creates a new mapped submission project.
     * 
     * @param name the new name for the mapping
     * @param project the mapped project
     * 
     * @since 2.00
     */
    public MappedSubmissionProject(String name, ISubmissionProject project) {
        this.project = project;
        this.name = name;
    }
    
    /**
     * Returns the name of the project.
     * 
     * @return the name of the project
     * 
     * @since 2.00
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the entire OS path to the project.
     * 
     * @return the path to the project
     * 
     * @since 2.00
     */
    public String getPath() {
        return project.getPath();
    }
    
    /**
     * Returns the associated eclipse project instance.
     * 
     * @return the associated eclipse project instance
     * 
     * @since 2.00
     */
    public IProject getProject() {
        return project.getProject();
    }

    /**
     * Returns an Eclipse resource object to the specified
     * relative path or file.
     * 
     * @param path a relative path or file within the project
     * 
     * @return the resource object representing <code>path</code>
     *         or <b>null</b> if the element dies not exist
     *         
     * @since 2.00
     */
    public IResource getResource(String path) {
        return project.getResource(path);
    }

    /**
     * Returns the root resource of this project.
     * 
     * @return the root resource of this project
     * 
     * @since 2.00
     */
    public IResource getResource() {
        return project.getResource();
    }

    /**
     * Creates a marker object (of a plugin local type) for the
     * specified project relative file or path.
     * 
     * @param type the type of the marker
     * @param path the file or path to create the marker for
     * 
     * @return the marker object or <b>null</b> if the marker cannot
     *         be created
     *         
     * @since 2.00
     */
    public IMarker createMarker(String type, String path) {
        return project.createMarker(type, path);
    }

    /**
     * Removes all marker objects on this project created by this
     * plugin.
     * 
     * @since 2.00
     */
    public void clearAllMarker() {
        project.clearAllMarker();
    }

    /**
     * Refreshes, i.e. synchronizes this project with the file system.
     * 
     * @since 2.00
     */
    public void refresh() {
        project.refresh();
    }

    /**
     * Refreshes, i.e. synchronizes the top-level path of this project with 
     * the file system.
     * 
     * @since 2.00
     */
    public void refreshTopLevel() {
        project.refreshTopLevel();
    }
    
    /**
     * Returns the specified value from the project preferences
     * (if available).
     * 
     * @param qualifiedName the qualified name of the preference
     * @return the value or <b>null</b> if no value can be 
     *         retrieved for the specified <code>qualifiedName</code>
     * 
     * @since 2.00
     */
    public Object getPreference(String qualifiedName) {
        return project.getPreference(qualifiedName);
    }

}
