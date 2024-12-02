package mygame;

import com.jme3.audio.AudioNode;
import com.jme3.asset.AssetManager;

import java.util.HashMap;
import java.util.Map;

public class SoundManager {

    private final Map<String, AudioNode> bgmMap;
    private final Map<String, AudioNode> sfxMap;
    private final Map<String, Boolean> sfxStateMap; // Tracks the play state of looping SFX
    private AudioNode currentBGM; // Keeps track of the currently playing BGM

    public SoundManager(AssetManager assetManager) {
        bgmMap = new HashMap<>();
        sfxMap = new HashMap<>();
        sfxStateMap = new HashMap<>();
        currentBGM = null;

        loadBGMs(assetManager);
        loadSFX(assetManager);
    }

    /**
     * Loads all background music into the bgmMap.
     */
    private void loadBGMs(AssetManager assetManager) {
        bgmMap.put("quiet_bgm", createAudioNode(assetManager, "Sounds/bgm/quiet_bgm.ogg", true, 0.3f));
        bgmMap.put("mystery_bgm", createAudioNode(assetManager, "Sounds/bgm/quite_unsettled_bgm.ogg", true, 0.3f));
        bgmMap.put("movement_bgm", createAudioNode(assetManager, "Sounds/bgm/Movement_bgm.ogg", true, 0.3f));
    }

    /**
     * Loads all sound effects into the sfxMap.
     */
    private void loadSFX(AssetManager assetManager) {
        sfxMap.put("pickup", createAudioNode(assetManager, "Sounds/click.wav", false, 1.0f));
//        sfxMap.put("step", createAudioNode(assetManager, "Sounds/wood_step.ogg", true, 0.7f));
        sfxMap.put("step", createAudioNode(assetManager, "Sounds/wood_step.ogg", 0.7f));
        sfxMap.put("elevator_step", createAudioNode(assetManager, "Sounds/elevator_steps.ogg", false, 1.0f));
        sfxMap.put("game_over", createAudioNode(assetManager, "Sounds/game-over.ogg", false, 0.3f));
        sfxMap.put("bang", createAudioNode(assetManager, "Sounds/Bang.wav", false, 0.3f));
        sfxMap.put("monster", createAudioNode(assetManager, "Sounds/monster.wav", false, 0.8f));
    }

    /**
     * Helper to create a preconfigured AudioNode.
     */
    private AudioNode createAudioNode(AssetManager assetManager, String filePath, boolean loop, float volume) {
        AudioNode audio = new AudioNode(assetManager, filePath, false);
        audio.setPositional(false);
        audio.setLooping(loop); // Loop for BGMs, single instance for SFX
        audio.setVolume(volume); // Default volume
        return audio;
    }
    
    /**
    * Overloaded method to create a positional audio node specifically for step sounds.
    *
    * @param assetManager The asset manager to load the sound file.
    * @param filePath     The file path to the step sound.
    * @param volume       The volume of the step sound.
    * @return The configured AudioNode for the step sound.
    */
   private AudioNode createAudioNode(AssetManager assetManager, String filePath, float volume) {
       AudioNode audio = new AudioNode(assetManager, filePath, false);
       audio.setPositional(true);       // Step sounds are positional
       audio.setLooping(true);          // Step sounds are looping
       audio.setVolume(volume);         // Set default volume
       return audio;
   }


    /**
     * Play the specified BGM, stopping any currently playing BGM.
     * @param name Name of the background music to play.
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
     * Play a looping SFX, like footsteps.
     * @param name Name of the sound effect to play.
     */
    public void playLoopingSFX(String name) {
        AudioNode sfx = sfxMap.get(name);
        if (sfx != null && !sfxStateMap.getOrDefault(name, false)) {
            sfx.play();
            sfxStateMap.put(name, true); // Mark as playing
        } else if (sfx == null) {
            System.err.println("SFX with name '" + name + "' not found!");
        }
    }

    /**
     * Stop a looping SFX, like footsteps.
     * @param name Name of the sound effect to stop.
     */
    public void stopLoopingSFX(String name) {
        AudioNode sfx = sfxMap.get(name);
        if (sfx != null && sfxStateMap.getOrDefault(name, false)) {
            sfx.stop();
            sfxStateMap.put(name, false); // Mark as not playing
        } else if (sfx == null) {
            System.err.println("SFX with name '" + name + "' not found!");
        }
    }

    /**
     * Play a sound effect once.
     * @param name Name of the sound effect to play.
     */
    public void playSFX(String name) {
        AudioNode sfx = sfxMap.get(name);
        if (sfx != null) {
            sfx.playInstance();
        } else {
            System.err.println("SFX with name '" + name + "' not found!");
        }
    }

    /**
     * Stop a sound effect, if it is playing.
     * @param name Name of the sound effect to stop.
     */
    public void stopSFX(String name) {
        AudioNode sfx = sfxMap.get(name);
        if (sfx != null) {
            sfx.stop();
        } else {
            System.err.println("SFX with name '" + name + "' not found!");
        }
    }

    /**
     * Get the currently playing BGM.
     * @return The currently playing AudioNode for BGM.
     */
    public AudioNode getCurrentBGM() {
        return currentBGM;
    }
}
