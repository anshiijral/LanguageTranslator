package app;

import dao.InMemoryPhraseDao;
import dao.InMemoryResultDao;
import exception.DatabaseException;
import exception.DuplicatePhraseException;
import exception.InvalidInputException;
import exception.PhraseNotFoundException;
import model.CategoryStat;
import model.Phrase;
import model.QuizResult;
import service.ChoiceQuestion;
import service.Question;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// Console menu that exercises every feature of PhraseApp for testing and demo.
public class Main {

    private static Scanner scanner;
    private static PhraseApp app;

    // Entry point: sets UTF-8 output, creates PhraseApp and runs the menu loop.
    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
        } catch (Exception e) {
            // fall back to default encoding
        }

        scanner = new Scanner(System.in, "UTF-8");

        // Use in-memory DAOs for testing; replace with new PhraseApp() for MySQL
        app = new PhraseApp(new InMemoryPhraseDao(), new InMemoryResultDao());

        System.out.println("===== Local Language Phrase Practice (Hindi) =====");

        while (true) {
            printMenu();
            int choice = readInt("Choose: ");
            switch (choice) {
                case 1:  viewAllPhrases();     break;
                case 2:  searchByCategory();   break;
                case 3:  searchByKeyword();    break;
                case 4:  takeQuiz();           break;
                case 5:  viewProgress();       break;
                case 6:  addPhrase();          break;
                case 7:  correctPhrase();      break;
                case 8:  verifyPhrase();       break;
                case 9:  importCsv();          break;
                case 10: exportCsv();          break;
                case 0:
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    // Prints the main menu options.
    private static void printMenu() {
        System.out.println();
        System.out.println(" 1. View all phrases            6. Add a phrase");
        System.out.println(" 2. Search by category          7. Correct a phrase");
        System.out.println(" 3. Search by keyword           8. Verify / unverify a phrase");
        System.out.println(" 4. Take a quiz                 9. Import phrases from CSV");
        System.out.println(" 5. View progress              10. Export phrases to CSV");
        System.out.println(" 0. Exit");
    }

    // Displays every phrase in the database.
    private static void viewAllPhrases() {
        try {
            List<Phrase> phrases = app.getAllPhrases();
            System.out.println("Total phrases: " + phrases.size());
            for (Phrase p : phrases) {
                System.out.println(p);
            }
        } catch (DatabaseException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // Prompts for a category and displays matching phrases.
    private static void searchByCategory() {
        try {
            List<String> categories = app.getCategories();
            System.out.println("Available categories: " + categories);
            System.out.print("Enter category (or ALL): ");
            String cat = scanner.nextLine().trim();
            List<Phrase> results = app.searchByCategory(cat);
            System.out.println("Found " + results.size() + " phrase(s):");
            for (Phrase p : results) {
                System.out.println(p);
            }
        } catch (DatabaseException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // Prompts for a keyword and displays matching phrases.
    private static void searchByKeyword() {
        try {
            System.out.print("Enter keyword: ");
            String kw = scanner.nextLine().trim();
            List<Phrase> results = app.searchByKeyword(kw);
            System.out.println("Found " + results.size() + " phrase(s):");
            for (Phrase p : results) {
                System.out.println(p);
            }
        } catch (InvalidInputException e) {
            System.out.println(e.getMessage());
        } catch (DatabaseException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // Runs a full quiz session: category selection, question loop, scoring.
    private static void takeQuiz() {
        try {
            List<String> categories = app.getCategories();
            System.out.println("Categories: " + categories + " or ALL");
            System.out.print("Enter category: ");
            String cat = scanner.nextLine().trim();

            int count = readInt("Number of questions: ");

            System.out.println("Mode: 1 = Typing, 2 = Multiple Choice");
            int modeChoice = readInt("Choose mode: ");
            String mode = (modeChoice == 2) ? "CHOICE" : "TYPING";

            List<Question> questions = app.startQuiz(cat, count, mode);
            List<String> answers = new ArrayList<>();

            for (int i = 0; i < questions.size(); i++) {
                Question q = questions.get(i);
                System.out.println();
                System.out.println("Q" + (i + 1) + "/" + questions.size() + ": " + q.getText());

                if (q instanceof ChoiceQuestion) {
                    ChoiceQuestion cq = (ChoiceQuestion) q;
                    List<String> options = cq.getOptions();
                    for (int j = 0; j < options.size(); j++) {
                        System.out.println("  " + (j + 1) + ". " + options.get(j));
                    }
                    int pick = readInt("Your choice (number): ");
                    if (pick >= 1 && pick <= options.size()) {
                        answers.add(options.get(pick - 1));
                    } else {
                        answers.add("");
                    }
                } else {
                    System.out.print("Your answer: ");
                    answers.add(scanner.nextLine());
                }
            }

            QuizResult result = app.submitQuiz(questions, answers);
            System.out.println();
            System.out.printf("Score: %d/%d (%.1f%%)%n",
                    result.getCorrect(), result.getTotal(), result.getPercentage());

            // Show wrong answers
            for (int i = 0; i < questions.size(); i++) {
                if (!questions.get(i).checkAnswer(answers.get(i))) {
                    System.out.println("  Wrong: \"" + answers.get(i) + "\" -> Correct: \""
                            + questions.get(i).getCorrectAnswer() + "\"");
                }
            }
        } catch (InvalidInputException e) {
            System.out.println(e.getMessage());
        } catch (DatabaseException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // Displays the progress summary table and weakest category.
    private static void viewProgress() {
        try {
            List<CategoryStat> stats = app.getProgress();
            if (stats.isEmpty()) {
                System.out.println("No quizzes taken yet.");
                return;
            }
            System.out.printf("%-15s %8s %15s %10s%n", "Category", "Quizzes", "Correct/Total", "Accuracy");
            System.out.println("-".repeat(50));
            for (CategoryStat s : stats) {
                System.out.printf("%-15s %8d %7d/%-7d %9.1f%%%n",
                        s.getCategory(), s.getQuizzes(), s.getCorrect(), s.getTotal(), s.getAccuracy());
            }
            CategoryStat weakest = app.getWeakestCategory();
            if (weakest != null) {
                System.out.printf("Weakest category: %s (%.1f%%)%n",
                        weakest.getCategory(), weakest.getAccuracy());
            }
        } catch (DatabaseException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // Prompts for phrase details and adds a new unverified entry.
    private static void addPhrase() {
        try {
            System.out.print("Hindi phrase: ");
            String phrase = scanner.nextLine();
            System.out.print("English translation: ");
            String translation = scanner.nextLine();
            System.out.print("Transliteration (optional, press Enter to skip): ");
            String transliteration = scanner.nextLine();
            System.out.print("Category: ");
            String category = scanner.nextLine();

            Phrase p = new Phrase(phrase, translation, transliteration, category);
            app.addPhrase(p);
            System.out.println("Phrase added: " + p);
        } catch (InvalidInputException | DuplicatePhraseException e) {
            System.out.println(e.getMessage());
        } catch (DatabaseException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // Prompts for a phrase ID and new details, then updates the existing entry.
    private static void correctPhrase() {
        try {
            int id = readInt("Phrase ID to correct: ");
            System.out.print("New Hindi phrase: ");
            String phrase = scanner.nextLine();
            System.out.print("New English translation: ");
            String translation = scanner.nextLine();
            System.out.print("New transliteration (optional): ");
            String transliteration = scanner.nextLine();
            System.out.print("New category: ");
            String category = scanner.nextLine();

            Phrase p = new Phrase(id, phrase, translation, transliteration, category, false);
            app.correctPhrase(p);
            System.out.println("Phrase corrected.");
        } catch (InvalidInputException | PhraseNotFoundException | DuplicatePhraseException e) {
            System.out.println(e.getMessage());
        } catch (DatabaseException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // Prompts for a phrase ID and toggles its verified flag.
    private static void verifyPhrase() {
        try {
            int id = readInt("Phrase ID: ");
            System.out.print("Set verified? (true/false): ");
            String input = scanner.nextLine().trim();
            boolean v = input.equalsIgnoreCase("true") || input.equalsIgnoreCase("yes");
            app.setVerified(id, v);
            System.out.println("Verification updated.");
        } catch (PhraseNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (DatabaseException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // Prompts for a file path and imports phrases from a CSV file.
    private static void importCsv() {
        try {
            System.out.print("CSV file path: ");
            String path = scanner.nextLine().trim();
            int added = app.importCsv(path);
            System.out.println("Import complete. Phrases added: " + added);
        } catch (IOException e) {
            System.out.println("File error: " + e.getMessage());
        } catch (InvalidInputException e) {
            System.out.println(e.getMessage());
        } catch (DatabaseException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // Prompts for a file path and exports all phrases to a CSV file.
    private static void exportCsv() {
        try {
            System.out.print("Export file path (e.g. data/phrases.csv): ");
            String path = scanner.nextLine().trim();
            app.exportCsv(path);
            System.out.println("Export complete: " + path);
        } catch (IOException e) {
            System.out.println("File error: " + e.getMessage());
        } catch (DatabaseException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // Reads an integer from the user, re-prompting on invalid input (no crash).
    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
}
