package service;

import model.Phrase;

// A question where the learner TYPES the English translation.
public class TypingQuestion extends Question {

    // Pass the phrase up to the parent class.
    public TypingQuestion(Phrase phrase) {
        super(phrase);
    }

    // Correct if the typed text equals the translation after normalising both.
    @Override
    public boolean checkAnswer(String answer) {
        return normalize(answer).equals(normalize(getCorrectAnswer()));
    }
}
