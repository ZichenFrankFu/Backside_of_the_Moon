package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.font.BitmapText;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl.ControlDirection;
import com.jme3.ui.Picture;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 * @author normenhansen
 */
public class Main extends SimpleApplication {
    // Game State
    private GameState gameState;
    
    // Basic settings
    private BulletAppState bulletAppState;
    private UserInputHandler inputHandler;
    private ModelLoader modelLoader;
    private SceneSwitchingManager sceneManager;
    private SoundManager soundManager;
        
    // FPS control
    private CameraNode camNode;
    private Node playerNode;
    private BetterCharacterControl playerControl;
    
    // UI
    private BitmapText notificationText;
    private BitmapText crosshair;
    float iconWidth = 52;
    float iconHeight = 47;


    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        
        // Settings
        this.setDisplayFps(false);
        this.setDisplayStatView(false);
        this.setShowSettings(false);
        this.inputManager.setCursorVisible(false);
        inputManager.setCursorVisible(false);
        //flyCam.setEnabled(false);
        
        // Physics
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        
        // Add gravity
        bulletAppState.getPhysicsSpace().setGravity(new Vector3f(0, -1.62f, 0));

        // Create player Node
        playerNode = new Node("the player");
        Spatial handsModel = assetManager.loadModel("Models/Hands/arms.glb");
        handsModel.scale(2f);
        playerNode.attachChild(handsModel);
        playerNode.setLocalTranslation(new Vector3f(0, 6, 0));
        rootNode.attachChild(playerNode);
        
        // Player Control
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
        
        // Scene Switch
        sceneManager = new SceneSwitchingManager(this);
        stateManager.attach(sceneManager);
        
        // Sound Switch
        soundManager = new SoundManager(assetManager);
        
        // Input Handle
        inputHandler = new UserInputHandler(inputManager, cam, sceneManager, camNode);
        
        // UI
        setNotificationText();
        gameState = new GameState(cam, inputManager, notificationText);
        stateManager.attach(gameState);
        createSaveButton();
        createLoadButton();
        createCrosshair();
        
        
        // Load Model
        modelLoader = new ModelLoader(assetManager, rootNode, bulletAppState, sceneManager, cam);
        Node classroomScene = modelLoader.loadClassroom();
        modelLoader.loadMonkey(classroomScene);
        Node blackholeScene = modelLoader.loadBlackhole();
        modelLoader.loadOto(blackholeScene);
        modelLoader.loadCakes(3, classroomScene, gameState);
        
        // Initialize the first scene
        sceneManager.switchToNextScene();

        
    }
    


    @Override
    public void simpleUpdate(float tpf) {
        inputHandler.firstPersonNavigationUpdate(tpf, playerNode, playerControl);
        
        if (sceneManager.hasSceneChanged()) {
            playSceneMusic(sceneManager.getCurrentSceneName());
        }
        

    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
    
    public void createSaveButton(){
        Picture frame = new Picture("User interface frame");
        frame.setImage(assetManager, "Interface/save.png", false); 
        frame.setWidth(iconWidth);
        frame.setHeight(iconHeight);
        frame.setPosition(5, settings.getHeight() - iconHeight - 9);
        guiNode.attachChild(frame);

        frame.getMaterial().getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
    }
    
    public void createLoadButton(){
        Picture frame2 = new Picture("Button 2");
        frame2.setImage(assetManager, "Interface/load.png", false);
        frame2.setWidth(iconWidth);
        frame2.setHeight(iconHeight);
        frame2.setPosition(iconWidth + 10, settings.getHeight() - iconHeight - 5);
        guiNode.attachChild(frame2);

        frame2.getMaterial().getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
    }
    
    private void createCrosshair() {
        crosshair = new BitmapText(guiFont, false);
        crosshair.setSize(guiFont.getCharSet().getRenderedSize());
        crosshair.setText("+");
        crosshair.setColor(ColorRGBA.White);
        float x = (cam.getWidth() / 2) - (crosshair.getLineWidth() / 2);
        float y = (cam.getHeight() / 2) + (crosshair.getLineHeight() / 2);
        crosshair.setLocalTranslation(x, y, 0);
        guiNode.attachChild(crosshair);
    }
    
    private void setNotificationText(){
        // Initialize the HUD text for showing item notifications
        notificationText = new BitmapText(guiFont, false);
        notificationText.setSize(guiFont.getCharSet().getRenderedSize());
        notificationText.setText("");
        notificationText.setColor(ColorRGBA.Red);
        guiNode.attachChild(notificationText);
    }
    
    private void playSceneMusic(String sceneName) {
        soundManager.stopCurrentBGM(); // Stop any currently playing BGM

        switch (sceneName) {
            case "ClassroomScene":
                soundManager.playBGM("quiet_bgm");
                break;
            case "BlackholeScene":
                soundManager.playBGM("mystery_bgm");
                break;
            default:
                System.out.println("No BGM mapped for scene: " + sceneName);
                break;
        }
    }

}
