package io.fastgogame;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.*;

public class DatabaseConnector {
    private static final String url = Dotenv.load().get("DATABASE");

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }

    public static boolean isGuildExists(String guildid) throws SQLException {
        try (Connection connection = getConnection()) {
            String query = "SELECT * FROM userdata WHERE guildid = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, guildid);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next();
                }
            }
        }
    }

    public static void addGuild(String guildid) throws SQLException {
        try (Connection connection = getConnection()) {
            String query = "INSERT INTO userdata (guildid) VALUES (?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, guildid);
                statement.executeUpdate();
            }
        }
    }

    public static void updateUserLogin(String guildid, String login) throws SQLException{
        try (Connection connection = getConnection()) {
            String query = "UPDATE userdata SET login = (?) WHERE guildid = (?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, login);
                statement.setString(2, guildid);
                statement.executeUpdate();
            }
        }
    }

    public static void updateUserPassword(String guildid, String password) throws SQLException {
        try (Connection connection = getConnection()) {
            String query = "UPDATE userdata SET password = (?) WHERE guildid = (?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, password);
                statement.setString(2, guildid);
                statement.executeUpdate();
            }
        }
    }

    public static String[] getLoginData (String guildid) throws SQLException {
        try (Connection connection = getConnection()) {
            String query = "SELECT login, password FROM userdata WHERE guildid = (?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, guildid);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String login = resultSet.getString("login");
                        String password = resultSet.getString("password");
                        if (login != null && !login.isEmpty() && password != null && !password.isEmpty()) {
                            return new String[]{login, password};
                        } else {
                            return null;
                        }
                    } else {
                        return null;
                    }
                }
            }
        }
    }
}
