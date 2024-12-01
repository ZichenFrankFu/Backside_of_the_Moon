package mygame;

import com.jme3.anim.AnimComposer;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.ChaseCamera;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;


public class ModelLoader {
    private final AssetManager assetManager;
    private final Node rootNode;
    private final SceneSwitchingManager sceneManager;
    private final BulletAppState bulletAppState;
    private final Camera cam;
    
    private Node handsNode;
    private CameraNode camNode;
    private AnimComposer animComposer;
    private ChaseCamera chaseCam;
    private BetterCharacterControl physicsHands;

    public ModelLoader(AssetManager assetManager, Node rootNode, BulletAppState bulletAppState, SceneSwitchingManager sceneManager, Camera cam) {
        this.assetManager = assetManager;
        this.rootNode = rootNode;
        this.bulletAppState = bulletAppState;
        this.sceneManager = sceneManager;
        this.cam = cam;
        
        assetManager.registerLocator("assets/", FileLocator.class);
        
        // Set up the sun lighting
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(0, -1, 0));
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);
    }
    

    public Node loadClassroom() {        
        /*
        * Scene 1: Class room
        */
        
        // Load the classroom scene
        Node classroomScene = new Node("ClassroomScene");
        Spatial classroom = assetManager.loadModel("Models/NoDeskClassroom/noDeskClassroom.j3o");
        classroom.setLocalScale(2.0f);
        classroom.setLocalTranslation(0,0,0);
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
    
    public Node loadTeleportGate(Node scene){
        Node teleportGateNode = new Node("TeleportGate");
        Spatial teleportGate = assetManager.loadModel("Models/TeleportGate/scene.j3o");
        teleportGate.setLocalScale(10f);
        teleportGate.setLocalTranslation(-12,2f,-1);
        teleportGateNode.setCullHint(Spatial.CullHint.Never);

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
    
    
    public Node loadMonkey(Node classroomScene) {
        // Load and scale the BloodyMonkey model
        Node bloodyMonkey = (Node) assetManager.loadModel("Models/Monkey/Jaime.j3o");
        bloodyMonkey.rotate(0, FastMath.DEG_TO_RAD * 180, 0);
        bloodyMonkey.setLocalScale(2.0f);
        bloodyMonkey.setLocalTranslation(0, 5.0f, 10.0f);
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
    
    public Node loadBlackhole() {
        // Load the blackhole scene
        Node blackholeScene = new Node("BlackholeScene");
        Spatial blackhole = assetManager.loadModel("Models/Blackhole/scene.j3o");
        blackhole.setLocalScale(12.0f);
        blackhole.setLocalTranslation(0,0,-40.0f);
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
        return blackholeScene;
    }
    
    public Node loadOto(Node blackholeScene) {
        /*
        Node Oto = (Node) assetManager.loadModel("Textures/Oto/Oto.mesh.xml");
        try {
          File file = new File("assets/Models/Oto/Oto.j3o");
          BinaryExporter exporter = BinaryExporter.getInstance();
          exporter.save(Oto, file);
        } catch (IOException e) {
          System.out.println("Unable to save j3o file: " + e.getMessage());
        }
        */
        Node Oto = (Node) assetManager.loadModel("Models/Oto/Oto.j3o");
        blackholeScene.attachChild(Oto);
        
        // Oto Physics
        RigidBodyControl OtoControl = new RigidBodyControl(0.5f); 
        Oto.addControl(OtoControl);
        bulletAppState.getPhysicsSpace().add(OtoControl); 
        
        return Oto;
    }
    
    public Node loadTerrain() {
        // Instantiate the Terrain class
        Terrain terrainApp = new Terrain();
        terrainApp.simpleInitApp(); // Initialize the terrain

        // Load Room3 node from the Terrain class
        Node terrainNode = terrainApp.loadRoom3();

        // Attach the terrain to the rootNode
        rootNode.attachChild(terrainNode);

        // Ensure all physics-related or scene manager actions are handled
        RigidBodyControl terrainPhysics = new RigidBodyControl(0f); // Static terrain
        terrainNode.addControl(terrainPhysics);
        bulletAppState.getPhysicsSpace().add(terrainPhysics);

        return terrainNode;
    }   
    
    public void loadCakes(int num, Node Scene, GameState gameState){
        int keyInd = (int) (Math.random() * num);
        for(int i = 0; i < num; i++){
            if(i == keyInd){
                Spatial cake = assetManager.loadModel("Models/Items/CAFETERIAcake.j3o");
                cake.setName("Key");
                cake.setLocalScale(5.0f);
                cake.setLocalTranslation(1.0f + i, 6.0f, 2.0f);
                
           
                RigidBodyControl cakeControl = new RigidBodyControl(0.5f); 
                cake.addControl(cakeControl);
                bulletAppState.getPhysicsSpace().add(cakeControl); 
                gameState.addPickableItem(cake);
                Scene.attachChild(cake);
            } else {
                Spatial cake = assetManager.loadModel("Models/Items/CAFETERIAcake.j3o");
                cake.setName("Cake");
                cake.setLocalScale(5.0f);
                cake.setLocalTranslation(1.0f + i, 6.0f, 2.0f);
                RigidBodyControl cakeControl = new RigidBodyControl(0.5f); 
                cake.addControl(cakeControl);
                bulletAppState.getPhysicsSpace().add(cakeControl); 
                gameState.addPickableItem(cake);
                Scene.attachChild(cake);

            }
            
        }
        
    }
    
    public void loadCatnana(int num, Node Scene, GameState gameState){
        int keyInd = (int) (Math.random() * num);
        for(int i = 0; i < num; i++){
            if(i == keyInd){
                Spatial cake = assetManager.loadModel("Models/Catnana/scene.gltf");
                cake.setName("Key");
                cake.setLocalScale(2.0f);
                cake.setLocalTranslation(1.0f + i, 3.0f, 2.0f);
                
           
                RigidBodyControl cakeControl = new RigidBodyControl(0.5f); 
                cake.addControl(cakeControl);
                bulletAppState.getPhysicsSpace().add(cakeControl); 
                gameState.addPickableItem(cake);
                Scene.attachChild(cake);
            } else {
                Spatial cake = assetManager.loadModel("Models/Catnana/scene.gltf");
                cake.setName("Cake");
                cake.setLocalScale(5.0f);
                cake.setLocalTranslation(1.0f + i, 6.0f, 2.0f);
                RigidBodyControl cakeControl = new RigidBodyControl(0.5f); 
                cake.addControl(cakeControl);
                bulletAppState.getPhysicsSpace().add(cakeControl); 
                gameState.addPickableItem(cake);
                Scene.attachChild(cake);

            }
            
        }
        
    }
}
