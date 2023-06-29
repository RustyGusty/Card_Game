package BaseGame.CardLogic;

import BaseGame.App;
import processing.core.PConstants;

// TODO - implement rules page
public class HomePage extends DeckHandler {

    public HomePage(App app) {
        super(app, 0);
        setup();
    }

    @Override
    public void setup() {
    }

    @Override
    public String encodeGameState() {
        return null;
    }

    @Override
    public boolean handleMouseClick(int mouseX, int mouseY) {
        return false;
    }

    @Override
    public void handleMousePress(int mouseX, int mouseY) {

    }

    @Override
    public void handleMouseDrag(int mouseX, int mouseY) {
        
    }

    @Override
    public void handleMouseRelease(int mouseX, int mouseY) {

    }

    @Override
    public void draw() {
        app.textSize(50f);
        app.color(255);
        String text = (app.waitingForGame()) 
            ? "Go to discord to join or host a game!"
            : "Waiting for host to start game";
        app.textAlign(PConstants.CENTER);
        app.text(text, 0.5f * app.displayWidth, 0.5f * app.displayHeight);
        app.textAlign(PConstants.LEFT);
    }

    @Override
    public String initializeDeck() {
        return null;
    }

    @Override
    public void initializeDeck(String str) {
    }

    @Override
    public void nextTurn(String boardState) {
    }
    
}
