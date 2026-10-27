package model;

import java.time.LocalDate;

// Stores the outcome of one completed quiz: date, category, score.
public class QuizResult {

    private int id;
    private LocalDate quizDate;
    private String category;
    private int correct;
    private int total;

    // Creates a new result with today's date and id 0 (not yet saved).
    public QuizResult(String category, int correct, int total) {
        this(0, LocalDate.now(), category, correct, total);
    }

    // Full constructor used when reading an existing row from the database.
    public QuizResult(int id, LocalDate quizDate, String category, int correct, int total) {
        this.id = id;
        this.quizDate = quizDate;
        this.category = category;
        this.correct = correct;
        this.total = total;
    }

    // Gets the unique database ID of this result.
    public int getId() {
        return id;
    }

    // Gets the date when this quiz was taken.
    public LocalDate getQuizDate() {
        return quizDate;
    }

    // Gets the category of the quiz (a category name, or "ALL" for mixed).
    public String getCategory() {
        return category;
    }

    // Gets the number of correct answers.
    public int getCorrect() {
        return correct;
    }

    // Gets the total number of questions in the quiz.
    public int getTotal() {
        return total;
    }

    // Computes the score as a percentage; returns 0 if total is 0.
    public double getPercentage() {
        return total == 0 ? 0 : correct * 100.0 / total;
    }
}
