package dao;

import exception.DatabaseException;
import model.QuizResult;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// JDBC implementation of ResultDao that reads and writes the quiz_results table.
public class ResultDaoImpl implements ResultDao {

    // Inserts one quiz result row into the database.
    @Override
    public void save(QuizResult r) {
        String sql = "INSERT INTO quiz_results (quiz_date, category, correct, total) " +
            "VALUES (?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(r.getQuizDate()));
            ps.setString(2, r.getCategory());
            ps.setInt(3, r.getCorrect());
            ps.setInt(4, r.getTotal());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not save quiz result", e);
        }
    }

    // Returns all quiz results ordered by date then id (oldest first).
    @Override
    public List<QuizResult> findAll() {
        String sql = "SELECT * FROM quiz_results ORDER BY quiz_date, id";
        List<QuizResult> results = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                results.add(new QuizResult(
                    rs.getInt("id"),
                    rs.getDate("quiz_date").toLocalDate(),
                    rs.getString("category"),
                    rs.getInt("correct"),
                    rs.getInt("total")
                ));
            }
            return results;
        } catch (SQLException e) {
            throw new DatabaseException("Could not retrieve quiz results", e);
        }
    }
}
