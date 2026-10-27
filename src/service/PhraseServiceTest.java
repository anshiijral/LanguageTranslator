package service;

import dao.InMemoryPhraseDao;
import exception.DuplicatePhraseException;
import exception.InvalidInputException;
import exception.PhraseNotFoundException;
import model.Phrase;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Console test harness for Member 2's PhraseService and Phrase model.
public class PhraseServiceTest {

    private static int failedCount = 0;

    // Helper method to report PASS or FAIL for an individual test case.
    private static void check(String testName, boolean condition) {
        if (condition) {
            System.out.println("PASS: " + testName);
        } else {
            System.out.println("FAIL: " + testName);
            failedCount++;
        }
    }

    // Runs unit-style test checks for Member 2 requirements.
    public static void main(String[] args) {
        PhraseService service = new PhraseService(new InMemoryPhraseDao());

        // 1. Add valid phrase (starts unverified)
        boolean addValidPassed = false;
        try {
            Phrase p = new Phrase("शुभ प्रभात", "Good morning", "Shubh prabhat", "Greetings");
            service.addPhrase(p);
            Phrase retrieved = service.getById(p.getId());
            addValidPassed = (retrieved != null && !retrieved.isVerified() && retrieved.getPhrase().equals("शुभ प्रभात"));
        } catch (Exception e) {
            addValidPassed = false;
        }
        check("add valid", addValidPassed);

        // 2. Add duplicate phrase (different case / surrounding spaces)
        boolean addDuplicatePassed = false;
        try {
            service.addPhrase(new Phrase("  नमस्ते  ", "Hello again", "Namaste", "Greetings"));
        } catch (DuplicatePhraseException e) {
            addDuplicatePassed = true;
        } catch (Exception e) {
            addDuplicatePassed = false;
        }
        check("add duplicate (different case)", addDuplicatePassed);

        // 3. Add empty translation throws InvalidInputException
        boolean emptyTranslationPassed = false;
        try {
            service.addPhrase(new Phrase("अलविदा", "   ", "Alvida", "Greetings"));
        } catch (InvalidInputException e) {
            emptyTranslationPassed = true;
        } catch (Exception e) {
            emptyTranslationPassed = false;
        }
        check("add empty translation", emptyTranslationPassed);

        // 4. Correct unknown ID throws PhraseNotFoundException
        boolean correctUnknownPassed = false;
        try {
            Phrase unknown = new Phrase(9999, "कुछ", "Something", "Kuch", "Food", false);
            service.correctPhrase(unknown);
        } catch (PhraseNotFoundException e) {
            correctUnknownPassed = true;
        } catch (Exception e) {
            correctUnknownPassed = false;
        }
        check("correct unknown id", correctUnknownPassed);

        // Add phrases needed for keyword and travel category tests
        try {
            service.addPhrase(new Phrase("मुझे पानी चाहिए", "I need water", "Mujhe paani chaahiye", "Food"));
            service.addPhrase(new Phrase("बस स्टॉप कहाँ है?", "Where is the bus stop?", "Bus stop kahaan hai?", "Travel"));
        } catch (Exception e) {
            System.out.println("Setup error adding test phrases: " + e.getMessage());
        }

        // 5. Keyword "WATER" finds "I need water" (case-insensitive search)
        boolean keywordWaterPassed = false;
        try {
            List<Phrase> waterResults = service.searchByKeyword("WATER");
            boolean foundTarget = false;
            for (Phrase p : waterResults) {
                if (p.getTranslation().toLowerCase().contains("i need water")) {
                    foundTarget = true;
                    break;
                }
            }
            keywordWaterPassed = foundTarget;
        } catch (Exception e) {
            keywordWaterPassed = false;
        }
        check("keyword \"WATER\" finds \"I need water\"", keywordWaterPassed);

        // 6. Category "travel" (lower case) works
        boolean categoryTravelPassed = false;
        try {
            List<Phrase> travelPhrases = service.searchByCategory("travel");
            categoryTravelPassed = (travelPhrases != null && !travelPhrases.isEmpty());
        } catch (Exception e) {
            categoryTravelPassed = false;
        }
        check("category \"travel\" (lower case) works", categoryTravelPassed);

        // 7. getCategories() has no repeats and is sorted
        boolean getCategoriesPassed = false;
        try {
            List<String> categories = service.getCategories();
            Set<String> set = new HashSet<>(categories);
            boolean noRepeats = (categories.size() == set.size());
            boolean isSorted = true;
            for (int i = 0; i < categories.size() - 1; i++) {
                if (categories.get(i).compareToIgnoreCase(categories.get(i + 1)) > 0) {
                    isSorted = false;
                    break;
                }
            }
            getCategoriesPassed = noRepeats && isSorted && !categories.isEmpty();
        } catch (Exception e) {
            getCategoriesPassed = false;
        }
        check("getCategories() has no repeats", getCategoriesPassed);

        // Summary
        System.out.println();
        if (failedCount == 0) {
            System.out.println("ALL MEMBER 2 TESTS PASSED");
        } else {
            System.out.println(failedCount + " TEST(S) FAILED");
        }
    }
}
