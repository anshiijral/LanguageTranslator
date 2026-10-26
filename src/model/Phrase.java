package model;

public class Phrase {
    private int id;
    private String phrase;
    private String translation;
    private String transliteration;
    private String category;
    private boolean verified;

    public Phrase(int id, String phrase, String translation,
                  String transliteration, String category, boolean verified) {
        this.id = id;
        this.phrase = phrase;
        this.translation = translation;
        this.transliteration = transliteration;
        this.category = category;
        this.verified = verified;
    }

    public Phrase(String phrase, String translation,
                  String transliteration, String category, boolean verified) {
        this(0, phrase, translation, transliteration, category, verified);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPhrase() { return phrase; }
    public void setPhrase(String phrase) { this.phrase = phrase; }

    public String getTranslation() { return translation; }
    public void setTranslation(String translation) { this.translation = translation; }

    public String getTransliteration() { return transliteration; }
    public void setTransliteration(String transliteration) { this.transliteration = transliteration; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
}
