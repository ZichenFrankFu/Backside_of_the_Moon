/**
 * Authors: Jitong Xian, Xinming Shen, Zichen Fu
 * Main class of game. 
 */


package mygame;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.ParticleMesh.Type;
import com.jme3.effect.shapes.EmitterSphereShape;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.jme3.math.Vector3f;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 * @author normenhansen
 */

public class ParticleEffects extends SimpleApplication {
    
    private ParticleEmitter dustEmitter;
    private float angle;
    private AssetManager assetManager;
    private Node rootNode;
    
    public ParticleEffects(AssetManager assetManager, Node rootNode) {
        this.assetManager = assetManager;
        this.rootNode = rootNode;
    }

    @Override
    public void simpleInitApp() {
    }
    
    public void dust() {
        dustEmitter = new ParticleEmitter("dust emitter", Type.Triangle, 100);
        Material dustMat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        dustEmitter.setMaterial(dustMat);
        dustMat.setTexture("Texture", assetManager.loadTexture("Effects/smoke.png"));
        dustEmitter.setImagesX(2);
        dustEmitter.setImagesY(2);
        dustEmitter.setSelectRandomImage(true);
        dustEmitter.setRandomAngle(true);
        dustEmitter.getParticleInfluencer().setVelocityVariation(1f);
        dustEmitter.setLocalTranslation(new Vector3f(50, 5, 50));
        rootNode.attachChild(dustEmitter);
    }
    
    public void sparks(){
        ParticleEmitter sparksEmitter = new ParticleEmitter("Spark emitter", Type.Triangle, 60);
        rootNode.attachChild(sparksEmitter);
        Material sparkMat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        sparkMat.setTexture("Texture",
        assetManager.loadTexture("Effects/spark.png"));
        sparksEmitter.setMaterial(sparkMat);
        sparksEmitter.setImagesX(1);
        sparksEmitter.setImagesY(1);
        sparksEmitter.getParticleInfluencer().
        setInitialVelocity(new Vector3f(0, 10, 0));
        sparksEmitter.getParticleInfluencer().
        setVelocityVariation(1.0f);
        sparksEmitter.setStartColor(ColorRGBA.Red);
        sparksEmitter.setEndColor(ColorRGBA.Yellow);
        sparksEmitter.setGravity(0, 50, 0);
        sparksEmitter.setFacingVelocity(true);
        sparksEmitter.setStartSize(50f);
        sparksEmitter.setEndSize(5f);
        sparksEmitter.setLowLife(.9f);
        sparksEmitter.setHighLife(1.1f);
        sparksEmitter.setLocalTranslation(new Vector3f(100, 5, 100));
    }
    
    public void burst(){
        ParticleEmitter burstEmitter = new ParticleEmitter("Burst emitter", Type.Triangle, 5);
        rootNode.attachChild(burstEmitter);
        Material burstMat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        burstMat.setTexture("Texture",
        assetManager.loadTexture("Effects/flash.png"));
        burstEmitter.setImagesX(2);
        burstEmitter.setImagesY(2);
        burstEmitter.setSelectRandomImage(true);
        burstEmitter.setMaterial(burstMat);
        burstEmitter.setStartColor(new ColorRGBA(1f, 0.8f, 0.36f, 1f));
        burstEmitter.setEndColor(new ColorRGBA(1f, 0.8f, 0.36f, 0f));
        burstEmitter.setStartSize(.1f);
        burstEmitter.setEndSize(5.0f);
        burstEmitter.setGravity(0, 0, 0);
        burstEmitter.setLowLife(.2f);
        burstEmitter.setHighLife(.2f);
        burstEmitter.getParticleInfluencer().
        setInitialVelocity(new Vector3f(0, 5f, 0));
        burstEmitter.getParticleInfluencer().
        setVelocityVariation(1f);
        burstEmitter.setShape(new EmitterSphereShape(Vector3f.ZERO, .5f));
        burstEmitter.setLocalTranslation(new Vector3f(70, 5, 70));
    }
    
    public void fire(){
        ParticleEmitter fireEmitter = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30);
        Material fireMat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        rootNode.attachChild(fireEmitter);
        fireMat.setTexture("Texture", assetManager.loadTexture("Effects/flame.png"));   
        fireEmitter.setMaterial(fireMat);
        fireEmitter.setImagesX(2);
        fireEmitter.setImagesY(2);
        fireEmitter.setSelectRandomImage(true);
        fireEmitter.setRandomAngle(true);
        fireEmitter.setStartColor(new ColorRGBA(1f, 1f, .5f, 1f));
        fireEmitter.setEndColor(new ColorRGBA(1f, 0f, 0f, 0f));
        fireEmitter.setGravity(0,0,0);
        fireEmitter.getParticleInfluencer().
        setVelocityVariation(0.2f);
        fireEmitter.getParticleInfluencer().
        setInitialVelocity(new Vector3f(0,3f,0));
        fireEmitter.setLowLife(0.5f);
        fireEmitter.setHighLife(2f);
        fireEmitter.setStartSize(4f);
        fireEmitter.setEndSize(0.05f);
        fireEmitter.setLocalTranslation(new Vector3f(-20, 5, -20));
    }
    
    @Override
    public void simpleUpdate(float tpf) {
        // make the emitter fly in circles
        angle += tpf;
        angle %= FastMath.TWO_PI;
        float x = FastMath.cos(angle) * 2;
        float y = FastMath.sin(angle) * 2;
        dustEmitter.setLocalTranslation(x, 0, y);
    }

    @Override
    public void simpleRender(RenderManager rm) {

    }
    
    
}
