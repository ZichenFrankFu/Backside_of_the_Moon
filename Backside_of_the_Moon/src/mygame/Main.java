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
    private BitmapText textDisplay;
    private Picture startScreen;
    private boolean startScreenActive = true;
    private boolean textSequenceActive = false;
    float iconWidth = 104;
    float iconHeight = 94;
    private BitmapText moveNextText;
    
    // Scene Manage
    private int sceneCount = 0;
    private Node classroomScene;
    private Node blackholeScene;
    private int moveNext = 0;
    private boolean enableSpaceSwitching = false;
    

    private boolean stopChasing = false;
    // Monkey chasing
    private Node monkeyNode;
    private BetterCharacterControl monkeyControl;
    private AnimComposer monkeyAnimComposer;
    private float monkeySpeed = 4.0f;
    
    // Monster sound timer
    private float monsterSoundTimer = 0.0f;
    private Boolean isPlayed = false;
    
    // Oto Chasing
    private Node otoNode;
    private BetterCharacterControl otoControl;
    private AnimComposer otoAnimComposer;
    private float otoSpeed = 15.0f;
    private float otoHalfSpeed = 2.0f;
   
    // Bag check
    private boolean gotKey = false;
    
    // Teleport Gate
    private Node teleportGateNode;
    private boolean hasTeleport = false;

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
    private Node terrainScene;
    
    //Endings
    private boolean enteredEnding = false;
    private Ending ending;
    public static int keyCount;
    private boolean firstEndingComplete = false;
    private boolean beforeRoom2 = false;

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        soundManager = new SoundManager(assetManager);
        
        flyCam.setEnabled(true); // Enable FlyCam
        flyCam.setMoveSpeed(50);
        ending = new Ending(this, soundManager);
        // Settings
        this.setDisplayFps(false);
        this.setDisplayStatView(false);
        this.setShowSettings(false);
        this.inputManager.setCursorVisible(false);
        
        // Show start screen
        showStartScreen();

        // Register input to proceed
        inputManager.addMapping("NextText", new KeyTrigger(com.jme3.input.KeyInput.KEY_SPACE));
        inputManager.addListener(nextTextListener, "NextText");

        // Initialize text sequence
        textSequence = List.of("""
                               ...A faint whisper stirs the stillness;
                               It calls to you, soft as breath on glass.....""",
                "You might wonder why there is no START button but only CONTINUE.",
                "Perhaps... this is not the beginning at all.", 
                                                               """
                                                               You think of choice, but choice has never been yours.
                                                               neither your birth,
                                                               nor this moment,
                                                               nor the steps you take from here.""",
                "Ah, but I jest.", 
                                    """
                                   The truth? You've lost everything.
                                   Your memory is as ash upon the wind.""",
                "We have stood here before... you and I... countless times", """
                                                                           Now, let us walk the same path again\u2014
                                                                           and see if, this time, you understand why.""", 
                    """
                    Press W S A D to run, Press Shift to speed up, 
                    Press F to pick things up. Run from what? You'll see. 
                    """,
                    """
                    Room 1 | The Feast of the Red Monkey
                    The crimson shadow has awakened, chasing the scent of sweetness. 
                    Do not halt, do not let his grasp find you.
                    The key hides in the false sweetness, and only those who touch its truth may escape.
                    """,
                    """
                    Room 2 | The Cage of the Watchful
                    This being freezes under light, stops under gaze.
                    But when your eyes falter, the shadow approaches silently.
                    Keep your gaze steady, search for salvation, and do not waver.
                    """);
        
    }
    
    @Override
    public void simpleUpdate(float tpf) {
        
        if (!startScreenActive && !textSequenceActive) {
            inputHandler.firstPersonNavigationUpdate(tpf, playerNode, playerControl);

            if (sceneManager.hasSceneChanged()) {
                playSceneMusic(sceneManager.getCurrentSceneName());
            }

            // Add positional sound from monster
            monsterSoundTimer += tpf;
            if (monsterSoundTimer >= 5f) { // Play every 5 seconds
                if (monkeyNode != null) { // Ensure the monster node exists
                    Vector3f monsterPosition = monkeyNode.getWorldTranslation();
                    soundManager.playPositionalSFX("monster", monsterPosition); // Use the new method
                } else {
                    System.err.println("Monster node (monkeyNode) is null!");
                }
                monsterSoundTimer = 0f; // Reset the timer
            }
            
            if (!stopChasing){
                monkeyChasePlayer();
            }

            gotKey = inputHandler.getGotKey();
            
            this.enqueue(() -> {
            
            if (gotKey && !hasTeleport && sceneCount == 0) {
                soundManager.playSFX("getkey");
                teleportGateNode = modelLoader.loadTeleportGate(classroomScene);
                hasTeleport = true;
            }
            
            if (gotKey && keyCount == 2 && sceneCount == 1) {
                if (!isPlayed) {
                    soundManager.playSFX("getkey");
                    isPlayed = true;
                }
                viewPort.removeProcessor(fpp);
                fpp.cleanup();
                
                ending.cleanupEnding(rootNode);
              
                List<String> textSequenceMoonbase = List.of("The moment you picked up the last key, a mirror appeared ahead.",
                    "You could not help but look inside.", """
                                                           You reach out, but the reflection does not.
                                                           The glass does not distort, and yet it reveals a grotesque truth:""",
                    "You are hands.",
                    "You are only hands, grasping at a memory of a body now lost.",
                    "You moved through the mirror and found yourself escaped the rooms, but not the truth.", 
                                                                """
                                                                The final puzzle is not one of locks or keys, but of recognition:
                                                                the desolation, the dust, the low hum of machines \u2014 all whisper this is no Earthly place.""",
                    "You are no longer home.", """
                                               Beyond, the horizon is barren, and the Earth hangs like a pale wound in the black.
                                               The Moon cradles you now, its cold, alien beauty reminds that --
                                               Survival is not the same as return.""",
                    ""
                    );
                ending.setEnding(textSequenceMoonbase, "Textures/ending_moonbase.jpg", "moon_ending");
                ending.startEnding();
                }
            
            // Check if player is standing in the teleport gate
            if (isPlayerInTeleportGate() && sceneCount == 0) {
                if (moveNext == 0){
                    setMoveNextText(true);
                    sceneManager.switchToNextScene();
                    gotKey = false;
                    inputHandler.resetGotKey();
                    enableSpaceSwitching = true;
                    inputHandler.enableSpaceSwitching(enableSpaceSwitching);
                    if (classroomScene.getControl(RigidBodyControl.class) != null) {
                        RigidBodyControl control = classroomScene.getControl(RigidBodyControl.class);
                        bulletAppState.getPhysicsSpace().remove(control);
                        classroomScene.removeControl(control);
                    }
                }
                stopChasing = true;
                moveNext++; 
            }
            otoChasePlayerWhenNotSeen();
            if (sceneCount == 1){
                
                playerNode.setLocalTranslation(new Vector3f(5f, 13f, 1f));
                moveNextText.setText("");
                guiNode.attachChild(moveNextText);
                enableSpaceSwitching = false;
                stopChasing = false;
                inputHandler.enableSpaceSwitching(enableSpaceSwitching);
                //setMoveNextText(false);
            }
            
            /*
             if (checkMonsterPlayerCollision(monkeyNode) && enteredEnding == false) {
                viewPort.removeProcessor(fpp);
                fpp.cleanup();
                ending.cleanupEnding(rootNode);
                
                List<String> textSequenceClassroom = List.of(
                "The desks have teeth. The windows have eyes.",
                "Your steps were loud where silence was demanded.",
                "You are seated now, and the class shall begin again.",
                "Attendance, mandatory.",
                ""
                );
                ending.setEnding(textSequenceClassroom, "Textures/ending_classroom.jpg", "classroom_ending");
                ending.startEnding();
            }
            
            if (checkMonsterPlayerCollision(otoNode)) {
                viewPort.removeProcessor(fpp);
                fpp.cleanup();
                ending.cleanupEnding(rootNode);
                
                List<String> textSequenceClassroom = List.of(
                "The void knows no mercy, and the stars do not mourn.",
                "The blackness does not end; it only consumes.",
                "You are stretched thin, and now, you are no more.",
                ""
                );
                ending.setEnding(textSequenceClassroom, "Textures/ending_blackhole.jpg", "terrin_ending");
                ending.startEnding();
            }
            */
            });
            
        }
    }
    
    @Override
    public void simpleRender(RenderManager rm) {}
    
    /*
    * Helper functions
    */
    
    private void initializeGame() {
        keyCount = 0;
        
        // Physics
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);

        // Add gravity
        bulletAppState.getPhysicsSpace().setGravity(new Vector3f(0, -9.8f, 0));

        // Sound Manager
        soundManager.playBGM("quiet_bgm"); // Play background music for the classroom

        // Create player Node
        playerNode = new Node("the player");
        
        playerNode.setLocalTranslation(new Vector3f(5f, 13f, 1f));
        rootNode.attachChild(playerNode);

        // Player Control
        playerControl = new BetterCharacterControl(1.5f, 4, 30f);
        playerControl.setGravity(new Vector3f(0, -9.8f, 0));
        playerNode.addControl(playerControl);
        bulletAppState.getPhysicsSpace().add(playerControl);

        // Camera Node Setup
        camNode = new CameraNode("CamNode", cam);
        camNode.setControlDir(ControlDirection.SpatialToCamera);
        camNode.setLocalTranslation(new Vector3f(0, 4, 0));
        playerNode.attachChild(camNode);

        // Scene Switch
        sceneManager = new SceneSwitchingManager(this);
        stateManager.attach(sceneManager);

        // Static UI
        setNotificationText();
        gameState = new GameState(cam, inputManager, notificationText);
        stateManager.attach(gameState);
        createCrosshair();
        createSaveButton();
        createLoadButton();

        // Input Handle
        inputHandler = new UserInputHandler(inputManager, cam, sceneManager, camNode, gameState, soundManager);
        inputHandler.enableSpaceSwitching(enableSpaceSwitching);

        // Load Model
        modelLoader = new ModelLoader(assetManager, rootNode, bulletAppState, sceneManager);
        classroomScene = modelLoader.loadClassroom();
        modelLoader.loadCakes(9, classroomScene, gameState);
        monkeyNode = modelLoader.loadMonkey(classroomScene);
        monkeyControl = monkeyNode.getControl(BetterCharacterControl.class);
        monkeyAnimComposer = monkeyNode.getControl(AnimComposer.class);

        blackholeScene = modelLoader.loadBlackhole();
        modelLoader.loadStars(9, blackholeScene, gameState);
        terrainScene = loadTerrain();
        otoNode = modelLoader.loadOto(terrainScene);
        otoControl = otoNode.getControl(BetterCharacterControl.class);
        otoAnimComposer = otoNode.getControl(AnimComposer.class);


        // Initialize the first scene
        sceneManager.switchToNextScene();
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
            case "terrainNode":
                soundManager.playBGM("mystery_bgm");
            default:
                System.out.println("No BGM mapped for scene: " + sceneName);
                break;
        }
    }
    
    /*
    * Static UI
    */
    
    private void showStartScreen() {
        soundManager.playBGM("starting");
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
        textDisplay.setSize(guiFont.getCharSet().getRenderedSize() * 4.5f);
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
            
        } else if (beforeRoom2) {
            return;
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
            
            if (name.equals("NextText") && isPressed && enableSpaceSwitching){
                sceneCount++;
            }
        }
    };
    
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
        notificationText.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        notificationText.setText("");
        notificationText.setColor(ColorRGBA.Red);
        guiNode.attachChild(notificationText);
    }
    
    private void setMoveNextText(boolean show){
        moveNextText = new BitmapText(guiFont, false);
        moveNextText.setSize(guiFont.getCharSet().getRenderedSize() * 3);
        
        moveNextText.setColor(ColorRGBA.Red);
        // Position the message text slightly below the crosshair
        float screenWidth = settings.getWidth();
        float screenHeight = settings.getHeight();
        float offset = 30; // Adjust offset as needed to position the text below
        moveNextText.setLocalTranslation(
            (screenWidth - moveNextText.getLineWidth()) / 2,
            (screenHeight + crosshair.getLineHeight()) / 2 - offset,
            0
        );
        
        if (show){
            moveNextText.setText("Press Space to escape!");
        } else {
            moveNextText.setText("");
        }
        guiNode.attachChild(moveNextText);
    }
    
    
    /*
    * Collision Check
    */
        
    private boolean isPlayerInTeleportGate() {
        if (teleportGateNode == null || playerNode == null) {
            return false;
        }

        // Get the position of both the player and the teleport gate
        Vector3f playerPosition = playerNode.getWorldTranslation();
        Vector3f gatePosition = teleportGateNode.getWorldTranslation();

        // Calculate the distance between the player and the teleport gate
        float distance = playerPosition.distance(gatePosition);

        // Define a threshold for the teleport range (e.g., 3 units)
        float teleportThreshold = 7.4f;

        // Check if the player is within the range of the teleport gate
        return distance <= teleportThreshold;
    }
    
    private boolean checkMonsterPlayerCollision(Node monsterNode) {
        if (playerNode == null || monsterNode == null) {
            return false;
        }

        // Get the positions of the player and the monkey
        Vector3f playerPosition = playerNode.getWorldTranslation();
        Vector3f monsterPosition = monsterNode.getWorldTranslation();

        // Calculate the distance between them
        float distance = playerPosition.distance(monsterPosition);

        // Define a collision threshold (e.g., 2.0f units)
        float collisionThreshold = 3.0f;

        // Check if the player and monkey are close enough
        return distance <= collisionThreshold;
    }

    
    
    
    /*
    * Monster Logic
    */
    private void monkeyChasePlayer() {
        if (monkeyNode != null && playerNode != null) {
            Vector3f monsterPosition = monkeyNode.getWorldTranslation();
            Vector3f playerPosition = playerNode.getWorldTranslation();
            Vector3f directionToPlayer = playerPosition.subtract(monsterPosition).normalizeLocal();

            monkeyControl.setWalkDirection(directionToPlayer.mult(monkeySpeed));
            monkeyControl.setViewDirection(directionToPlayer.negate());
        }
    }
    
    private void otoChasePlayerWhenNotSeen() {
    if (otoNode != null && playerNode != null) {
        // Get positions
        Vector3f otoPosition = otoNode.getWorldTranslation();
        Vector3f playerPosition = playerNode.getWorldTranslation();
        
        // Calculate direction to Oto from player
        Vector3f directionToOto = otoPosition.subtract(playerPosition).normalizeLocal();
        
        // Get the player's view direction (you might need to adjust how to get this in your framework)
        Vector3f playerViewDirection = playerNode.getControl(BetterCharacterControl.class).getViewDirection();
        
        // Calculate the dot product to determine if the player is looking at Oto
        float dotProduct = playerViewDirection.dot(directionToOto);
        
        // Define thresholds
        float fullSpeedThreshold = -0.3f; // Fully behind the player
        
        
        if (dotProduct < fullSpeedThreshold) {
            // Player is not looking at Oto, move at full speed
            Vector3f directionToPlayer = playerPosition.subtract(otoPosition).normalizeLocal();
            otoControl.setWalkDirection(directionToPlayer.mult(otoSpeed));
            otoControl.setViewDirection(directionToPlayer);
            otoAnimComposer.setCurrentAction("Walk"); // Play walk animation
        } else {
            // Player is looking directly at Oto, stop moving
            otoControl.setWalkDirection(Vector3f.ZERO);
        }
    }
    }
    
    public Node loadTerrain() {
        
        terrainScene = new Node("terrainNode");

        // Add Terrain
        Spatial terrainGeo = assetManager.loadModel("Scenes/room_3.j3o");
        terrainGeo.setLocalTranslation(0, 5, 0);
        terrainGeo.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        terrainScene.attachChild(terrainGeo);

        // Add Trees
        /*
        Spatial tree1 = assetManager.loadModel("Models/Tree/Tree.j3o");
        tree1.scale(10);
        tree1.setQueueBucket(RenderQueue.Bucket.Transparent);
        tree1.setLocalTranslation(0, 7f, 0);
        tree1.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        terrainScene.attachChild(tree1);

        Spatial tree2 = tree1.clone();
        tree2.setLocalTranslation(-50, 7f, -50);
        tree2.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        terrainScene.attachChild(tree2);
        */
        // Add Sky
        Spatial mySky = assetManager.loadModel("Scenes/mySky.j3o");
        terrainScene.attachChild(mySky);

        // Add Reflected Scene and Water
        reflectedScene = new Node("Scene");

        Spatial boat = assetManager.loadModel("Models/Swan_Boat/swanboat.j3o");
        boat.scale(4);
        boat.setLocalTranslation(200, 2, -100);

        reflectedScene.attachChild(mySky);

        reflectedScene.attachChild(boat);

        terrainScene.attachChild(reflectedScene);

        // Add Bonfire and Forest
        Spatial bonfire = assetManager.loadModel("Models/bonfire/bonfire_pot.j3o");
        bonfire.scale(8);
        bonfire.setLocalTranslation(-20, 13, -20);
        bonfire.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        terrainScene.attachChild(bonfire);

        Spatial forest = assetManager.loadModel("Models/manyTrees/multiple_trees.j3o");
        forest.scale(8);
        forest.setLocalTranslation(-150, 5, -170);
        terrainScene.attachChild(forest);

        // Add Particle Effects
        ParticleEffects particle = new ParticleEffects(assetManager, terrainScene);
        particle.dust();
        particle.sparks();
        particle.burst();
        particle.fire();
        
        // Initialize fpp and Add Filters
        fpp = new FilterPostProcessor(assetManager);
        viewPort.addProcessor(fpp);
        
        FogFilter fogFilter = new FogFilter();
        fogFilter.setFogDistance(50);
        fogFilter.setFogDensity(0.8f);
        fogFilter.setFogColor(new ColorRGBA(0.9f, 0.9f, 0.9f, 1.0f));
        fpp.addFilter(fogFilter);
        
        WaterFilter water = new WaterFilter(reflectedScene, lightDir);
        water.setWaterHeight(2f);
        fpp.addFilter(water);

        LightScatteringFilter sunLightFilter = new LightScatteringFilter(lightDir.mult(-3000));
        fpp.addFilter(sunLightFilter);
        
        BloomFilter bloom = new BloomFilter();
        fpp.addFilter(bloom);
        
        
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(lightDir);
        sun.setColor(ColorRGBA.White.clone().multLocal(2));
        terrainScene.addLight(sun);

        DirectionalLightShadowFilter dlsf = new DirectionalLightShadowFilter(assetManager, 1024, 2);
        dlsf.setLight(sun);
        fpp.addFilter(dlsf);
        
        rootNode.attachChild(terrainScene);
        
        terrainScene.setLocalTranslation(0, 0, -20);
        
        RigidBodyControl terrainPhysics = new RigidBodyControl(0f); // Static terrain
        terrainScene.addControl(terrainPhysics);
        bulletAppState.getPhysicsSpace().add(terrainPhysics);
        
        sceneManager.addScene(terrainScene);
        
        return terrainScene;
    }
    
}
