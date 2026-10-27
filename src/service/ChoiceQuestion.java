package service;

import model.Phrase;
import java.util.ArrayList;
import java.util.List;

// A multiple-choice question: one correct option plus up to 3 wrong ones.
public class ChoiceQuestion extends Question {

    private final List<String> options;

    // Keep a copy of the options so outside code cannot change them.
    public ChoiceQuestion(Phrase phrase, List<String> options) {
        super(phrase);
        this.options = new ArrayList<>(options);
    }

    // The options to show (already shuffled by QuizService).
    public List<String> getOptions() {
        return new ArrayList<>(options);
    }

    // Same comparison as typing; the UI passes the TEXT of the chosen option.
    @Override
    public boolean checkAnswer(String answer) {
        return normalize(answer).equals(normalize(getCorrectAnswer()));
    }
}
