package dao;

import model.Phrase;
import java.util.ArrayList;
import java.util.List;

public class InMemoryPhraseDao implements PhraseDao {
    private final List<Phrase> phrases = new ArrayList<>();
    private int nextId = 1;

    public InMemoryPhraseDao() {
        insert(new Phrase("नमस्ते", "Hello", "Namaste", "Greetings", true));
        insert(new Phrase("धन्यवाद", "Thank you", "Dhanyavaad", "Greetings", true));
        insert(new Phrase("आप कैसे हैं?", "How are you?", "Aap kaise hain?", "Greetings", true));
        insert(new Phrase("पानी दीजिए।", "Please give me water", "Paani dijiye", "Food", false));
        insert(new Phrase("मुझे भूख लगी है।", "I am hungry", "Mujhe bhookh lagi hai", "Food", false));
        insert(new Phrase("मदद कीजिए!", "Help me!", "Madad kijiye", "Emergency", false));
    }

    @Override
    public void insert(Phrase p) {
        p.setId(nextId++);
        phrases.add(p);
    }

    public void clear() {
        phrases.clear();
        nextId = 1;
    }

    @Override
    public boolean update(Phrase p) {
        for (int i = 0; i < phrases.size(); i++) {
            if (phrases.get(i).getId() == p.getId()) {
                phrases.set(i, p);
                return true;
            }
        }
        return false;
    }

    @Override
    public Phrase findById(int id) {
        for (Phrase p : phrases) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    @Override
    public List<Phrase> findAll() {
        return new ArrayList<>(phrases);
    }

    @Override
    public List<Phrase> findByCategory(String category) {
        List<Phrase> result = new ArrayList<>();
        for (Phrase p : phrases) {
            if (p.getCategory().equalsIgnoreCase(category)) {
                result.add(p);
            }
        }
        return result;
    }
}
