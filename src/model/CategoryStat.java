package model;

// One row of the progress summary: aggregated quiz stats for a single category.
public class CategoryStat {

    private String category;
    private int quizzes;
    private int correct;
    private int total;

    // Creates a category statistic with the given aggregated values.
    public CategoryStat(String category, int quizzes, int correct, int total) {
        this.category = category;
        this.quizzes = quizzes;
        this.correct = correct;
        this.total = total;
    }

    // Gets the category name.
    public String getCategory() {
        return category;
    }

    // Gets the number of quizzes taken in this category.
    public int getQuizzes() {
        return quizzes;
    }

    // Gets the total number of correct answers across all quizzes.
    public int getCorrect() {
        return correct;
    }

    // Gets the total number of questions across all quizzes.
    public int getTotal() {
        return total;
    }

    // Computes accuracy as a percentage; returns 0 if total is 0.
    public double getAccuracy() {
        return total == 0 ? 0 : correct * 100.0 / total;
    }
}
