package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.scene.Node;
import com.jme3.ui.Picture;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.ColorRGBA;

import java.util.List;

public class Ending {

    private final SimpleApplication app;
    private final BitmapText textDisplay;
    private final Node endingNode;
    private final Picture endingImage;
    private final SoundManager soundManager;

    private List<String> currentTextSequence;
    private int currentTextIndex;
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

        this.endingImage = new Picture("Ending Image");

        // Set up input mapping for transitioning through the ending
        app.getInputManager().addMapping("NextEnding", new KeyTrigger(com.jme3.input.KeyInput.KEY_SPACE));
        app.getInputManager().addListener(endTransitionListener, "NextEnding");

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
        this.currentTextIndex = 0;

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
        displayNextText();
    }

    private void displayNextText() {
        if (currentTextIndex < currentTextSequence.size()) {
            String text = currentTextSequence.get(currentTextIndex);
            textDisplay.setText(text);
            centerText();
            currentTextIndex++;
        } else {
            transitionToImage();
        }
    }

    private void transitionToImage() {
        isTextPhase = false;
        endingNode.detachChild(textDisplay); // Remove text
        endingNode.attachChild(endingImage); // Show image
    }

    private void quitGame() {
        app.stop(); // Quit the game
    }

    private final ActionListener endTransitionListener = (name, isPressed, tpf) -> {
        if (!isActive || !isPressed) return;

        if (isTextPhase) {
            displayNextText();
        } else {
            quitGame();
        }
    };

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
}
