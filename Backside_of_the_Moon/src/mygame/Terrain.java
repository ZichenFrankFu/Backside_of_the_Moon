/**
 * Authors: Jitong Xian, Xinming Shen, Zichen Fu
 * Main class of game. 
 */


package mygame;
import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.post.filters.DepthOfFieldFilter;
import com.jme3.post.filters.FogFilter;
import com.jme3.post.filters.LightScatteringFilter;
import com.jme3.post.ssao.SSAOFilter;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.water.WaterFilter;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 * @author normenhansen
 */
public class Terrain extends SimpleApplication {
    
    private FilterPostProcessor fpp;
    private FogFilter fogFilter;
    private final Vector3f lightDir = new Vector3f(-0.39f, -0.32f, -0.74f);
    private LightScatteringFilter sunLightFilter;
    private Node reflectedScene;
    private DepthOfFieldFilter dofFilter;
    private BloomFilter bloom;
    private Node room3;

    public static void main(String[] args) {
        Terrain app = new Terrain ();
        app.start(); 
    }

    @Override
    public void simpleInitApp() {
        room3 = new Node("Room3 Node");
        rootNode.attachChild(room3);
    }
    
    public Node loadRoom3(){
        //Add Terrain
        Spatial terrainGeo = assetManager.loadModel("Scenes/room_3.j3o");
        room3.attachChild(terrainGeo);
        
        //Add Trees
        Spatial tree1 = assetManager.loadModel("Models/Tree/Tree.j3o");
        tree1.scale(10); 
        tree1.setQueueBucket(Bucket.Transparent);
        room3.attachChild(tree1);
        Vector3f treeLoc1 = new Vector3f(0,7f,0);
        tree1.setLocalTranslation(treeLoc1);
        
        Spatial tree2 = tree1.clone();
        room3.attachChild(tree2);
        Vector3f treeLoc2 = new Vector3f(-50,7f,-50);
        tree2.setLocalTranslation(treeLoc2);
        
        //Add Sky
        Spatial mySky = assetManager.loadModel("Scenes/mySky.j3o");
        room3.attachChild(mySky);
        
        //Set Camera
        cam.setLocation(new Vector3f(-100, 10, -100));
        flyCam.setMoveSpeed(100);
        
        //Add Fog
        fpp = new FilterPostProcessor(assetManager);
        viewPort.addProcessor(fpp);
        
        fogFilter = new FogFilter();
        fogFilter.setFogDistance(500);
        fogFilter.setFogDensity(0.2f);
        fpp.addFilter(fogFilter);
        fogFilter.setFogColor(new ColorRGBA(0.9f, 0.9f, 0.9f, 1.0f));

        //Add Sunlight
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(lightDir);
        sun.setColor(ColorRGBA.White.clone().multLocal(2));
        room3.addLight(sun);
        sunLightFilter = new LightScatteringFilter(lightDir.mult(-3000));
        fpp.addFilter(sunLightFilter);
        cam.lookAtDirection(lightDir.negate(),Vector3f.UNIT_Y);
        
        //Add Water
        reflectedScene = new Node("Scene");
        room3.attachChild(reflectedScene);
        
        Spatial boat = assetManager.loadModel("Models/Swan_Boat/swanboat.j3o");
        boat.scale(4);
        boat.setLocalTranslation(200, 2, -100);
        
        reflectedScene.attachChild(mySky);
        reflectedScene.attachChild(boat);
        
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        viewPort.addProcessor(fpp);
        WaterFilter water = new WaterFilter(reflectedScene, lightDir);
        fpp.addFilter(water);
        water.setWaterHeight(3f);
        terrainGeo.setLocalTranslation(terrainGeo.getLocalTranslation().add(0, 5, 0));
        
        //Bonfire Model
        Spatial bonfire = assetManager.loadModel("Models/bonfire/bonfire_pot.j3o");
        room3.attachChild(bonfire);
        bonfire.scale(8);
        bonfire.setLocalTranslation(new Vector3f(-20, 13, -20));
        
        Spatial forest = assetManager.loadModel("Models/manyTrees/multiple_trees.j3o");
        room3.attachChild(forest);
        forest.scale(8);
        forest.setLocalTranslation(new Vector3f(-150, 5, -170));
                
        //Particle Effects
        ParticleEffects particle = new ParticleEffects(assetManager, rootNode);
        //particle.dust();
        //particle.sparks();
        //particle.burst();
        particle.fire();
        
        //Shading & Lighting -- Does not work for now somehow
        
        DirectionalLightShadowFilter dlsf = new DirectionalLightShadowFilter(assetManager, 1024, 2);
        dlsf.setLight(sun);
        dlsf.setEnabled(true);
        
        fpp.addFilter(dlsf);
        viewPort.addProcessor(fpp);
        
        //rootNode.setShadowMode(ShadowMode.Off);
        terrainGeo.setShadowMode(ShadowMode.CastAndReceive);
        tree1.setShadowMode(ShadowMode.CastAndReceive);
        tree2.setShadowMode(ShadowMode.CastAndReceive);
        bonfire.setShadowMode(ShadowMode.CastAndReceive);
        
        //Not really suitable for this scene, but it works
        
        bloom = new BloomFilter();
        fpp.addFilter(bloom);
        
        return room3;
    }
   
    
    public void simpleUpdate() {

    }
    
    @Override
    public void simpleRender(RenderManager rm) {

    }
    
    
}
