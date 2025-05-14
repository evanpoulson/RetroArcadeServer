package server.database;

import server.database.DatabaseConnector;
import java.sql.*;


public class ProfileDAO {
    private static final Connection conn = DatabaseConnector.connect();

    public static int registerPlayer(String username, String email, String hashedPassword) throws SQLException {

        // Prepare SQL query for storing profile into db. PreparedStatement is used to prevent SQL injection
        String query = "INSERT INTO profiles (username, email, hashed_Password) VALUES (?, ?, ?) RETURNING profile_id";

        // Open connection to database, set profile attributes and store into SQL table.
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, email);
            statement.setString(3, hashedPassword);

            ResultSet rs = statement.executeQuery();

            if(rs.next()) {
                int newPlayerID = rs.getInt("profile_id");
                //log("Player " + newPlayerID + " Registered Successfully!");
                return newPlayerID;
            } else {
                //log("Registration failed: No ID returned.");
                return -1;
            }

        } catch (SQLException e) {
            // Possibly do some logging
            throw new SQLException("Failed to register player: " + e.getMessage(), e);
        }
    }

    public static void main(String[] args) throws SQLException {
        registerPlayer("cristian", "cotal037@gmail.com", "FDSFD454FSD5F4");
    }
}
