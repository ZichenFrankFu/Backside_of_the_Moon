package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.scene.Node;
import com.jme3.ui.Picture;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;

import java.util.List;

public class Ending {

    private final SimpleApplication app;
    private final BitmapText textDisplay;
    private final Node endingNode;
    private final Picture endingImage;
    private final SoundManager soundManager;

    private List<String> currentTextSequence;
    private int currentTextIndex = 0;
    private boolean isTextPhase;
    private boolean isActive;

    public Ending(SimpleApplication app, SoundManager soundManager) {
        this.app = app;
        this.soundManager = soundManager;

        // Initialize textDisplay with the default font
        BitmapFont defaultFont = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        this.textDisplay = new BitmapText(defaultFont, false);
        this.textDisplay.setSize(defaultFont.getCharSet().getRenderedSize() * 1.5f);
        this.textDisplay.setColor(ColorRGBA.White);
        this.textDisplay.setText(""); // Start with no text
        this.endingNode = new Node("Ending Node");
        app.getGuiNode().attachChild(this.endingNode);
        this.endingNode.attachChild(this.textDisplay);
        endingNode.attachChild(textDisplay);

        this.endingImage = new Picture("Ending Image");

        // Set up input mapping for transitioning through the ending
        app.getInputManager().addMapping("NextEnding", new KeyTrigger(com.jme3.input.KeyInput.KEY_SPACE));
        app.getInputManager().addListener(nextTextListener, "NextEnding");

        this.isActive = false;
        this.isTextPhase = true;
    }

    /**
     * Sets up the ending with text, image, and sound.
     *
     * @param textSequence List of text to display during the ending.
     * @param imagePath    Path to the image to show after text.
     * @param soundKey     Sound effect to play for this ending (SoundManager key).
     */
    public void setEnding(List<String> textSequence, String imagePath, String soundKey) {
        this.currentTextSequence = textSequence;
        //this.currentTextIndex = 0;

        // Configure the ending image
        endingImage.setImage(app.getAssetManager(), imagePath, true);
        endingImage.setWidth(app.getCamera().getWidth());
        endingImage.setHeight(app.getCamera().getHeight());
        endingImage.setPosition(0, 0);

        // Play the associated sound effect
        if (soundKey != null && !soundKey.isEmpty()) {
            soundManager.playSFX(soundKey);
        }
        
    }

    public void startEnding() {
        isActive = true;
        isTextPhase = true;
        showTextSequence();
        if (currentTextIndex == 0){
            updateTextDisplay();
        }
    }
    
    private void showTextSequence() {
        endingNode.attachChild(textDisplay);
        isTextPhase = true;
    }
    
    private void updateTextDisplay() {
        Camera cam = app.getCamera();
        if (currentTextIndex < currentTextSequence.size()) {
            String text = currentTextSequence.get(currentTextIndex);
            textDisplay.setText(text);
            currentTextIndex = currentTextIndex + 1;
            centerText();
        } else {
            transitionToImage();
        }
        if (currentTextIndex == currentTextSequence.size()){
            isTextPhase = false;
            transitionToImage();
        }
    }
    
    private void transitionToImage() {
        if (textDisplay != null) {
            endingNode.attachChild(endingImage);
        }
    }

    private final ActionListener nextTextListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            
            if (currentTextSequence == null) {
                System.err.println("Error: currentTextSequence is null.");
                return;
            }
            
            if (name.equals("NextEnding") && isPressed) {
                if (isTextPhase) {
                    updateTextDisplay();
            }
        }
    };

    private void quitGame() {
        app.stop(); // Quit the game
    }

    public boolean isActive() {
        return isActive;
    }

    private void centerText() {
        float screenWidth = app.getCamera().getWidth();
        float screenHeight = app.getCamera().getHeight();
        textDisplay.setLocalTranslation(
                (screenWidth - textDisplay.getLineWidth()) / 2, // Center horizontally
                screenHeight / 2, // Center vertically
                0
        );
    }
    
    public void cleanupEnding(Node rootNode) {
        if (rootNode != null) {
            rootNode.detachAllChildren(); // Remove all child nodes
            app.getGuiNode().detachChild(rootNode); // Detach the ending node itself
        }
        this.isActive = false; // Reset active state
    }
}

