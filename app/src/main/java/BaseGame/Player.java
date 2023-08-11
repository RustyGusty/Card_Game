package BaseGame;

import java.util.ArrayList;
import java.util.List;

import BaseGame.Cards.*;

public class Player {
    public int playerNumber;
    private String name;
    public final List<Card> hand;
    private int score;

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public int incrementScore(int inc) {
        score += inc;
        return score;
    }

    public void resetScore() {
        score = 0;
    }

    public Player(String name, int playerNumber) {
        this.playerNumber = playerNumber;
        this.name = name;
        hand = new ArrayList<Card>();
        score = 0;
    }

    /**
     * Draws a card from drawDeck and places it into the player's hand
     * @param drawDeck Deck to be drawn from
     * @return {@code true} if the draw was successful, {@code false} if the drawDeck is empty
     */
    public boolean addFromDeck(List<Card> drawDeck) {
        Card addedCard = DeckHelper.draw(drawDeck);
        if(addedCard == null) return false;
        hand.add(addedCard);
        return true;
    }

    public int addFromDeck(List<Card> drawDeck, int numCards) {
        for(int i = 0; i < numCards; i++)
            if(!addFromDeck(drawDeck)) return i;
        return numCards;
    }

    public boolean isPlayer(String name) {
        return this.name.equals(name);
    }

    /**
     * Formats the player as Player #: player_name
     */
    public String toString() {
        return String.format("Player %d: %s", playerNumber, name);
    }
}
