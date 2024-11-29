package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;

import java.util.List;

public class TextSequenceManager {

    private final SimpleApplication app;
    private final BitmapText textDisplay;
    private final SoundManager soundManager;

    private List<String> textSequence;
    private int currentTextIndex;
    private boolean isActive;
    private float displayDuration; // Time each text is displayed
    private float elapsedTime;    // Time elapsed since the current text was shown
    private Runnable onComplete;  // Callback to invoke when the sequence finishes

    public TextSequenceManager(SimpleApplication app, SoundManager soundManager) {
        this.app = app;
        this.soundManager = soundManager;

        // Initialize textDisplay with the default font
        BitmapFont defaultFont = getDefaultFont();
        this.textDisplay = new BitmapText(defaultFont, false);
        this.textDisplay.setSize(defaultFont.getCharSet().getRenderedSize() * 1.5f);
        this.textDisplay.setColor(ColorRGBA.White);
        this.textDisplay.setText(""); // Start with no text
        centerText();
        app.getGuiNode().attachChild(this.textDisplay);

        this.isActive = false;
        this.displayDuration = 3.0f; // Default 3 seconds per text
        this.elapsedTime = 0.0f;
    }

    private BitmapFont getDefaultFont() {
        return app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
    }

    public void setTextSequence(List<String> textSequence) {
        this.textSequence = textSequence;
    }

    public void startSequence(Runnable onComplete) {
        this.onComplete = onComplete;
        this.isActive = true;
        this.currentTextIndex = 0;
        this.displayNextText();
    }

    private void displayNextText() {
        if (currentTextIndex < textSequence.size()) {
            String text = textSequence.get(currentTextIndex);
            textDisplay.setText(text);
            centerText();
            currentTextIndex++;
            elapsedTime = 0.0f; // Reset elapsed time for the next text
        } else {
            finishSequence();
        }
    }

    private void finishSequence() {
        isActive = false;
        textDisplay.setText("");
        if (onComplete != null) {
            onComplete.run(); // Trigger the completion callback
        }
    }

    public void update(float tpf) {
        if (!isActive) return;

        elapsedTime += tpf;
        if (elapsedTime >= displayDuration) {
            displayNextText();
        }
    }

    public boolean isActive() {
        return isActive;
    }

    /**
     * Centers the text on the screen based on the current text content and screen dimensions.
     */
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
