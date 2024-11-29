package mygame;
import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.renderer.RenderManager;

/**
 * Authors: Jitong Xian, Xinming Shen, Zichen Fu
 * All sounds are handled in this class
 */
public class BackgroundMusic extends SimpleApplication {

    public static void main(String[] args) {
        BackgroundMusic app = new BackgroundMusic();
        app.start(); 
    }

    @Override
    public void simpleInitApp() {
        AudioNode quietBGM = new AudioNode(
        assetManager, "Sounds/bgm/quiet_bgm.ogg", false);
        quietBGM.setPositional(false);
        quietBGM.setVolume(5);
        quietBGM.setLooping(true);
        quietBGM.play();
    }
    
    @Override
    public void simpleUpdate(float tpf) {

    }

    @Override
    public void simpleRender(RenderManager rm) {

    }
    
    
}
