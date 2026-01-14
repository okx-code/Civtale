package sh.okx.civtale.reinforcement;

import sh.okx.civtale.database.Migrator;
import sh.okx.civtale.database.store.DatabasePositionStoreable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class ReinforcementStoreable implements DatabasePositionStoreable<Reinforcement> {
    @Override
    public void migrate(Connection connection) throws SQLException {
        Migrator migrator = new Migrator();

        migrator.registerMigration("reinforcements", 1, """
            CREATE TABLE reinforcements (world TEXT NOT NULL, x INT NOT NULL, y INT NOT NULL, z INT NOT NULL, type TEXT NOT NULL, health DOUBLE NOT NULL, created TIMESTAMP NOT NULL, UNIQUE (world, x, y, z))
            """, """
            CREATE INDEX reinforcement_chunk ON reinforcements (world, x / 16, z / 16)
            """);

        migrator.migrate(connection);
    }

    @Override
    public String replaceStatement(String table) {
        return """
            INSERT INTO %s (world, x, y, z, type, health, created)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(world, x, y, z) DO UPDATE SET
                world = excluded.world,
                x = excluded.x,
                y = excluded.y,
                z = excluded.z,
                type = excluded.type,
                health = excluded.health,
                created = excluded.created""".formatted(table);
    }

    @Override
    public Reinforcement deserialize(ResultSet resultSet) throws SQLException {
        String world = resultSet.getString("world");
        int x = resultSet.getInt("x");
        int y = resultSet.getInt("y");
        int z = resultSet.getInt("z");
        String type = resultSet.getString("type");
        double health = resultSet.getDouble("health");
        long created = resultSet.getTimestamp("created").getTime();

        return new Reinforcement(world, x, y, z, type, health, created);
    }

    @Override
    public void serialize(Reinforcement value, PreparedStatement statement) throws SQLException {
        statement.setString(1, value.world());
        statement.setInt(2, value.x());
        statement.setInt(3, value.y());
        statement.setInt(4, value.z());
        statement.setString(5, value.type());
        statement.setDouble(6, value.health());
        statement.setTimestamp(7, new Timestamp(value.created()));
    }
}
