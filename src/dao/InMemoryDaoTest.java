package dao;

import model.Phrase;

public class InMemoryDaoTest {
    public static void main(String[] args) {
        PhraseDao dao = new InMemoryPhraseDao();

        System.out.println("Total sample phrases: " + dao.findAll().size());
        System.out.println("Greetings: " + dao.findByCategory("greetings").size());
        System.out.println("Food: " + dao.findByCategory("FOOD").size());

        int verified = 0;
        int unverified = 0;

        for (Phrase p : dao.findAll()) {
            if (p.isVerified()) verified++;
            else unverified++;
        }

        System.out.println("Verified: " + verified);
        System.out.println("Unverified: " + unverified);
    }
}
