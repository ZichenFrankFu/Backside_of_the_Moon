package mygame;

import com.jme3.audio.AudioNode;
import com.jme3.asset.AssetManager;

import java.util.HashMap;
import java.util.Map;

public class SoundManager {

    private final Map<String, AudioNode> bgmMap;
    private final Map<String, AudioNode> sfxMap; 
    private AudioNode currentBGM; // Keeps track of the currently playing BGM

    public SoundManager(AssetManager assetManager) {
        bgmMap = new HashMap<>();
        sfxMap = new HashMap<>();
        currentBGM = null;

        loadBGMs(assetManager);
        loadSFX(assetManager);
    }

    /**
     * Loads all background music into the bgmMap.
     */
    private void loadBGMs(AssetManager assetManager) {
        bgmMap.put("quiet_bgm", createAudioNode(assetManager, "Sounds/bgm/quiet_bgm.ogg", true));
        bgmMap.put("mystery_bgm", createAudioNode(assetManager, "Sounds/bgm/quite_unsettled_bgm.ogg", true));
        bgmMap.put("movement_bgm", createAudioNode(assetManager, "Sounds/bgm/Movement_bgm.ogg", true));
    }

    /**
     * Loads all sound effects into the sfxMap.
     */
    private void loadSFX(AssetManager assetManager) {
        sfxMap.put("click", createAudioNode(assetManager, "Sounds/click.wav", false));
        sfxMap.put("step", createAudioNode(assetManager, "Sounds/wood_step.ogg", false));
        sfxMap.put("elevator_step", createAudioNode(assetManager, "Sounds/elevator_steps.ogg", false));
        sfxMap.put("game_over", createAudioNode(assetManager, "Sounds/game-over.ogg", false));
        sfxMap.put("bang", createAudioNode(assetManager, "Sounds/Bang.wav", false));
    }

    /**
     * Helper to create a preconfigured AudioNode.
     */
    private AudioNode createAudioNode(AssetManager assetManager, String filePath, boolean loop) {
        AudioNode audio = new AudioNode(assetManager, filePath, false);
        audio.setPositional(false);
        audio.setLooping(loop); // Loop for BGMs, single instance for SFX
        audio.setVolume(2); // Default volume
        return audio;
    }

    /**
     * Play the specified BGM, stopping any currently playing BGM.
     * @param name
     */
    public void playBGM(String name) {
        if (currentBGM != null) {
            currentBGM.stop();
        }
        currentBGM = bgmMap.get(name);
        if (currentBGM != null) {
            currentBGM.play();
        } else {
            System.err.println("BGM with name '" + name + "' not found!");
        }
    }

    /**
     * Stop the currently playing BGM.
     */
    public void stopCurrentBGM() {
        if (currentBGM != null) {
            currentBGM.stop();
            currentBGM = null;
        }
    }

    /**
     * Play a sound effect once.
     * @param name
     */
    public void playSFX(String name) {
        AudioNode sfx = sfxMap.get(name);
        if (sfx != null) {
            sfx.playInstance();
        } else {
            System.err.println("SFX with name '" + name + "' not found!");
        }
    }
    
    public AudioNode getSFXNode(String name) {
        return sfxMap.get(name);
    }

    
    public AudioNode getSFXNodeForStep(String name) {
        AudioNode original = sfxMap.get(name); // Retrieve the original AudioNode
        if (original != null) {
            AudioNode clone = original.clone(); // Clone the AudioNode
            clone.setPositional(false); // Ensure the sound is non-positional
            clone.setLooping(false); // Ensure the sound does not loop
            return clone;
        } else {
            System.err.println("Sound effect '" + name + "' not found in the SFX map.");
            return null;
        }
    }
    public AudioNode getCurrentBGM() {
    return currentBGM;
}


}
