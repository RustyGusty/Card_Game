package BaseGame.CardLogic;


import BaseGame.App;

/**
 * Abstract class to work in conjunction with App.java, allowing multiple different games
 * to be run at once depending on how the abstract class is implemented
 */
public abstract class DeckHandler {
    /** Reference to currently-used App */
    public App app;
    /** Encoded version of the starting deck */
    public String startingDeck;
    /** Defines the minimum player count, usually 2 */
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

    /** 
     * Begins the game after the deck has been initialized with initializeDeck,
     * setting up everything that needs to be drawn and initializing logic
     */
    public abstract void setup();

    /**
     * Creates a new, randomized deck for the game
     * @return a String representing the initial starting deck
     */
    public abstract String initializeDeck();

    /**
     * Reads in the String found in deck (from another call to initializeDeck)
     * and uses it to initialize the starting deck
     * @param deck A String representing the deck to initialize
     */
    public abstract void initializeDeck(String deck);

    /**
     * Returns a String representation of the game state, to be 
     * used in nextTurn
     * @return A String representation of the current game state
     */
    public abstract String encodeGameState();

    /**
     * Reads in the String boardState and uses it to fill the board with the new
     * board layout from a previous call to encodeGameState
     * @param boardState A String representation of the desired game state
     */
    public abstract void nextTurn(String boardState);


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
}
