package BaseGame.CardLogic;


import BaseGame.App;

public abstract class DeckHandler {
    public App app;
    /** Encoded version of the starting deck */
    public String startingDeck;
    public final int minPlayerCount;
    /** Indicates which player has won in playerList */
    public int winningPlayerNumber = -1;

    /**
     * Initializes the passed app through the setup function
     * @param app
     */
    public DeckHandler(App app, int minPlayerCount) {
        this.app = app;
        this.minPlayerCount = minPlayerCount;
    }

    public abstract void setup();

    public abstract String initializeDeck();

    public abstract void initializeDeck(String str);

    public abstract String encodeGameState();

    public abstract void declareWinner(int winningPlayerNumber);

    
    /**
     * Handles whatever should happen when the mouse is clicked
     * @param mouseX X-position of click
     * @param mouseY Y-position of click
     * @return {@code true} if the next turn should start, {@code false} if no
     * turn switch has happened yet
     */
    public abstract boolean handleMouseClick(int mouseX, int mouseY);

    /**
     * Handles whatever should happen when the mouse is pressed
     * @param mouseX
     * @param mouseY
     * @return
     */
    public abstract void handleMousePress(int mouseX, int mouseY);

    /**
     * Handles whatever should happen when the mouse is dragged
     * @param mouseX
     * @param mouseY
     * @return
     */
    public abstract void handleMouseDrag(int mouseX, int mouseY);

    /**
     * Handles whatever should happen when the mouse is released
     * @param mouseX
     * @param mouseY
     */
    public abstract void handleMouseRelease(int mouseX, int mouseY);
    
    /**
     * Defines the layout of the board and where everything is drawn
     */
    public abstract void draw();

    public abstract void nextTurn(String boardState);
    

}
