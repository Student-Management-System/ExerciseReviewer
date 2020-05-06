package de.uni_hildesheim.sse.exerciseReviewer.eclipse.decorators;


import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Defines an overlay image descriptor, a proxy providing
 * the overlay of an image with another image (descriptor).
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class OverlayImageDescriptor extends CompositeImageDescriptor {

    /**
     * Stores the base image.
     * 
     * @since 1.00
     */
    private Image baseImage;

    /**
     * Stores the size of the base image.
     * 
     * @since 1.00
     */
    private Point size;
    
    /**
     * Stores the overlay image.
     * 
     * @since 1.00
     */
    private ImageDescriptor overlay;
    
    /**
     * Stores the position of the overlay.
     * 
     * @since 1.00
     */
    private Position position;
    
    /**
     * Defines valid overlay positions according
     * to the Eclipse conventions.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    public enum Position {
        
        /**
         * Defines the top left position.
         * 
         * @since 1.00
         */
        TOP_LEFT,
        
        /**
         * Defines the bottom right position.
         * 
         * @since 1.00
         */
        BOTTOM_RIGHT;
    }
    
    /**
     * Creates a new overlay image descriptor.
     * 
     * @param baseImage defines the base image
     * @param overlay defines the overlay image (descriptor)
     * @param position the fines the position of <code>overlay</code>
     *        relative to <code>baseImage</code>
     * 
     * @since 1.00
     */
    public OverlayImageDescriptor(Image baseImage, ImageDescriptor overlay, 
        Position position) {
        this.baseImage = baseImage;
        Rectangle rSize = baseImage.getBounds();
        size = new Point(rSize.width, rSize.height);
        this.overlay = overlay;
        this.position = position;
    }

    /**
     * Draw the composite images.
     *
     * @param width the width
     * @param height the height
     * 
     * @since 2.00
     */
    @Override
    protected void drawCompositeImage(int width, int height) {
        ImageData overlayImageData = overlay.getImageData();
        int xValue = 0;
        int yValue = 0;
        
        switch (position) {
        case TOP_LEFT:
            xValue = 0;
            yValue = 0;
            break;
        case BOTTOM_RIGHT:
            xValue = size.x - overlayImageData.width;
            yValue = size.y - overlayImageData.height;
            break;
        default:
            xValue = 0;
            yValue = 0;
            break;
        }
        drawImage(baseImage.getImageData(), 0, 0); 
        drawImage(overlayImageData, xValue, yValue);
    }

    /**
     * Return the size of this composite image.
     *
     * @return the x and y size of the image expressed as a 
     *   point object
     *   
     * @since 2.00
     */
    @Override
    protected Point getSize() {
        return size;
    }
    
    /**
     * Get the image formed by overlaying different images on the base image.
     * 
     * @return composite image
     * 
     * @since 2.00
     */ 
    public Image getImage() {
        return createImage();
    }


}
