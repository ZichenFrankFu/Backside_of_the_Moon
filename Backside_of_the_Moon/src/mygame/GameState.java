/**
 * Authors: Jitong Xian, Xinming Shen, Zichen Fu
 * Game state class of game. 
 */

package mygame;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.input.InputManager;
import com.jme3.font.BitmapText;
import java.util.List;
import java.util.ArrayList;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 * @author normenhansen
 */
public class GameState extends AbstractAppState {

    private Camera cam;
    private InputManager inputManager;
    private List<Spatial> pickableItems = new ArrayList<>();
    private Spatial aimedItem;
    private BitmapText notificationText; // Notification for item pick-up

    private boolean forward = false, backward = false, left = false, right = false, speedBoost = false;
    private float normalSpeed = 5.0f;  // Normal movement speed
    private float fastSpeed = 10.0f;   // Speed when Shift is held down
    private float currentSpeed = normalSpeed; // The current movement speed
    private float cameraHeight = 1.75f;  // Fixed camera height for walking

    /**
     * Constructor for initializing UserInput, camera, input manager, 
     * game state, and notification text.
     * 
     * @param cam The camera. 
     * @param inputManager The inputmanager that manages user inputs.
     * @param notificationText The text to notify the player about pick-ups.
     */
    public GameState(Camera cam, InputManager inputManager, BitmapText notificationText) {
        this.cam = cam;
        this.inputManager = inputManager;
        this.notificationText = notificationText;
    }
    
    /**
     * Updates the game state, handling player movement and item interaction.
     * 
     * @param tpf Time per frame. 
     */
    @Override
    public void update(float tpf) {
        // Check if the player is aiming at any items and update the notification
        checkAimedItem();
    }

    
    /**
     * Adds a pickable item to the list of pickable items. 
     * 
     * @param item The spatial object representing the pickable item.
     */
    public void addPickableItem(Spatial item) {
        pickableItems.add(item);
    }
    
    /**
     * Method to detect and pick up an item. 
     */
    public void pickUpItem() {
        if (aimedItem != null) {
            aimedItem.removeFromParent(); // Remove the item from the scene
            pickableItems.remove(aimedItem); // Remove the item from the list of pickable items
            System.out.println("Picked up: " + aimedItem.getName()); // Log the pick-up event
            aimedItem = null; // Reset the aimed item
        }
    }

    /**
     * Raycasting to check if the player is aiming at a pickable item
     */
    private void checkAimedItem() {
        // Cast a ray from the camera forward
        Ray ray = new Ray(cam.getLocation(), cam.getDirection());
        CollisionResults results = new CollisionResults();

        aimedItem = null; // Reset aimed item

        // Check all pickable items for a hit
        for (Spatial item : pickableItems) {
            if (item != null) {
                item.collideWith(ray, results);
                if (results.size() > 0) {
                    aimedItem = item; // If a collision is detected, set it as aimed item
                    break;
                }
            }
        }

        // If an item is aimed at, show the notification
        if (aimedItem != null) {
            notificationText.setText(""); // Clear existing text
            notificationText.setText("Press 'F' to pick up " + aimedItem.getName());
            notificationText.setLocalTranslation(cam.getWidth() / 2 - notificationText.getLineWidth() / 2, cam.getHeight() / 2 + 20, 0);
        } else {
            notificationText.setText(""); // No item aimed, clear notification
        }
    }

    /**
     * Initializes the game state. 
     * 
     * @param stateManager The state manager handling app states.
     * @param app The application instance.
     */
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        // Initialization code here
    }

    /**
     * Cleans up the game state. 
     */
    @Override
    public void cleanup() {
    }
    
}
