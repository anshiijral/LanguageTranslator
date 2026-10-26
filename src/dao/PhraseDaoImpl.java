package dao;

import exception.DatabaseException;
import model.Phrase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PhraseDaoImpl implements PhraseDao {

    @Override
    public void insert(Phrase p) {
        String sql = "INSERT INTO phrases " +
            "(phrase, translation, transliteration, category, verified) " +
            "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getPhrase());
            ps.setString(2, p.getTranslation());
            ps.setString(3, p.getTransliteration() == null ? "" : p.getTransliteration());
            ps.setString(4, p.getCategory());
            ps.setBoolean(5, p.isVerified());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    p.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not insert phrase", e);
        }
    }

    @Override
    public boolean update(Phrase p) {
        String sql = "UPDATE phrases SET phrase=?, translation=?, " +
            "transliteration=?, category=?, verified=? WHERE id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getPhrase());
            ps.setString(2, p.getTranslation());
            ps.setString(3, p.getTransliteration() == null ? "" : p.getTransliteration());
            ps.setString(4, p.getCategory());
            ps.setBoolean(5, p.isVerified());
            ps.setInt(6, p.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Could not update phrase", e);
        }
    }

    @Override
    public Phrase findById(int id) {
        String sql = "SELECT * FROM phrases WHERE id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not find phrase by id", e);
        }
    }

    @Override
    public List<Phrase> findAll() {
        String sql = "SELECT * FROM phrases ORDER BY category, id";
        List<Phrase> phrases = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                phrases.add(mapRow(rs));
            }
            return phrases;
        } catch (SQLException e) {
            throw new DatabaseException("Could not retrieve phrases", e);
        }
    }

    @Override
    public List<Phrase> findByCategory(String category) {
        String sql = "SELECT * FROM phrases " +
            "WHERE LOWER(category)=LOWER(?) ORDER BY id";
        List<Phrase> phrases = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, category);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    phrases.add(mapRow(rs));
                }
            }
            return phrases;
        } catch (SQLException e) {
            throw new DatabaseException("Could not find phrases by category", e);
        }
    }

    private Phrase mapRow(ResultSet rs) throws SQLException {
        String transliteration = rs.getString("transliteration");

        return new Phrase(
            rs.getInt("id"),
            rs.getString("phrase"),
            rs.getString("translation"),
            transliteration == null ? "" : transliteration,
            rs.getString("category"),
            rs.getBoolean("verified")
        );
    }
}
