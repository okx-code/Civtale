package sh.okx.civtale.database;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private final Connection connection;

    public Database(Path folder) {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + folder.resolve("database.sqlite").toAbsolutePath());
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    public Connection getConnection() {
        return this.connection;
    }
}
