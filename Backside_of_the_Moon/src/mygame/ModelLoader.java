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
import com.jme3.export.binary.BinaryExporter;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import java.io.File;
import java.io.IOException;

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
    
    public Node loadHands() {
        // Load the arms model
        Spatial handsModel = assetManager.loadModel("Models/Hands/arms.glb");
        handsModel.scale(0.5f);  // Adjust the scale as needed.
        
        handsModel.setName("hands");

        // Create a node for the hands and attach the model
        handsNode = new Node("HandsNode");
        handsNode.setLocalTranslation(new Vector3f(4, 5, 2));
        handsNode.attachChild(handsModel);

        // Add a character control to the node so we can add other things and
        // control the model rotation
        //physicsHands = new BetterCharacterControl(0.3f, 2.0f, 8f);
        //handsNode.addControl(physicsHands);
        //bulletAppState.getPhysicsSpace().add(physicsHands);
        
        

        // Add character node to the rootNode
        rootNode.attachChild(handsNode);
        
        // Set up animations if available
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
              
        return handsNode;
    }

    public Node loadClassroom() {        
        /*
        * Scene 1: Class room
        */
        
        // Load the classroom scene
        Node classroomScene = new Node("Classroom Scene");
        Spatial classroom = assetManager.loadModel("Models/NoDeskClassroom/noDeskClassroom.gltf");
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
        mainLightClassroom.setColor(ColorRGBA.White.mult(2.0f)); // Brighter white light
        classroom.addLight(mainLightClassroom);

        AmbientLight ambientLightClassroom = new AmbientLight();
        ambientLightClassroom.setColor(ColorRGBA.White.mult(1.0f)); // Bright ambient light to fill shadows
        classroom.addLight(ambientLightClassroom);
        
        // Classroom Physics
        RigidBodyControl classroomPhy = new RigidBodyControl(0f);
        classroomScene.addControl(classroomPhy);
        bulletAppState.getPhysicsSpace().add(classroomPhy);
       
        // Load Claassroom into scene manager
        sceneManager.addScene(classroomScene);
        
        
        
        return classroomScene;
    }
    
    public Node loadMonkey(Node classroomScene) {
        // Load and scale the BloodyMonkey model
        Node bloodyMonkey = (Node) assetManager.loadModel("Models/Monkey/Jaime.j3o");
        bloodyMonkey.rotate(0, FastMath.DEG_TO_RAD * 180, 0);
        bloodyMonkey.setLocalScale(2.0f);
        bloodyMonkey.setLocalTranslation(0, 3.0f, 0);
        classroomScene.attachChild(bloodyMonkey);
        
        //Load materials onto BloodyMonkey model
        Material bloodyMonkeyMaterial = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        TextureKey bloodyMonkeyTextureKey = new TextureKey("Textures/blood.png", true); 
        Texture bloodyMonkeyTexture = assetManager.loadTexture(bloodyMonkeyTextureKey);
        bloodyMonkeyMaterial.setTexture("DiffuseMap", bloodyMonkeyTexture);
        bloodyMonkey.setMaterial(bloodyMonkeyMaterial);
        
        // Monkey Physics
        RigidBodyControl monkeyControl = new RigidBodyControl(0.5f); 
        bloodyMonkey.addControl(monkeyControl);
        bulletAppState.getPhysicsSpace().add(monkeyControl); 
        
        return bloodyMonkey;
    }
    
    public Node loadBlackhole() {
        // Load the blackhole scene
        Node blackholeScene = new Node("Blackhole Scene");
        Spatial blackhole = assetManager.loadModel("Models/Blackhole/scene.j3o");
        blackhole.setLocalScale(9.0f);
        blackhole.setLocalTranslation(0,0,-40.0f);
        blackholeScene.attachChild(blackhole);
        sceneManager.addScene(blackholeScene);
        
        // load Oto
        // code to convert Oto example to j3o file 
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
        Spatial Oto = assetManager.loadModel("Models/Oto/Oto.j3o");

        blackholeScene.attachChild(Oto);
        
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
    
}
