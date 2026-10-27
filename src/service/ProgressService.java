package service;

import dao.ResultDao;
import model.CategoryStat;
import model.QuizResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// Saves quiz results and computes per-category progress summaries.
public class ProgressService {

    private final ResultDao resultDao;

    // Initializes the service with a ResultDao for storage.
    public ProgressService(ResultDao dao) {
        this.resultDao = dao;
    }

    // Delegates saving a quiz result to the DAO.
    public void saveResult(QuizResult r) {
        resultDao.save(r);
    }

    // Groups all saved results by category and returns aggregated stats.
    public List<CategoryStat> getCategorySummary() {
        Map<String, int[]> map = new TreeMap<>();            // category -> {quizzes, correct, total}
        for (QuizResult r : resultDao.findAll()) {
            int[] a = map.computeIfAbsent(r.getCategory(), k -> new int[3]);
            a[0] += 1;
            a[1] += r.getCorrect();
            a[2] += r.getTotal();
        }
        List<CategoryStat> list = new ArrayList<>();
        for (Map.Entry<String, int[]> e : map.entrySet()) {
            list.add(new CategoryStat(e.getKey(), e.getValue()[0], e.getValue()[1], e.getValue()[2]));
        }
        return list;
    }

    // Returns the category with the lowest accuracy, ignoring "ALL" and zero-total rows.
    public CategoryStat getWeakestCategory() {
        CategoryStat weakest = null;
        for (CategoryStat s : getCategorySummary()) {
            if (s.getCategory().equalsIgnoreCase("ALL") || s.getTotal() == 0) {
                continue;
            }
            if (weakest == null || s.getAccuracy() < weakest.getAccuracy()) {
                weakest = s;
            }
        }
        return weakest;
    }
}
