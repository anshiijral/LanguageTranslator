package util;

import exception.InvalidInputException;
import model.Phrase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

// Reads and writes phrase data in quoted CSV format with UTF-8 encoding.
public class CsvUtil {

    // Exports the given phrases to a CSV file with a header row.
    public static void exportPhrases(List<Phrase> phrases, String filePath) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {

            bw.write("id,phrase,translation,transliteration,category,verified");
            bw.newLine();

            for (Phrase p : phrases) {
                bw.write(quote(String.valueOf(p.getId())));
                bw.write(",");
                bw.write(quote(p.getPhrase()));
                bw.write(",");
                bw.write(quote(p.getTranslation()));
                bw.write(",");
                bw.write(quote(p.getTransliteration() == null ? "" : p.getTransliteration()));
                bw.write(",");
                bw.write(quote(p.getCategory()));
                bw.write(",");
                bw.write(quote(String.valueOf(p.isVerified())));
                bw.newLine();
            }
        }
    }

    // Imports phrases from a CSV file; each row becomes a new unverified Phrase with id 0.
    public static List<Phrase> importPhrases(String filePath) throws IOException, InvalidInputException {
        List<Phrase> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {

            String line = br.readLine(); // skip header
            int lineNum = 1;

            while ((line = br.readLine()) != null) {
                lineNum++;
                if (line.trim().isEmpty()) {
                    continue;
                }
                List<String> parts = splitLine(line);
                if (parts.size() < 6) {
                    throw new InvalidInputException("Bad CSV format on line " + lineNum);
                }
                // parts: id, phrase, translation, transliteration, category, verified
                String phrase = parts.get(1);
                String translation = parts.get(2);
                String transliteration = parts.get(3);
                String category = parts.get(4);
                list.add(new Phrase(phrase, translation, transliteration, category));
            }
        }
        return list;
    }

    // Wraps a value in double quotes, doubling any existing quotes inside.
    private static String quote(String value) {
        if (value == null) {
            value = "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    // Splits a CSV line respecting quoted fields that may contain commas.
    private static List<String> splitLine(String line) {
        List<String> parts = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                parts.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        parts.add(cur.toString());
        return parts;
    }
}
