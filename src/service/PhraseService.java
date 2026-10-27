package service;

import dao.PhraseDao;
import exception.DuplicatePhraseException;
import exception.InvalidInputException;
import exception.PhraseNotFoundException;
import model.Phrase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

// Provides business logic for managing, validating, and searching phrases.
public class PhraseService {

    private final PhraseDao dao;

    // Initializes PhraseService with a data access object.
    public PhraseService(PhraseDao dao) {
        this.dao = dao;
    }

    // Validates, trims, and inserts a new unverified phrase after checking for duplicates.
    public void addPhrase(Phrase p) throws InvalidInputException, DuplicatePhraseException {
        validate(p);
        trimFields(p);
        if (isDuplicate(p.getPhrase(), 0)) {
            throw new DuplicatePhraseException("Phrase already exists: " + p.getPhrase());
        }
        p.setVerified(false);
        dao.insert(p);
    }

    // Validates, trims, and updates an existing phrase keeping its verification status.
    public void correctPhrase(Phrase p) throws InvalidInputException, PhraseNotFoundException, DuplicatePhraseException {
        validate(p);
        Phrase old = dao.findById(p.getId());
        if (old == null) {
            throw new PhraseNotFoundException("No phrase with id " + p.getId());
        }
        trimFields(p);
        if (isDuplicate(p.getPhrase(), p.getId())) {
            throw new DuplicatePhraseException("Phrase already exists: " + p.getPhrase());
        }
        p.setVerified(old.isVerified());
        dao.update(p);
    }

    // Flips the verification flag for a phrase identified by its ID.
    public void setVerified(int id, boolean v) throws PhraseNotFoundException {
        Phrase p = dao.findById(id);
        if (p == null) {
            throw new PhraseNotFoundException("No phrase with id " + id);
        }
        p.setVerified(v);
        dao.update(p);
    }

    // Retrieves a single phrase by its ID or throws an exception if not found.
    public Phrase getById(int id) throws PhraseNotFoundException {
        Phrase p = dao.findById(id);
        if (p == null) {
            throw new PhraseNotFoundException("No phrase with id " + id);
        }
        return p;
    }

    // Returns a list of all phrases currently stored.
    public List<Phrase> getAllPhrases() {
        return dao.findAll();
    }

    // Returns only phrases that have been verified.
    public List<Phrase> getVerifiedPhrases() {
        return dao.findAll().stream()
                .filter(Phrase::isVerified)
                .collect(Collectors.toList());
    }

    // Returns unique categories sorted alphabetically, preserving original casing.
    public List<String> getCategories() {
        Map<String, String> map = new TreeMap<>();
        for (Phrase p : dao.findAll()) {
            if (p.getCategory() != null && !p.getCategory().trim().isEmpty()) {
                String cat = p.getCategory().trim();
                map.putIfAbsent(cat.toLowerCase(), cat);
            }
        }
        TreeSet<String> set = new TreeSet<>(map.values());
        return new ArrayList<>(set);
    }

    // Searches phrases matching a category, returning all phrases if blank or 'ALL'.
    public List<Phrase> searchByCategory(String category) {
        if (category == null || category.trim().isEmpty() || category.trim().equalsIgnoreCase("ALL")) {
            return dao.findAll();
        }
        return dao.findByCategory(category.trim());
    }

    // Searches phrases across phrase, translation, and transliteration fields by keyword.
    public List<Phrase> searchByKeyword(String keyword) throws InvalidInputException {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new InvalidInputException("Keyword cannot be empty");
        }
        String kw = keyword.trim().toLowerCase();
        List<Phrase> result = new ArrayList<>();
        for (Phrase p : dao.findAll()) {
            String ph = p.getPhrase() != null ? p.getPhrase().toLowerCase() : "";
            String tr = p.getTranslation() != null ? p.getTranslation().toLowerCase() : "";
            String tl = p.getTransliteration() != null ? p.getTransliteration().toLowerCase() : "";
            if (ph.contains(kw) || tr.contains(kw) || tl.contains(kw)) {
                result.add(p);
            }
        }
        return result;
    }

    // Validates non-null, non-empty, and length constraints on phrase fields according to rule R1.
    private void validate(Phrase p) throws InvalidInputException {
        if (p == null) {
            throw new InvalidInputException("Phrase is missing");
        }
        if (isBlank(p.getPhrase())) {
            throw new InvalidInputException("Phrase text is required");
        }
        if (isBlank(p.getTranslation())) {
            throw new InvalidInputException("Translation is required");
        }
        if (isBlank(p.getCategory())) {
            throw new InvalidInputException("Category is required");
        }
        if (p.getTransliteration() == null) {
            p.setTransliteration("");
        }
        if (p.getPhrase().trim().length() > 200) {
            throw new InvalidInputException("Phrase text exceeds 200 characters");
        }
        if (p.getTranslation().trim().length() > 200) {
            throw new InvalidInputException("Translation exceeds 200 characters");
        }
        if (p.getTransliteration().trim().length() > 200) {
            throw new InvalidInputException("Transliteration exceeds 200 characters");
        }
        if (p.getCategory().trim().length() > 50) {
            throw new InvalidInputException("Category exceeds 50 characters");
        }
    }

    // Trims leading and trailing spaces from all text fields of a phrase in place.
    private void trimFields(Phrase p) {
        p.setPhrase(p.getPhrase().trim());
        p.setTranslation(p.getTranslation().trim());
        p.setTransliteration(p.getTransliteration() == null ? "" : p.getTransliteration().trim());
        p.setCategory(p.getCategory().trim());
    }

    // Checks whether a string is null or consists only of whitespace.
    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    // Checks whether another phrase in the database has the same phrase text ignoring case.
    private boolean isDuplicate(String text, int ignoreId) {
        if (text == null) {
            return false;
        }
        for (Phrase x : dao.findAll()) {
            if (x.getId() != ignoreId && x.getPhrase() != null && x.getPhrase().trim().equalsIgnoreCase(text.trim())) {
                return true;
            }
        }
        return false;
    }
}
