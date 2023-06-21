package BaseGame.Cards;

import java.util.ArrayList;
import java.util.List;

public class DeckHelper {
    public static List<Card> createDeck(int numFullDecks, int numJokerSets) {
        List<Card> deck = new ArrayList<Card>(Math.max(10, 52 * numFullDecks + 2 * numJokerSets));
        for (int i = 0; i < numFullDecks; i++) {
            addFullDeck(deck, i);
        }
        for (int i = 0; i < numJokerSets; i++) {
            deck.add(new Card(true));
            deck.add(new Card(false));
        }
        return deck;
    }

    private static void addFullDeck(List<Card> deck, int deckNum) {
        for (int i = 0; i < 13; i++) {
            for (Suit s : Suit.values()) {
                deck.add(new Card(i, s));
            }
        }
    }

    /**
     * Draws the top card from the deck to return it
     * @param deck The deck to draw from
     * @return The removed card from the deck, {@code null} if no card could be drawn
     */
    public static Card draw(List<Card> deck) {
        if(deck.size() <= 0) return null;
        return deck.remove(deck.size() - 1);
    }

    /**
     * Draws multiple cards and returns it as an Array
     * @param deck The deck to draw from
     * @param numCards Number of cards to remove
     * @return An Array of numCards cards taken from the top, with null at the last
     * placed card if not able to draw enough
     */
    public static Card[] draw(List<Card> deck, int numCards) {
        Card res[] = new Card[numCards];
        for (int i = 0; i < numCards; i++) {
            res[i] = draw(deck);
            if(res[i] == null) return res;
        }
        return res;
    }
}
