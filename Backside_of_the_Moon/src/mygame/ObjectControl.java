/**
 * Authors: Jitong Xian, Xinming Shen, Zichen Fu
 * Object control class of game. 
 */

package mygame;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 * @author normenhansen
 */
public class ObjectControl extends AbstractControl {
    
    private float rotationSpeed = 1.0f;

    /**
     * Constructor for setting rotation speed if needed.
     * 
     * @param speed the speed at which the object rotate (deg/s).
     */
    public ObjectControl(float speed) {
        this.rotationSpeed = speed;
    }

    /**
     * Updates the control logic, rotating the cake along the Y-axis. 
     * 
     * @param tpf time per frame. 
     */
    @Override
    protected void controlUpdate(float tpf) {
        // Rotate the cake along the y-axis
        if (spatial != null) {
            spatial.rotate(0, rotationSpeed * tpf, 0);  
            // This is frame rate independent.
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        //TODO: add render code
    }

    /**
     * Creates a clone of  ObjectControl for another spatial object. 
     * 
     * @param spatial the spatial to which the cloned control will be attached.
     * @return a new ObjectControl with rotationSpeed applied to the spatial.
     */
    @Override
    public ObjectControl cloneForSpatial(Spatial spatial) {
        ObjectControl control = new ObjectControl(rotationSpeed);
        control.setSpatial(spatial);
        return control;
    }
    
}
