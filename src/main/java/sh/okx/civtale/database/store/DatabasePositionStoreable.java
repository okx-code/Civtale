package sh.okx.civtale.database.store;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface DatabasePositionStoreable<T extends PositionStoreable> {
    void migrate(Connection connection) throws SQLException;

    String replaceStatement(String table);
    T deserialize(ResultSet resultSet) throws SQLException;
    void serialize(T value, PreparedStatement statement) throws SQLException;
}
