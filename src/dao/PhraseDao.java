package dao;

import java.util.List;
import model.Phrase;

public interface PhraseDao {
    void insert(Phrase p);
    boolean update(Phrase p);
    Phrase findById(int id);
    List<Phrase> findAll();
    List<Phrase> findByCategory(String category);
}
