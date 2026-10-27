package service;

import exception.InvalidInputException;
import model.Phrase;
import model.QuizResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Builds random quizzes from verified phrases and scores the answers.
public class QuizService {

    private final PhraseService phraseService;

    // The quiz gets its phrases from PhraseService (never from the database directly).
    public QuizService(PhraseService phraseService) {
        this.phraseService = phraseService;
    }

    // Creates a random quiz. mode = "TYPING" or "CHOICE"; category may be a name or "ALL".
    public List<Question> createQuiz(String category, int count, String mode)
            throws InvalidInputException {

        // Step 1: check mode and count
        if (mode == null) {
            throw new InvalidInputException("Quiz mode is required (TYPING or CHOICE)");
        }
        boolean typing = mode.trim().equalsIgnoreCase("TYPING");
        boolean choice = mode.trim().equalsIgnoreCase("CHOICE");
        if (!typing && !choice) {
            throw new InvalidInputException("Quiz mode must be TYPING or CHOICE");
        }
        if (count < 1) {
            throw new InvalidInputException("Number of questions must be at least 1");
        }

        // Step 2: build the pool = verified phrases of that category (a NEW list)
        List<Phrase> all = phraseService.getVerifiedPhrases();
        List<Phrase> pool = new ArrayList<>();
        boolean everything = category == null || category.trim().isEmpty()
                || category.trim().equalsIgnoreCase("ALL");
        for (Phrase p : all) {
            if (everything || p.getCategory().equalsIgnoreCase(category.trim())) {
                pool.add(p);
            }
        }

        // Step 3: not enough data?
        if (pool.isEmpty()) {
            throw new InvalidInputException("No verified phrases found for category: " + category);
        }
        if (choice && all.size() < 2) {
            throw new InvalidInputException("Multiple choice needs at least 2 verified phrases");
        }

        // Step 4: shuffle and take the first n
        Collections.shuffle(pool);
        int n = Math.min(count, pool.size());

        // Step 5: turn each chosen phrase into a Question object
        List<Question> questions = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Phrase p = pool.get(i);
            if (typing) {
                questions.add(new TypingQuestion(p));
            } else {
                questions.add(new ChoiceQuestion(p, buildOptions(p, all)));
            }
        }
        return questions;
    }

    // Counts correct answers and returns a QuizResult (it is NOT saved here).
    public QuizResult evaluate(List<Question> questions, List<String> answers)
            throws InvalidInputException {

        if (questions == null || answers == null) {
            throw new InvalidInputException("Questions and answers are required");
        }
        if (questions.size() != answers.size()) {
            throw new InvalidInputException("Number of answers must match number of questions");
        }
        if (questions.isEmpty()) {
            throw new InvalidInputException("There are no questions to evaluate");
        }

        int correct = 0;
        for (int i = 0; i < questions.size(); i++) {
            // Polymorphism: the right checkAnswer() (typing or choice) runs automatically
            if (questions.get(i).checkAnswer(answers.get(i))) {
                correct++;
            }
        }

        // Category of the result: same for all questions, otherwise "ALL"
        String category = questions.get(0).getCategory();
        for (Question q : questions) {
            if (!q.getCategory().equalsIgnoreCase(category)) {
                category = "ALL";
                break;
            }
        }
        return new QuizResult(category, correct, questions.size());
    }

    // Correct translation + up to 3 DIFFERENT wrong translations, shuffled (rule R10).
    private List<String> buildOptions(Phrase correct, List<Phrase> all) {
        List<String> wrong = new ArrayList<>();
        for (Phrase x : all) {
            String t = x.getTranslation();
            boolean sameAsCorrect = t.trim().equalsIgnoreCase(correct.getTranslation().trim());
            boolean alreadyIn = false;
            for (String w : wrong) {
                if (w.trim().equalsIgnoreCase(t.trim())) {
                    alreadyIn = true;
                }
            }
            if (!sameAsCorrect && !alreadyIn) {
                wrong.add(t);
            }
        }
        Collections.shuffle(wrong);
        List<String> options = new ArrayList<>(wrong.subList(0, Math.min(3, wrong.size())));
        options.add(correct.getTranslation());
        Collections.shuffle(options);
        return options;
    }
}
