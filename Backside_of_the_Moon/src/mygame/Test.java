package mygame;
import com.jme3.animation.*;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;

public class Test extends SimpleApplication implements ActionListener {

    private BulletAppState bulletAppState;
    private CharacterControl playerControl;
    private Vector3f walkDirection = new Vector3f();
    private boolean left = false, right = false, up = false, down = false;
    private AnimChannel armChannel;
    private AnimControl armControl;

    public static void main(String[] args) {
        Test app = new Test();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Initialize physics
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        assetManager.registerLocator("assets/", FileLocator.class);

        // Load the scene
        Spatial scene = assetManager.loadModel("Models/NoDeskClassroom/noDeskClassroom.gltf");
        scene.addControl(new RigidBodyControl(0));
        bulletAppState.getPhysicsSpace().add(scene);
        rootNode.attachChild(scene);

        // Setup first-person arms
        setupArms();

        // Setup player
        setupPlayer();

        // Setup camera
        cam.setLocation(new Vector3f(0, 1.8f, 0));

        // Setup controls
        setupKeys();
    }

    private void setupArms() {
        // Load arm model
        Node armNode = (Node) assetManager.loadModel("Models/Hands/arms.glb");
        armNode.setLocalTranslation(0, 1.5f, 1); // Adjust position in front of the camera
        if (armNode == null) {
            System.out.println("Error: Arm model could not be loaded.");
            return;
        }
        rootNode.attachChild(armNode);
        
        // Setup animation
        armControl = armNode.getControl(AnimControl.class);
        if (armControl != null) {
            for (String anim : armControl.getAnimationNames()) {
                System.out.println(anim);  // Print available animations
            }
            armChannel = armControl.createChannel();
            // armChannel.setAnim("Relax_hands_idle_loop"); // Default animation
        } else {
            System.out.println("Warning: AnimControl not found on armNode.");
        }
    }

    private void setupPlayer() {
        // Create player control
        CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f, 6f, 1);
        playerControl = new CharacterControl(capsuleShape, 0.05f);
        playerControl.setJumpSpeed(20);
        playerControl.setFallSpeed(30);
        playerControl.setGravity(30);
        playerControl.setPhysicsLocation(new Vector3f(0, 10, 0));
        bulletAppState.getPhysicsSpace().add(playerControl);
    }

    private void setupKeys() {
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));

        inputManager.addListener(this, "Left", "Right", "Up", "Down", "Jump");
    }

    @Override
    public void onAction(String binding, boolean value, float tpf) {
        if (binding.equals("Left")) {
            left = value;
        } else if (binding.equals("Right")) {
            right = value;
        } else if (binding.equals("Up")) {
            up = value;
        } else if (binding.equals("Down")) {
            down = value;
        } else if (binding.equals("Jump")) {
            playerControl.jump();
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
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
        playerControl.setWalkDirection(walkDirection);
        cam.setLocation(playerControl.getPhysicsLocation());

        // Update animation based on movement
        if (walkDirection.length() == 0) {
            armChannel.setAnim("Idle", 0.5f);
        } else {
            armChannel.setAnim("Walk", 0.5f);
        }
    }
}
