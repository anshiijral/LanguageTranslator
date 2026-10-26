package dao;

import model.Phrase;

public class DaoTest {
    public static void main(String[] args) {
        PhraseDao dao = new PhraseDaoImpl();

        // 1. Use an existing phrase if already inserted; otherwise insert it.
        Phrase phrase = dao.findAll().stream()
            .filter(p -> p.getPhrase().equals("फिर मिलेंगे"))
            .findFirst()
            .orElse(null);

        if (phrase == null) {
            phrase = new Phrase(
                "फिर मिलेंगे", "See you again",
                "Phir milenge", "Greetings", true
            );
            dao.insert(phrase);
            System.out.println("Inserted phrase ID: " + phrase.getId());
        } else {
            System.out.println("Phrase already exists; ID: " + phrase.getId());
        }

        // 2. Find by ID
        Phrase found = dao.findById(phrase.getId());
        System.out.println("Found phrase: " +
            (found != null ? found.getPhrase() : "Not found"));

        // 3. Update translation
        phrase.setTranslation("See you again!");
        boolean updated = dao.update(phrase);
        System.out.println("Updated: " + updated);

        Phrase updatedPhrase = dao.findById(phrase.getId());
        System.out.println("Updated translation: " +
            (updatedPhrase != null ? updatedPhrase.getTranslation() : "Not found"));

        // 4. Find all
        System.out.println("Total phrases in database: " + dao.findAll().size());

        // 5. Find by category (case-insensitive)
        System.out.println("Travel phrases: " +
            dao.findByCategory("travel").size());
    }
}
