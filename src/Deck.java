import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Collections;
class Deck {
    private final List<Card> cards = new ArrayList<>();
    private final Random random = new Random();

    public Deck() {
        initializeDeck();
        shuffle();
    }

    private void initializeDeck() {
        String[] suits = {"Hearts", "Diamonds", "Clubs", "Spades"};
        String[] ranks = {"Ace", "2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King"};

        for (String suit : suits) {
            for (String rank : ranks) {
                cards.add(new Card(rank, suit));
            }
        }
    }

    public void shuffle() {
        Collections.shuffle(cards, random);
    }

    public List<Card> drawFourCards() {
        List<Card> hand = new ArrayList<>();
        for (int i = 0; i < 4 && !cards.isEmpty(); i++) {
            hand.add(cards.remove(0));
        }
        return hand;
    }

}