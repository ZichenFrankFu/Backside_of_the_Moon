package mygame;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;


public class ModelLoader {
    private final AssetManager assetManager;
    private final Node rootNode;
    private final SceneSwitchingManager sceneManager;
    private final BulletAppState bulletAppState;
    

    public ModelLoader(AssetManager assetManager, Node rootNode, BulletAppState bulletAppState, SceneSwitchingManager sceneManager) {
        this.assetManager = assetManager;
        this.rootNode = rootNode;
        this.bulletAppState = bulletAppState;
        this.sceneManager = sceneManager;
        
        assetManager.registerLocator("assets/", FileLocator.class);
        
        // Set up the sun lighting
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(0, -1, 0));
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);
    }
    
    /*
    * Scenes
    */
    
     public Node loadTeleportGate(Node scene){
        /*
         * Transition Teleport gate
         */
        Node teleportGateNode = new Node("TeleportGate");
        Spatial teleportGate = assetManager.loadModel("Models/TeleportGate/scene.j3o");
        teleportGate.setLocalScale(15f);
        teleportGate.setLocalTranslation(-12,12f,-1);
        teleportGateNode.setCullHint(Spatial.CullHint.Never);

        // Gate lights
        DirectionalLight gateLight = new DirectionalLight();
        gateLight.setDirection(new Vector3f(1, -1, 0));
        gateLight.setColor(ColorRGBA.White.mult(1.5f));
        teleportGate.addLight(gateLight);
        
        AmbientLight ambientLightGate = new AmbientLight();
        ambientLightGate.setColor(ColorRGBA.White.mult(1.0f));
        teleportGate.addLight(ambientLightGate);
        
        // Gate Physics
        RigidBodyControl gatePhy = new RigidBodyControl(0f);
        teleportGate.addControl(gatePhy);
        bulletAppState.getPhysicsSpace().add(gatePhy);
        
        teleportGateNode.attachChild(teleportGate);
        scene.attachChild(teleportGateNode);
        
        return teleportGateNode;
    }
    
    
    public Node loadClassroom() {        
        /*
        * Scene 1: Classroom
        */
        
        // Load the classroom scene
        Node classroomScene = new Node("ClassroomScene");
        Spatial classroom = assetManager.loadModel("Models/NoDeskClassroom/noDeskClassroom.j3o");
        classroom.setLocalScale(2.0f);
        classroom.setLocalTranslation(0,8f,0);
        classroomScene.attachChild(classroom);
        sceneManager.addScene(classroomScene);
        
        // Classroom Lights
        DirectionalLight classroomLight = new DirectionalLight();
        classroomLight.setDirection(new Vector3f(1, -1, 0));
        classroomLight.setColor(ColorRGBA.White.mult(1.5f));
        classroom.addLight(classroomLight);

        DirectionalLight classroomLight2 = new DirectionalLight();
        classroomLight2.setDirection(new Vector3f(-1, -1, 0));
        classroomLight2.setColor(ColorRGBA.White.mult(1.5f));
        classroom.addLight(classroomLight2);

        DirectionalLight classroomLight3 = new DirectionalLight();
        classroomLight3.setDirection(new Vector3f(0, -1, 1));
        classroomLight3.setColor(ColorRGBA.White.mult(1.5f));
        classroom.addLight(classroomLight3);
        
        DirectionalLight mainLightClassroom = new DirectionalLight();
        mainLightClassroom.setDirection(new Vector3f(-1, -1, -1).normalizeLocal());
        mainLightClassroom.setColor(ColorRGBA.White.mult(2.0f)); 
        classroom.addLight(mainLightClassroom);

        AmbientLight ambientLightClassroom = new AmbientLight();
        ambientLightClassroom.setColor(ColorRGBA.White.mult(1.0f));
        classroom.addLight(ambientLightClassroom);
        
        // Classroom Physics
        RigidBodyControl classroomPhy = new RigidBodyControl(0f);
        classroomScene.addControl(classroomPhy);
        bulletAppState.getPhysicsSpace().add(classroomPhy);
       
        // Load Claassroom into scene manager
        sceneManager.addScene(classroomScene);
        
        
        return classroomScene;
    }
 
    public Node loadBlackhole() {
        /*
        * Scene 2: Blackhole
        */
        // Load the blackhole scene
        Node blackholeScene = new Node("BlackholeScene");
        Spatial blackhole = assetManager.loadModel("Models/Blackhole/scene.j3o");
        blackhole.setLocalScale(12.0f);
        blackhole.setLocalTranslation(0,18f,-40.0f);
        blackholeScene.attachChild(blackhole);
        sceneManager.addScene(blackholeScene);
        
        // Blackhole lights
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White.mult(1.2f)); 
        blackholeScene.addLight(ambient);

        DirectionalLight glow = new DirectionalLight();
        glow.setDirection(new Vector3f(-0.5f, -1, -0.5f));
        glow.setColor(new ColorRGBA(0.5f, 0.4f, 0.3f, 1.0f).mult(0.2f)); 
        blackholeScene.addLight(glow);

        PointLight glowEffect = new PointLight();
        glowEffect.setPosition(new Vector3f(0, 0, -5));
        glowEffect.setColor(new ColorRGBA(0.7f, 0.5f, 0.3f, 1.0f).mult(0.3f)); 
        glowEffect.setRadius(10f); 
        blackholeScene.addLight(glowEffect);
        
        //Particle Effects
        ParticleEffects particle = new ParticleEffects(assetManager, rootNode);
        particle.dust();
        //particle.sparks();
        //particle.burst();
        //particle.fire();
        
        return blackholeScene;
    }
    
    
    /*
    * Monsters
    */
       public Node loadMonkey(Node classroomScene) {
        // Load and scale the BloodyMonkey model
        Node bloodyMonkey = (Node) assetManager.loadModel("Models/Monkey/Jaime.j3o");
        bloodyMonkey.rotate(0, FastMath.DEG_TO_RAD * 180, 0);
        bloodyMonkey.setLocalScale(4.0f);
        bloodyMonkey.setLocalTranslation(-7.0f, 11.0f, 18.0f);
        classroomScene.attachChild(bloodyMonkey);
        
        //Load materials onto BloodyMonkey model
        Material bloodyMonkeyMaterial = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        TextureKey bloodyMonkeyTextureKey = new TextureKey("Textures/blood.png", true); 
        Texture bloodyMonkeyTexture = assetManager.loadTexture(bloodyMonkeyTextureKey);
        bloodyMonkeyMaterial.setTexture("DiffuseMap", bloodyMonkeyTexture);
        bloodyMonkey.setMaterial(bloodyMonkeyMaterial);
        
        // Monkey Physics using BetterCharacterControl for proper movement
        BetterCharacterControl monkeyControl = new BetterCharacterControl(1.5f, 4, 30f);
        bloodyMonkey.addControl(monkeyControl);
        bulletAppState.getPhysicsSpace().add(monkeyControl);
        
        // Set up the AnimControl for animations
        AnimControl animControl = bloodyMonkey.getControl(AnimControl.class);
        if (animControl != null) {
            AnimChannel animChannel = animControl.createChannel();
            animChannel.setAnim("Walk"); // Set the default animation to Idle
        }
        
        return bloodyMonkey;
    }
       
    public Node loadOto(Node blackholeScene) {
        
        Node Oto = (Node) assetManager.loadModel("Models/Oto/Oto.j3o");
        blackholeScene.attachChild(Oto);
        
        
        // Set up the AnimControl for animations
        AnimControl animControl = Oto.getControl(AnimControl.class);
        if (animControl != null) {
            AnimChannel animChannel = animControl.createChannel();
            animChannel.setAnim("Walk"); // Set the default animation to Idle
        }
        
        Oto.setLocalTranslation(-10, 25.0f, -10);
        BetterCharacterControl otoControl = new BetterCharacterControl(0.5f, 1.8f, 80f);
        Oto.addControl(otoControl);
        bulletAppState.getPhysicsSpace().add(otoControl); // Ensure it is added to the physics space
        
        return Oto;
    }
    
    /*
    * Pickable Items
    */
    
    public void loadCakes(int num, Node scene, GameState gameState) {
        // Randomly select one cake to be the "Key"
        int keyInd = (int) (Math.random() * num);
        if (keyInd == num){
            keyInd = num - 1;
        }

        // Load the cake model once and create a reusable material
        Spatial cakeModel = assetManager.loadModel("Models/Items/CAFETERIAcake.j3o");

        for (int i = 0; i < num; i++) {
            // Clone the cake model to create a new instance for each cake
            Spatial cake = cakeModel.clone();
            cake.setLocalScale(5.0f);

            int row = i / 3;
            int col = i % 3;
            // Set different translations for each cake to avoid overlapping
            float xPos = -12.0f + col * 10.0f;  // Adjusted to give some spacing
            float yPos = 7.0f;
            float zPos = 2.0f + row * 15.0f;

            cake.setLocalTranslation(xPos, yPos, zPos);

            // Set unique name for the key cake
            if (i == keyInd) {
                cake.setName("Key");
            } else {
                cake.setName("Cake");
            }

            // Add RigidBodyControl to the cake
            RigidBodyControl cakeControl = new RigidBodyControl(0.5f);
            cake.addControl(cakeControl);
            bulletAppState.getPhysicsSpace().add(cakeControl);

            // Add the cake to the game state as a pickable item
            gameState.addPickableItem(cake);
            scene.attachChild(cake);
        }
    }

    
    public void loadStars(int num, Node scene, GameState gameState) {
        // Randomly select one star to be the "Key"
        int keyInd = (int) (Math.random() * num);

        // Load the star model and create a reusable material
        Spatial starModel = assetManager.loadModel("Models/Star/scene.j3o");
        Material starMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        // Set a moon-like color (soft grayish white)
        starMat.setColor("Diffuse", new ColorRGBA(0.3f, 0.3f, 0.3f, 1.0f));  // Dark gray to reduce brightness
        starMat.setColor("Ambient", new ColorRGBA(0.2f, 0.2f, 0.2f, 1.0f));  // Even darker for ambient light
        starMat.setBoolean("UseMaterialColors", true);
        // Remove or reduce shininess
        starMat.setFloat("Shininess", 8f);


        for (int i = 0; i < num; i++) {
            // Clone the star model to create a new instance for each star
            Spatial star = starModel.clone();
            star.setMaterial(starMat);  // Reuse the same material for all stars
            star.setLocalScale(0.05f);

            int row = i / 3;
            int col = i % 3;
            // Set different translations for each cake to avoid overlapping
            float xPos = -12.0f + col * 10.0f;  // Adjusted to give some spacing
            float yPos = 7.0f;
            float zPos = 2.0f + row * 15.0f;
            star.setLocalTranslation(xPos, yPos, zPos);

            // Set unique name for the key star
            if (i == keyInd) {
                star.setName("Key");
            } else {
                star.setName("Star");
            }

            // Add RigidBodyControl to the star
            RigidBodyControl starControl = new RigidBodyControl(0.5f);
            star.addControl(starControl);
            bulletAppState.getPhysicsSpace().add(starControl);

            // Add the star to the game state as a pickable item
            gameState.addPickableItem(star);
            scene.attachChild(star);
        }
    }

    
 
}
