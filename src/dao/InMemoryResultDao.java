package dao;

import model.QuizResult;
import java.util.ArrayList;
import java.util.List;

// In-memory implementation of ResultDao using an ArrayList (no database needed).
public class InMemoryResultDao implements ResultDao {

    private final List<QuizResult> results = new ArrayList<>();

    // Adds the quiz result to the in-memory list.
    @Override
    public void save(QuizResult r) {
        results.add(r);
    }

    // Returns a copy of all stored results in insertion order (oldest first).
    @Override
    public List<QuizResult> findAll() {
        return new ArrayList<>(results);
    }
}
