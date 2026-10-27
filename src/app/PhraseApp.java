package app;

import dao.PhraseDao;
import dao.PhraseDaoImpl;
import dao.ResultDao;
import dao.ResultDaoImpl;
import exception.DuplicatePhraseException;
import exception.InvalidInputException;
import exception.PhraseNotFoundException;
import model.CategoryStat;
import model.Phrase;
import model.QuizResult;
import service.PhraseService;
import service.ProgressService;
import service.Question;
import service.QuizService;
import util.CsvUtil;

import java.io.IOException;
import java.util.List;

// Facade that the future UI will call; delegates to services and DAOs.
public class PhraseApp {

    private final PhraseService phraseService;
    private final QuizService quizService;
    private final ProgressService progressService;

    // Default constructor that connects to the real MySQL database.
    public PhraseApp() {
        this(new PhraseDaoImpl(), new ResultDaoImpl());
    }

    // Testing constructor that accepts any DAO implementation.
    public PhraseApp(PhraseDao phraseDao, ResultDao resultDao) {
        phraseService = new PhraseService(phraseDao);
        quizService = new QuizService(phraseService);
        progressService = new ProgressService(resultDao);
    }

    // Returns all phrases from the database.
    public List<Phrase> getAllPhrases() {
        return phraseService.getAllPhrases();
    }

    // Returns unique sorted category names.
    public List<String> getCategories() {
        return phraseService.getCategories();
    }

    // Searches phrases by category (blank or "ALL" returns everything).
    public List<Phrase> searchByCategory(String category) {
        return phraseService.searchByCategory(category);
    }

    // Searches phrases by keyword across all text fields.
    public List<Phrase> searchByKeyword(String keyword) throws InvalidInputException {
        return phraseService.searchByKeyword(keyword);
    }

    // Validates and adds a new unverified phrase.
    public void addPhrase(Phrase p) throws InvalidInputException, DuplicatePhraseException {
        phraseService.addPhrase(p);
    }

    // Validates and updates an existing phrase, keeping its verified status.
    public void correctPhrase(Phrase p) throws InvalidInputException, PhraseNotFoundException,
            DuplicatePhraseException {
        phraseService.correctPhrase(p);
    }

    // Sets the verified flag for a phrase by its ID.
    public void setVerified(int id, boolean verified) throws PhraseNotFoundException {
        phraseService.setVerified(id, verified);
    }

    // Creates a new quiz with the given parameters.
    public List<Question> startQuiz(String category, int count, String mode) throws InvalidInputException {
        return quizService.createQuiz(category, count, mode);
    }

    // Evaluates quiz answers, saves the result, and returns it.
    public QuizResult submitQuiz(List<Question> questions, List<String> answers) throws InvalidInputException {
        QuizResult r = quizService.evaluate(questions, answers);
        progressService.saveResult(r);
        return r;
    }

    // Returns per-category progress summaries.
    public List<CategoryStat> getProgress() {
        return progressService.getCategorySummary();
    }

    // Returns the category with the lowest accuracy, or null if no data.
    public CategoryStat getWeakestCategory() {
        return progressService.getWeakestCategory();
    }

    // Imports phrases from a CSV file, skipping duplicates; returns how many were added.
    public int importCsv(String path) throws IOException, InvalidInputException {
        List<Phrase> imported = CsvUtil.importPhrases(path);
        int added = 0;
        for (Phrase p : imported) {
            try {
                phraseService.addPhrase(p);
                added++;
            } catch (DuplicatePhraseException | InvalidInputException e) {
                // skip duplicates and invalid entries silently
            }
        }
        return added;
    }

    // Exports all phrases to a CSV file.
    public void exportCsv(String path) throws IOException {
        CsvUtil.exportPhrases(phraseService.getAllPhrases(), path);
    }

    // Optional innovation: starts a quiz focused on the user's weakest category (falls back to ALL if no data).
    public List<Question> startWeakCategoryQuiz(int count, String mode) throws InvalidInputException {
        CategoryStat weakest = getWeakestCategory();
        String cat = (weakest != null) ? weakest.getCategory() : "ALL";
        return startQuiz(cat, count, mode);
    }

    // Optional innovation: returns a deterministic phrase of the day from verified phrases.
    public Phrase getPhraseOfTheDay() {
        List<Phrase> verified = phraseService.getVerifiedPhrases();
        if (verified.isEmpty()) {
            return null;
        }
        int index = java.time.LocalDate.now().getDayOfYear() % verified.size();
        return verified.get(index);
    }
}
