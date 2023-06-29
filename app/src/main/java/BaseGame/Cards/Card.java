package BaseGame.Cards;

public final class Card implements Comparable<Card> {

    /** -1 for black joker, -2 for red joker */
    public final int value;
    public final Suit suit;

    /**
     * Constructor for a poker card, with value and suit
     * @param value
     * @param suit
     */
    public Card(int value, Suit suit) {
        this.value = value;
        this.suit = suit;
    }

    /**
     * Constructor for a joker (black or red)
     * @param deckNum
     */
    public Card(boolean isBlackJoker) {
        this.value = isBlackJoker ? -1 : -2;
        this.suit = null;
    }

    @Override
    public boolean equals(Object other) {
        if(other == null) return this == null;
        if(other.getClass() != this.getClass()) return false;
        return this.hashCode() == other.hashCode();
    }

    /**
     * The hashCode for this card corresponds to the index in App.imageList
     */
    @Override
    public int hashCode() {
        if(value < 0)
            return 54 + value;
        return (value) * 4 + suit.getValue();
    }

    @Override
    public int compareTo(Card o) {
        return this.hashCode() - o.hashCode();
    }

    @Override
    public String toString() {
        String res;
        switch(value){
            case 0:
                res = "A";
                break;
            case 10:
                res = "J";
                break;
            case 11:
                res = "Q";
                break;
            case 12:
                res = "K";
                break;
            default:
                res = Integer.toString(value + 1);
                break;
        }
        res += " of " + suit.toString();
        return res;
    }

    public String toHexString(){
        String res = Integer.toHexString(this.hashCode());
        int startingLength = res.length();
        for(int i = startingLength; i < 2; i++) {
            res = "0" + res;
        }
        return res;
    }

    /**
     * Given the hash value of a Card, returns a new equivalent card
     * @param hashValue
     * @return
     */
    public static Card toCard(int hashValue) {
        if(hashValue >= 52) 
            return new Card(hashValue == 53);
        
        return new Card(hashValue / 4, Suit.getSuit(hashValue % 4));
    }

}