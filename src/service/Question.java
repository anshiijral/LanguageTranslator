package service;

import model.Phrase;

// Base class for one quiz question. Subclasses decide HOW an answer is checked.
public abstract class Question {

    protected Phrase phrase;

    // Remember which phrase this question is about.
    public Question(Phrase phrase) {
        this.phrase = phrase;
    }

    // Text shown to the learner, e.g. "Translate: नमस्ते (namaste)".
    public String getText() {
        String t = "Translate: " + phrase.getPhrase();
        if (phrase.getTransliteration() != null && !phrase.getTransliteration().isEmpty()) {
            t += " (" + phrase.getTransliteration() + ")";
        }
        return t;
    }

    // Category of the phrase (used to decide the category of the quiz result).
    public String getCategory() {
        return phrase.getCategory();
    }

    // The right answer is the English translation.
    public String getCorrectAnswer() {
        return phrase.getTranslation();
    }

    // Each subclass decides whether the given answer is correct.
    public abstract boolean checkAnswer(String answer);

    // Rule R9: trim, lower-case, collapse spaces, drop trailing . ? !
    protected static String normalize(String s) {
        if (s == null) {
            return "";
        }
        String t = s.trim().toLowerCase().replaceAll("\\s+", " ");
        return t.replaceAll("[.?!]+$", "");
    }
}
