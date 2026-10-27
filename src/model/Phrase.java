package model;

// Encapsulates a phrase entry with translation, transliteration, category, and verification status.
public class Phrase {

    private int id; // 0 = not saved yet
    private String phrase; // text in the local language (Devanagari script)
    private String translation; // English meaning
    private String transliteration; // Latin spelling, may be "" (optional)
    private String category; // e.g. "Travel"
    private boolean verified;

    // Default constructor.
    public Phrase() {
    }

    // Constructor for a new phrase entry, defaulting id to 0 and verified to false.
    public Phrase(String phrase, String translation, String transliteration, String category) {
        this(0, phrase, translation, transliteration, category, false);
    }

    // Overloaded constructor for creating a phrase with specified verification status and id 0.
    public Phrase(String phrase, String translation, String transliteration, String category, boolean verified) {
        this(0, phrase, translation, transliteration, category, verified);
    }

    // Full constructor initializing all fields of a phrase.
    public Phrase(int id, String phrase, String translation, String transliteration,
                  String category, boolean verified) {
        this.id = id;
        this.phrase = phrase;
        this.translation = translation;
        this.transliteration = transliteration == null ? "" : transliteration;
        this.category = category;
        this.verified = verified;
    }

    // Gets the unique database ID of the phrase.
    public int getId() {
        return id;
    }

    // Sets the unique database ID of the phrase.
    public void setId(int id) {
        this.id = id;
    }

    // Gets the phrase text in the local language.
    public String getPhrase() {
        return phrase;
    }

    // Sets the phrase text in the local language.
    public void setPhrase(String phrase) {
        this.phrase = phrase;
    }

    // Gets the English translation of the phrase.
    public String getTranslation() {
        return translation;
    }

    // Sets the English translation of the phrase.
    public void setTranslation(String translation) {
        this.translation = translation;
    }

    // Gets the transliteration in Latin script.
    public String getTransliteration() {
        return transliteration;
    }

    // Sets the transliteration in Latin script.
    public void setTransliteration(String transliteration) {
        this.transliteration = transliteration == null ? "" : transliteration;
    }

    // Gets the category or situation of the phrase.
    public String getCategory() {
        return category;
    }

    // Sets the category or situation of the phrase.
    public void setCategory(String category) {
        this.category = category;
    }

    // Returns whether the phrase has been verified.
    public boolean isVerified() {
        return verified;
    }

    // Sets whether the phrase is verified.
    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    // Returns a formatted string representation of the phrase.
    @Override
    public String toString() {
        String transPart = (transliteration != null && !transliteration.trim().isEmpty())
                ? " (" + transliteration.trim() + ")"
                : "";
        return "[" + id + "] " + phrase + transPart + " = " + translation + " | " + category + " | " + (verified ? "VERIFIED" : "UNVERIFIED");
    }
}
