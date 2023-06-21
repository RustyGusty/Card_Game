package BaseGame.CardLogic;

import BaseGame.App;

// TODO - implement rules page
public class HomePage extends DeckHandler {

    public HomePage(App app) {
        super(app, 0);
    }

    @Override
    public void setup() {
        
    }
    @Override
    public void decodeGameState(String boardState) {
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
        
    }

    @Override
    public String initializeDeck() {
        return null;
    }

    @Override
    public void initializeDeck(String str) {
    }
    
}
