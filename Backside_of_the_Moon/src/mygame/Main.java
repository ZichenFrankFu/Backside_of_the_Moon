package mygame;

import com.jme3.anim.AnimComposer;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl;
import com.jme3.scene.control.CameraControl.ControlDirection;
import com.jme3.scene.shape.Box;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 * @author normenhansen
 */
public class Main extends SimpleApplication {
    private BulletAppState bulletAppState;
    private UserInputHandler inputHandler;
    private ModelLoader modelLoader;
    private SceneSwitchingManager sceneManager;
        
    private Node handsNode;
    private CameraNode camNode;
    private AnimComposer animComposer;
    private ChaseCamera chaseCam;
    private BetterCharacterControl physicsHands;
    private Node playerNode;
    private BetterCharacterControl playerControl;
    


    public static void main(String[] args) {
        Main app = new Main();
        //Test app = new Test();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        
        this.setDisplayFps(false);
        this.setDisplayStatView(false);
        this.setShowSettings(false);
        this.inputManager.setCursorVisible(false);
        
        // Physics
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        
        // Add gravity
        bulletAppState.getPhysicsSpace().setGravity(new Vector3f(0, -1.62f, 0));

        
        playerNode = new Node("the player");
        Spatial handsModel = assetManager.loadModel("Models/Hands/arms.glb");
        handsModel.scale(2f);
        playerNode.attachChild(handsModel);
        playerNode.setLocalTranslation(new Vector3f(0, 6, 0));
        rootNode.attachChild(playerNode);
        playerControl = new BetterCharacterControl(1.5f, 4, 30f);
        playerControl.setJumpForce(new Vector3f(0, 300, 0));
        playerControl.setGravity(new Vector3f(0, -10, 0));
        playerNode.addControl(playerControl);
        bulletAppState.getPhysicsSpace().add(playerControl);
        
                // Camera Node Setup
        camNode = new CameraNode("CamNode", cam);
        camNode.setControlDir(ControlDirection.SpatialToCamera);
        camNode.setLocalTranslation(new Vector3f(0, 4, -6));
        playerNode.attachChild(camNode);
        flyCam.setEnabled(false);
        
        
        
        
        
        // Scene Switch
        sceneManager = new SceneSwitchingManager(this);
        stateManager.attach(sceneManager);
        
        // Input Handle
        inputHandler = new UserInputHandler(inputManager, cam, sceneManager, camNode);
        
        
        // Load Model
        modelLoader = new ModelLoader(assetManager, rootNode, bulletAppState, sceneManager, cam);
        Node classroomScene = modelLoader.loadClassroom();
        Node monkey = modelLoader.loadMonkey(classroomScene);
        //Spatial hands = modelLoader.loadHands();
        
        Node blackholeScene = modelLoader.loadBlackhole();
        
        // Initialize the first scene
        sceneManager.switchToNextScene();
        
        
        
        animComposer = handsModel.getControl(AnimComposer.class);
        if (animComposer != null) {
            System.out.println("AnimComposer");
            animComposer.setCurrentAction("Relax_hands_idle_loop");
        } else {
            System.out.println("No AnimComposer");
        }
        
        AnimControl control = handsModel.getControl(AnimControl.class);
        AnimChannel channel;
        if (control != null) {
            for (String anim : control.getAnimationNames()) {
                System.out.println(anim);  // Print available animations
            }
        }
        
 
        
        
    }
    


    @Override
    public void simpleUpdate(float tpf) {
        inputHandler.firstPersonNavigationUpdate(tpf, playerNode, playerControl);
        

    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
    

}
