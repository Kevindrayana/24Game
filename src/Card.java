import java.util.List;
class Card {
    private final String rank;
    private final String suit;
    private final int value;

    public Card(String rank, String suit) {
        this.rank = rank;
        this.suit = suit;
        this.value = calculateValue(rank);
    }

    private int calculateValue(String rank) {
        switch (rank) {
            case "a": return 1;
            case "j": return 11;
            case "q": return 12;
            case "k": return 13;
            default: return Integer.parseInt(rank);
        }
    }

    public int getValue() { return value; }
    public String getRank() { return rank; }
    public String getSuit() { return suit; }

    @Override
    public String toString() {
        return rank + suit;
    }
}