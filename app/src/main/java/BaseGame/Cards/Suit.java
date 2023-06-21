package BaseGame.Cards;

public enum Suit {
    DIAMONDS(0),
    CLUBS(1),
    HEARTS(2),
    SPADES(3);

    private final static Suit values[] = {DIAMONDS, CLUBS, HEARTS, SPADES};
    private final int value;

    private Suit(int value) {
        this.value = value;
    }

    public int getValue(){
        return this.value;
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }

    public static Suit getSuit(int index) {
        return values[index];
    }
}
