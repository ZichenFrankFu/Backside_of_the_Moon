package mygame;

import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

/**
 *
 * @author frankfu
 */
public class UserInputHandler {
    private Vector3f walkDirection = new Vector3f();
    private float speed = 5f;
    private boolean isSpeedUp = false;

    private boolean left = false, right = false, up = false, down = false;
    private final InputManager inputManager;
    private final Camera cam;
    private final SceneSwitchingManager sceneManager;

    public UserInputHandler(InputManager inputManager, Camera cam, SceneSwitchingManager sceneManager) {
        this.inputManager = inputManager;
        this.cam = cam;
        this.sceneManager = sceneManager;
        setupKeys();
    }

    private void setupKeys() {
        inputManager.addMapping("MoveLeft", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("MoveRight", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("MoveUp", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("MoveDown", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("SpeedUp", new KeyTrigger(KeyInput.KEY_LSHIFT));
        inputManager.addMapping("SwitchScene", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(switchSceneListener, "SwitchScene");

        inputManager.addListener(actionListener, "MoveLeft", "MoveRight", "MoveUp", "MoveDown", "SpeedUp");
    }

    private final ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            switch (name) {
                case "MoveLeft":
                    left = isPressed;
                    break;
                case "MoveRight":
                    right = isPressed;
                    break;
                case "MoveUp":
                    up = isPressed;
                    break;
                case "MoveDown":
                    down = isPressed;
                    break;
                case "SpeedUp":
                    isSpeedUp = isPressed;
                    break;
            }
        }
    };

    public void update(float tpf) {
        Vector3f camDir = cam.getDirection().clone().multLocal(0.6f);
        Vector3f camLeft = cam.getLeft().clone().multLocal(0.4f);
        walkDirection.set(0, 0, 0);

        if (left) {
            walkDirection.addLocal(camLeft);
        }
        if (right) {
            walkDirection.addLocal(camLeft.negate());
        }
        if (up) {
            walkDirection.addLocal(camDir);
        }
        if (down) {
            walkDirection.addLocal(camDir.negate());
        }

        if (isSpeedUp) {
            walkDirection.multLocal(2); // Double the speed when Shift is pressed
        }

        walkDirection.multLocal(speed * tpf);
        cam.setLocation(cam.getLocation().add(walkDirection));
        
        //physicsHands.setWalkDirection(walkDirection.multLocal(1));
        //physicsHands.setViewDirection(camDir);
    }
    
    // Scene switch action listener
    private final ActionListener switchSceneListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals("SwitchScene") && isPressed) {
                sceneManager.switchToNextScene();
            }
        }
    };
}
    
