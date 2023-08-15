package io.fastgogame;

import io.github.cdimascio.dotenv.Dotenv;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.*;

public class DatabaseService {
    private static final String URL = Dotenv.load().get("DATABASE");
    private static final HikariConfig HIKARI_CONFIG = new HikariConfig();
    private static final HikariDataSource HIKARI_DATA_SOURCE;
    private final MessageReceivedEvent event;

    public DatabaseService(MessageReceivedEvent event) {
        this.event = event;
    }

    static {
        HIKARI_CONFIG.setJdbcUrl(URL);
        HIKARI_DATA_SOURCE = new HikariDataSource(HIKARI_CONFIG);
    }

    private Connection getConnection() {
        try {
            return HIKARI_DATA_SOURCE.getConnection();
        } catch (SQLException e) {
            handleSQLException(e);
            return null;
        }
    }

    private void handleSQLException(SQLException e) {
        event.getChannel().sendMessage("An error " + e.getSQLState() + " occurred while connecting to the database!").queue();
    }

    public boolean hasGuild(String guildid) {
        Connection connection = getConnection();
        if (connection != null) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM userdata WHERE guildid = (?)")) {
                statement.setString(1, guildid);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            return false;
        }

    }

    private void executeQuery(String query, String... params) {
        Connection connection = getConnection();
        if (connection!= null) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                for (int i = 0; i < params.length; i++) {
                    statement.setString(i + 1, params[i]);
                }
                statement.executeUpdate();
            } catch (SQLException e) {
                handleSQLException(e);
            }
        }
    }

    public void addGuild(String guildid) {
        executeQuery("INSERT INTO userdata (guildid) VALUES (?)", guildid);
    }

    public void updateUserLogin(String guildid, String login) {
        executeQuery("UPDATE userdata SET login = (?) WHERE guildid = (?)", login, guildid);
    }

    public void updateUserPassword(String guildid, String password) {
        executeQuery("UPDATE userdata SET password = (?) WHERE guildid = (?)", password, guildid);
    }


    public String[] getLoginData (String guildid) {
        Connection connection = getConnection();
        if (connection == null) {
            return null;
        }
        try (PreparedStatement statement = connection.prepareStatement("SELECT login, password FROM userdata WHERE guildid = (?)")) {
            statement.setString(1, guildid);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String login = resultSet.getString("login");
                    String password = resultSet.getString("password");
                    if (!login.isEmpty() && !password.isEmpty()) {
                        return new String[]{login, password};
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            handleSQLException(e);
            return null;
        }
    }
}


