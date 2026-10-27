package dao;

import java.util.List;
import model.QuizResult;

// Data access interface for quiz results (mirrors PhraseDao's pattern).
public interface ResultDao {

    // Inserts one quiz result row.
    void save(QuizResult r);

    // Returns all saved quiz results, oldest first.
    List<QuizResult> findAll();
}
