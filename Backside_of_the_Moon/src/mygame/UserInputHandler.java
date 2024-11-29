package mygame;

import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.RawInputListener;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.input.event.TouchEvent;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;

/**
 * Handles user input for the game.
 * @author frankfu
 */
public class UserInputHandler {

    private final GameState gameState;
    private final InputManager inputManager;
    private final Camera cam;
    private final CameraNode camNode;
    private final SceneSwitchingManager sceneManager;
    private final SoundManager soundManager;

    // Movement controls
    private Vector3f walkDirection = new Vector3f(0, 0, 0);
    private Vector3f viewDirection = new Vector3f(0, 0, 1);
    private float speed = 5f;
    private boolean isSpeedUp = false;
    private boolean left = false, right = false, up = false, down = false;

    // Step sound controls
    private float stepTimer = 0f;
    private float stepInterval = 0.5f; // Interval in seconds between step sounds

    // Key check
    private boolean gotKey = false;

    public UserInputHandler(InputManager inputManager, Camera cam, SceneSwitchingManager sceneManager, CameraNode camNode, GameState gameState, SoundManager soundManager) {
        this.inputManager = inputManager;
        this.cam = cam;
        this.sceneManager = sceneManager;
        this.camNode = camNode;
        this.gameState = gameState;
        this.soundManager = soundManager;

        setupKeys();
        setupMouseListener();
    }

    private void setupKeys() {
        inputManager.addMapping("MoveLeft", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("MoveRight", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("MoveForward", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("MoveBackward", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("SpeedUp", new KeyTrigger(KeyInput.KEY_LSHIFT));
        inputManager.addMapping("SwitchScene", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Pickup", new KeyTrigger(KeyInput.KEY_F));

        inputManager.addListener(actionListener, "MoveLeft", "MoveRight", "MoveForward", "MoveBackward", "SpeedUp", "SwitchScene", "Pickup");
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
                case "MoveForward":
                    up = isPressed;
                    break;
                case "MoveBackward":
                    down = isPressed;
                    break;
                case "SpeedUp":
                    isSpeedUp = isPressed;
                    break;
                case "SwitchScene":
                    sceneManager.switchToNextScene();
                    break;
                case "Pickup":
                    gotKey = gameState.pickUpItem();
                    if (isPressed) {
                        soundManager.playSFX("pickup"); // Play pickup sound
                    }
                    break;
            }
        }
    };

    private void setupMouseListener() {
        inputManager.addRawInputListener(new RawInputListener() {
            @Override
            public void onMouseMotionEvent(MouseMotionEvent evt) {
                float mouseSensitivity = 0.3f;
                float valueX = evt.getDX() * mouseSensitivity;
                float valueY = evt.getDY() * mouseSensitivity;

                rotateCamera(valueX);
                tiltCamera(valueY);
            }

            @Override
            public void beginInput() {}

            @Override
            public void endInput() {}

            @Override
            public void onJoyAxisEvent(JoyAxisEvent evt) {}

            @Override
            public void onJoyButtonEvent(JoyButtonEvent evt) {}

            @Override
            public void onMouseButtonEvent(MouseButtonEvent evt) {}

            @Override
            public void onKeyEvent(KeyInputEvent evt) {}

            @Override
            public void onTouchEvent(TouchEvent evt) {}
        });
    }

    private void rotateCamera(float value) {
        Quaternion rotation = new Quaternion().fromAngleAxis(-value * FastMath.DEG_TO_RAD, Vector3f.UNIT_Y);
        rotation.multLocal(viewDirection);
        cam.setRotation(new Quaternion().lookAt(viewDirection, Vector3f.UNIT_Y));
    }

    private void tiltCamera(float value) {
        float mouseSensitivity = 0.2f;
        float maxPitch = FastMath.HALF_PI - 0.1f;

        Quaternion currentRotation = camNode.getLocalRotation();
        Vector3f left = camNode.getLocalRotation().mult(Vector3f.UNIT_X);

        Quaternion pitchRotation = new Quaternion().fromAngleAxis(-value * mouseSensitivity * FastMath.DEG_TO_RAD, left);
        Quaternion newRotation = pitchRotation.mult(currentRotation);

        Vector3f newDirection = newRotation.getRotationColumn(2);
        if (newDirection.y > -maxPitch && newDirection.y < maxPitch) {
            camNode.setLocalRotation(newRotation);
        }
    }

    public void firstPersonNavigationUpdate(float tpf, Node playerNode, BetterCharacterControl playerControl) {
        Vector3f modelForwardDir = playerNode.getWorldRotation().mult(Vector3f.UNIT_Z);
        Vector3f modelLeftDir = playerNode.getWorldRotation().mult(Vector3f.UNIT_X);

        walkDirection.set(0, 0, 0);
        if (left) {
            walkDirection.addLocal(modelLeftDir.mult(speed));
        }
        if (right) {
            walkDirection.addLocal(modelLeftDir.mult(speed).negate());
        }
        if (up) {
            walkDirection.addLocal(modelForwardDir.mult(speed));
        }
        if (down) {
            walkDirection.addLocal(modelForwardDir.mult(speed).negate());
        }
        if (isSpeedUp) {
            walkDirection.multLocal(2);
        }
        playerControl.setWalkDirection(walkDirection);
        playerControl.setViewDirection(viewDirection);

        // Handle step sound
        if (!walkDirection.equals(Vector3f.ZERO)) {
            stepTimer += tpf;
            if (stepTimer >= stepInterval) {
                soundManager.playSFX("step");
                stepTimer = 0f;
            }
        } else {
            stepTimer = 0f; // Reset the timer when not moving
        }
    }

    public Vector3f getWalkDirection() {
        return walkDirection;
    }

    public Vector3f getViewDirection() {
        return viewDirection;
    }

    public boolean getGotKey() {
        return gotKey;
    }
}
