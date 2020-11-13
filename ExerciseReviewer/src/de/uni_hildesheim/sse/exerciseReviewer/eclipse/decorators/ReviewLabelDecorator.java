package de.uni_hildesheim.sse.exerciseReviewer.eclipse.decorators;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecorationContext;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelDecorator;
import org.eclipse.swt.graphics.Image;

import de.uni_hildesheim.sse.exerciseLib.Review;
import de.uni_hildesheim.sse.exerciseLib.ReviewException;
import de.uni_hildesheim.sse.exerciseReviewer.core.ReviewCommunication;
import de.uni_hildesheim.sse.exerciseReviewer.core.ReviewPublicMessage;
import de.uni_hildesheim.sse.exerciseReviewer.eclipse.ReviewUtils;
import de.uni_hildesheim.sse.exerciseSubmitter.Activator;
import de.uni_hildesheim.sse.exerciseSubmitter.configuration.IConfiguration;
import de.uni_hildesheim.sse.exerciseSubmitter.submission.
    CommunicationException;
import net.ssehub.exercisesubmitter.protocol.frontend.Assignment;

/**
 * Realizes a review label decorator showing if a project
 * was reviewed or reviewed and submitted.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class ReviewLabelDecorator extends LabelDecorator {
 
    /**
     * Defines the decorator identification as used in the manifest.
     * 
     * @since 1.00
     */
    public static final String DECORATOR_ID = "net.ssehub.ExerciseReviewer.reviewedDecorator";
    
    /**
     * Stores an image descriptor proxy for the reviewed projects.
     * 
     * @since 1.00
     */
    private static ImageDescriptor reviewed;

    /**
     * Stores the last exception produced in this decorator.
     * 
     * @since 1.12
     */
    private static ReviewException lastException = null;
    
    /**
     * Stores an image descriptor proxy for the reviewed and 
     * submitted projects.
     * 
     * @since 1.00
     */
    private static ImageDescriptor submitted;
    
    /**
     * Initializes the image descriptors.
     * 
     * @since 1.00
     */
    static {
        reviewed = Activator.getImageDescriptor("icons/reviewed_ov.gif");
        submitted = Activator.getImageDescriptor("icons/submitted_ov.gif");
    }
    
    /**
     * Stores the listeners.
     * 
     * @since 1.00
     */
    private List<ILabelProviderListener> listeners = 
        new ArrayList<ILabelProviderListener>();
    
     /**
     * Returns an image that is based on the given image,
     * but decorated with additional information relating to the state
     * of the provided element taking into account the provided context.
     * 
     * Text and image decoration updates can occur as a result of other 
     * updates within the workbench including deferred decoration by 
     * background processes. Clients should handle labelProviderChangedEvents 
     * for the given element to get the complete decoration.
     *
     * @param image the input image to decorate, or <code>null</code> if the #
     *      element has no image
     * @param element the element whose image is being decorated
     * @param context additional context information about the element being 
     *      decorated
     * @return the decorated image, or <code>null</code> if no decoration is 
     *      to be applied
     *
     * @see org.eclipse.jface.resource.CompositeImageDescriptor
     */
    public Image decorateImage(Image image, Object element, 
        IDecorationContext context) {
        return decorateImage(image, element);
    }

    /**
     * Returns a text label that is based on the given text label,
     * but decorated with additional information relating to the state
     * of the provided element taking into account the provided context.
     * 
     * Text and image decoration updates can occur as a result of other 
     * updates within the workbench including deferred decoration by 
     * background processes. Clients should handle labelProviderChangedEvents 
     * for the given element to get the complete decoration.
     *
     * @param text the input text label to decorate
     * @param element the element whose image is being decorated
     * @param context additional context information about the element 
     *      being decorated
     * @return the decorated text label, or <code>null</code> if no decoration 
     *      is to be applied
     */
    public String decorateText(String text, Object element, 
        IDecorationContext context) {
        return decorateText(text, element);
    }
    
    /**
     * Prepare the element for decoration. If it is already decorated and 
     *   ready for update return true. If decoration is pending return false.
     * 
     * @param element The element to be decorated
     * @param originalText The starting text. 
     * @param context The decoration context
     * 
     * @return boolean <code>true</code> if the decoration is ready for this 
     *      element
     */
    public boolean prepareDecoration(Object element, String originalText, 
        IDecorationContext context) {
        return true;
    }
    
    /**
     * Returns an image that is based on the given image,
     * but decorated with additional information relating to the state
     * of the provided element.
     * 
     * Text and image decoration updates can occur as a result of other 
     * updates within the workbench including deferred decoration by 
     * background processes. Clients should handle labelProviderChangedEvents 
     * for the given element to get the complete decoration.
     * 
     * @param image the input image to decorate, or <code>null</code> if the 
     *      element has no image
     * @param element the element whose image is being decorated
     * @return the decorated image, or <code>null</code> if no decoration 
     *      is to be applied
     *
     * @see org.eclipse.jface.resource.CompositeImageDescriptor
     */
    public Image decorateImage(Image image, Object element) {
        Image result = null;
        if (element instanceof IJavaProject) {
            IJavaProject project = (IJavaProject) element;
            String user = project.getElementName();
            Assignment task = ReviewUtils.getTaskFromPath(project.getResource().getLocation());
            // If task = null: Wrong workspace used -> Not in review mode
            if (null != task) {
                try {
                    ReviewCommunication comm = ReviewCommunication.getInstance(IConfiguration.INSTANCE, null);
                    Review review = comm.getReview(task.getName(), user);
                    if (null != review) {
                        ImageDescriptor id = review.isSubmittedToServer() ? submitted : reviewed;
                        OverlayImageDescriptor oid = new OverlayImageDescriptor(image, id,
                            OverlayImageDescriptor.Position.BOTTOM_RIGHT);
                        result = oid.getImage();
                    }
                } catch (ReviewException e) {
                    if (null == lastException) {
                        lastException = e;
                    }
                } catch (CommunicationException e) {
                    if (e.getMessage().equals(
                        ReviewPublicMessage.INVALID_REVIEW_DATASTRUCTURE.toString())) {
                        System.err.println("Review submitter: Erroneous or no "
                            + "local review data files in user home directory.");
                    } else {
                        e.printStackTrace();
                    }
                }
            }
        }

        return result;
    }

    /**
     * Returns a text label that is based on the given text label,
     * but decorated with additional information relating to the state
     * of the provided element.
     * 
     * Text and image decoration updates can occur as a result of other updates
     * within the workbench including deferred decoration by background 
     * processes. Clients should handle labelProviderChangedEvents for the 
     * given element to get the complete decoration.
     * 
     * @param text the input text label to decorate
     * @param element the element whose image is being decorated
     * @return the decorated text label, or <code>null</code> if no 
     *        decoration is to be applied
     */
    public String decorateText(String text, Object element) {
        return null;
    }
    
    /**
     * Adds a listener to this label provider. 
     * Has no effect if an identical listener is already registered.
     * <p>
     * Label provider listeners are informed about state changes 
     * that affect the rendering of the viewer that uses this label provider.
     * </p>
     *
     * @param listener a label provider listener
     */
    public void addListener(ILabelProviderListener listener) {
        listeners.add(listener);
    }

    /**
     * Disposes of this label provider.  When a label provider is
     * attached to a viewer, the viewer will automatically call
     * this method when the viewer is being closed.  When label providers
     * are used outside of the context of a viewer, it is the client's
     * responsibility to ensure that this method is called when the
     * provider is no longer needed.
     */
    public void dispose() {
    }

    /**
     * Returns whether the label would be affected 
     * by a change to the given property of the given element.
     * This can be used to optimize a non-structural viewer update.
     * If the property mentioned in the update does not affect the label,
     * then the viewer need not update the label.
     *
     * @param element the element
     * @param property the property
     * @return <code>true</code> if the label would be affected,
     *    and <code>false</code> if it would be unaffected
     */
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    /**
     * Removes a listener to this label provider.
     * Has no affect if an identical listener is not registered.
     *
     * @param listener a label provider listener
     */
    public void removeListener(ILabelProviderListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Stores the last exception occurred and cleans the 
     * internal store for exceptions.
     * 
     * @return the last exception
     * 
     * @since 1.12
     */
    public static ReviewException getLastException() {
        ReviewException last = lastException;
        lastException = null;
        return last;
    }

}
