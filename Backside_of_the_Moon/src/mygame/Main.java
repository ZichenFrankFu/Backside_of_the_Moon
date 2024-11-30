package mygame;

import com.jme3.anim.AnimComposer;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.font.BitmapText;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.post.filters.DepthOfFieldFilter;
import com.jme3.post.filters.FogFilter;
import com.jme3.post.filters.LightScatteringFilter;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl.ControlDirection;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.ui.Picture;
import com.jme3.water.WaterFilter;

import java.util.List;

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
    private BitmapText textDisplay; // For displaying the text sequence
    private Picture startScreen;
    private boolean startScreenActive = true;
    private boolean textSequenceActive = false;
    
    float iconWidth = 52;
    float iconHeight = 47;

    // Monster chasing
    private Node monkeyNode;
    private BetterCharacterControl monkeyControl;
    private AnimComposer monkeyAnimComposer;
    private float monkeySpeed = 4.0f;

    // Bag check
    private boolean gotKey = false;

    // Text sequence
    private List<String> textSequence;
    private int currentTextIndex = 0;
    
    //Terrain
    private FilterPostProcessor fpp;
    private FogFilter fogFilter;
    private final Vector3f lightDir = new Vector3f(-0.39f, -0.32f, -0.74f);
    private LightScatteringFilter sunLightFilter;
    private Node reflectedScene;
    private DepthOfFieldFilter dofFilter;
    private BloomFilter bloom;
    private Node room3;

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Show start screen
        showStartScreen();

        // Register input to proceed
        inputManager.addMapping("NextText", new KeyTrigger(com.jme3.input.KeyInput.KEY_SPACE));
        inputManager.addListener(nextTextListener, "NextText");

        // Initialize text sequence
        textSequence = List.of(
                "...A faint whisper calls out to you...",
                "You might wonder why there’s no START button but CONTINUE.",
                "Perhaps... this isn’t the beginning at all.",
                "You never have a choice, really. Not your birth, not here.",
                "Just kidding",
                "Actually, you’ve just lost all your memory.",
                "We’ve been here before... you and I... countless times",
                "Now, let’s see if you can figure out why."
        );
    }

    private void showStartScreen() {
        startScreen = new Picture("Start Screen");
        startScreen.setImage(assetManager, "Textures/horror_door.jpg", true);
        startScreen.setWidth(settings.getWidth());
        startScreen.setHeight(settings.getHeight());
        startScreen.setPosition(0, 0);
        guiNode.attachChild(startScreen);
        startScreenActive = true;
    }

    private void dismissStartScreen() {
        if (startScreen != null) {
            guiNode.detachChild(startScreen);
            startScreenActive = false;
        }
    }

    private void showTextSequence() {
        textDisplay = new BitmapText(guiFont, false);
        textDisplay.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        textDisplay.setColor(ColorRGBA.White);
        guiNode.attachChild(textDisplay);
        textSequenceActive = true;

        updateTextDisplay();
    }

    private void updateTextDisplay() {
        if (currentTextIndex < textSequence.size()) {
            String text = textSequence.get(currentTextIndex);
            textDisplay.setText(text);
            textDisplay.setLocalTranslation(
                (cam.getWidth() - textDisplay.getLineWidth()) / 2,
                cam.getHeight() / 2,
                0
            );
            currentTextIndex++;
        } else {
            dismissTextSequence();
            initializeGame();
        }
    }

    private void dismissTextSequence() {
        if (textDisplay != null) {
            guiNode.detachChild(textDisplay);
            textSequenceActive = false;
        }
    }

    private void initializeGame() {
        // Settings
        this.setDisplayFps(false);
        this.setDisplayStatView(false);
        this.setShowSettings(false);
        this.inputManager.setCursorVisible(false);

        // Physics
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);

        // Add gravity
        bulletAppState.getPhysicsSpace().setGravity(new Vector3f(0, -9.8f, 0));

        // Sound Manager
        soundManager = new SoundManager(assetManager);
        soundManager.playBGM("quiet_bgm"); // Play background music for the classroom

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
        camNode.setLocalTranslation(new Vector3f(0, 4, -3));
        playerNode.attachChild(camNode);

        // Scene Switch
        sceneManager = new SceneSwitchingManager(this);
        stateManager.attach(sceneManager);

        // UI
        setNotificationText();
        gameState = new GameState(cam, inputManager, notificationText);
        stateManager.attach(gameState);
        createCrosshair();
        createSaveButton();
        createLoadButton();

        // Input Handle
        inputHandler = new UserInputHandler(inputManager, cam, sceneManager, camNode, gameState, soundManager);

        // Load Model
        modelLoader = new ModelLoader(assetManager, rootNode, bulletAppState, sceneManager, cam);
        Node classroomScene = modelLoader.loadClassroom();
        monkeyNode = modelLoader.loadMonkey(classroomScene);
        monkeyControl = monkeyNode.getControl(BetterCharacterControl.class);
        monkeyAnimComposer = monkeyNode.getControl(AnimComposer.class);

        Node blackholeScene = modelLoader.loadBlackhole();
        modelLoader.loadOto(blackholeScene);
        modelLoader.loadCakes(10, classroomScene, gameState);
        modelLoader.loadCakes(10, blackholeScene, gameState);

        // Initialize the first scene
        sceneManager.switchToNextScene();
    }

    private final ActionListener nextTextListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals("NextText") && isPressed) {
                if (startScreenActive) {
                    dismissStartScreen();
                    showTextSequence();
                } else if (textSequenceActive) {
                    updateTextDisplay();
                }
            }
        }
    };

    @Override
    public void simpleUpdate(float tpf) {
        if (!startScreenActive && !textSequenceActive) {
            inputHandler.firstPersonNavigationUpdate(tpf, playerNode, playerControl);

            if (sceneManager.hasSceneChanged()) {
                playSceneMusic(sceneManager.getCurrentSceneName());
            }

            chasePlayer();
            gotKey = inputHandler.getGotKey();
            if (gotKey) {
                System.out.println("Detected Key in Bag!!");
            }
        }
        
        /*      
        boolean destructTerrain = inputHandler.getDestructTerrain();
        if (destructTerrain){
            enqueue(() -> {
                // Perform terrain modification here, such as removing a part of the terrain
                room3.detachAllChildren(); // Replace with your specific operation
                room3.updateGeometricState();
            });
        }
        */
    }

    @Override
    public void simpleRender(RenderManager rm) {}

    
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
        notificationText = new BitmapText(guiFont, false);
        notificationText.setSize(guiFont.getCharSet().getRenderedSize());
        notificationText.setText("");
        notificationText.setColor(ColorRGBA.Red);
        guiNode.attachChild(notificationText);
    }
    
    private void playSceneMusic(String sceneName) {
        soundManager.stopCurrentBGM();

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
    
    private void chasePlayer() {
        if (monkeyNode != null && playerNode != null) {
            Vector3f monsterPosition = monkeyNode.getWorldTranslation();
            Vector3f playerPosition = playerNode.getWorldTranslation();
            Vector3f directionToPlayer = playerPosition.subtract(monsterPosition).normalizeLocal();

            monkeyControl.setWalkDirection(directionToPlayer.mult(monkeySpeed));
            monkeyControl.setViewDirection(directionToPlayer.negate());
        }
    }
    
    public Node loadRoom3() {
        
        room3 = new Node("Room3 Node");

        // Add Terrain
        Spatial terrainGeo = assetManager.loadModel("Scenes/room_3.j3o");
        terrainGeo.setLocalTranslation(0, 5, 0);
        terrainGeo.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        room3.attachChild(terrainGeo);

        // Add Trees
        Spatial tree1 = assetManager.loadModel("Models/Tree/Tree.j3o");
        tree1.scale(10);
        tree1.setQueueBucket(RenderQueue.Bucket.Transparent);
        tree1.setLocalTranslation(0, 7f, 0);
        tree1.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        room3.attachChild(tree1);

        Spatial tree2 = tree1.clone();
        tree2.setLocalTranslation(-50, 7f, -50);
        tree2.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        room3.attachChild(tree2);

        // Add Sky
        Spatial mySky = assetManager.loadModel("Scenes/mySky.j3o");
        room3.attachChild(mySky);

        // Add Reflected Scene and Water
        reflectedScene = new Node("Scene");

        Spatial boat = assetManager.loadModel("Models/Swan_Boat/swanboat.j3o");
        boat.scale(4);
        boat.setLocalTranslation(200, 2, -100);

        reflectedScene.attachChild(mySky);

        reflectedScene.attachChild(boat);

        room3.attachChild(reflectedScene);

        // Add Bonfire and Forest
        Spatial bonfire = assetManager.loadModel("Models/bonfire/bonfire_pot.j3o");
        bonfire.scale(8);
        bonfire.setLocalTranslation(-20, 13, -20);
        bonfire.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        room3.attachChild(bonfire);

        Spatial forest = assetManager.loadModel("Models/manyTrees/multiple_trees.j3o");
        forest.scale(8);
        forest.setLocalTranslation(-150, 5, -170);
        room3.attachChild(forest);

        // Add Particle Effects
        ParticleEffects particle = new ParticleEffects(assetManager, room3);
        particle.dust();
        particle.sparks();
        particle.burst();
        particle.fire();

        // Initialize fpp and Add Filters
        fpp = new FilterPostProcessor(assetManager);
        viewPort.addProcessor(fpp);

        FogFilter fogFilter = new FogFilter();
        fogFilter.setFogDistance(500);
        fogFilter.setFogDensity(0.2f);
        fogFilter.setFogColor(new ColorRGBA(0.9f, 0.9f, 0.9f, 1.0f));
        fpp.addFilter(fogFilter);

        WaterFilter water = new WaterFilter(reflectedScene, lightDir);
        water.setWaterHeight(3f);
        fpp.addFilter(water);

        LightScatteringFilter sunLightFilter = new LightScatteringFilter(lightDir.mult(-3000));
        fpp.addFilter(sunLightFilter);

        BloomFilter bloom = new BloomFilter();
        fpp.addFilter(bloom);

        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(lightDir);
        sun.setColor(ColorRGBA.White.clone().multLocal(2));
        room3.addLight(sun);

        DirectionalLightShadowFilter dlsf = new DirectionalLightShadowFilter(assetManager, 1024, 2);
        dlsf.setLight(sun);
        fpp.addFilter(dlsf);
        
        rootNode.attachChild(room3);
        
        room3.setLocalTranslation(0, 0, -20);
        
        RigidBodyControl terrainPhysics = new RigidBodyControl(0f); // Static terrain
        room3.addControl(terrainPhysics);
        bulletAppState.getPhysicsSpace().add(terrainPhysics);
        
        sceneManager.addScene(room3);
        
        return room3;
    }
}
