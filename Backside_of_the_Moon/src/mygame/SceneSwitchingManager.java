package mygame;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.scene.Node;
import java.util.ArrayList;
import java.util.List;

public class SceneSwitchingManager extends AbstractAppState {

    private final SimpleApplication app;
    private Node currentScene;
    private List<Node> scenes;
    private int currentSceneIndex;
    private int lastSceneIndex;

    public SceneSwitchingManager(SimpleApplication app) {
        this.app = app;
        this.scenes = new ArrayList<>();
        this.currentSceneIndex = -1;
        this.lastSceneIndex = -1;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
    }

    @Override
    public void cleanup() {
        // super.cleanup();
        // Detach the current scene from the rootNode when the AppState is cleaned up
        if (currentScene != null) {
            currentScene.removeFromParent();
        }
    }

    // Method to destruct the current scene
    public void destructScene() {
        if (currentScene != null) {
            currentScene.removeFromParent();
        }
    }

    // Method to reconstruct the scene
    public void reconstructScene(Node newScene) {
        // Destruct the old scene completely
        destructScene();

        // Attach the new scene directly to the rootNode
        currentScene = newScene;
        app.getRootNode().attachChild(currentScene);
    }
        // Method to load a new scene
    public void loadScene(Node newScene) {
        reconstructScene(newScene);
    }

    // Method to add a scene to the list of scenes
    public void addScene(Node scene) {
        scenes.add(scene);
    }

    // Method to switch to the next scene in a cyclic manner
    public void switchToNextScene() {
        if (scenes.isEmpty()) {
            return;
        }
        
        /*
        // Ensure scene switching happens on the rendering thread
        app.enqueue(() -> {
        // Destruct the current scene completely before loading the next one
        destructScene();
        lastSceneIndex = currentSceneIndex;
        
        currentSceneIndex = (currentSceneIndex + 1) % scenes.size();
        loadScene(scenes.get(currentSceneIndex));
        });
        */
        
        // Destruct the current scene completely before loading the next one
        destructScene();
        lastSceneIndex = currentSceneIndex;
        
        currentSceneIndex = (currentSceneIndex + 1) % scenes.size();
        loadScene(scenes.get(currentSceneIndex));
    }
    
    /**
     * Checks if the scene has changed since the last frame.
     *
     * @return true if the scene has changed; false otherwise.
     */
    public boolean hasSceneChanged() {
        if (currentSceneIndex != lastSceneIndex) {
            lastSceneIndex = currentSceneIndex; // Reset after detecting change
            return true;
        }
        return false;
    }

    
    /**
     * Gets the name of the current scene.
     *
     * @return the name of the current scene, or null if no scene is loaded.
     */
    public String getCurrentSceneName() {
        if (currentScene != null) {
            return currentScene.getName();
        }
        return null;
    }
}