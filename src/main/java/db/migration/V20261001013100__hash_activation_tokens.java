package db.migration;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

/**
 * Tokens are now stored as SHA-256 (hex) instead of the raw value
 * ({@code ActivationToken.hash}). Re-hashes the rows created before this change so
 * links already emailed keep working.
 *
 * <p>Java migration (not SQL) because hashing functions differ between PostgreSQL and
 * the H2 used by the tests. Self-contained on purpose: migrations must not depend on
 * application classes that may change later.</p>
 */
public class V20261001013100__hash_activation_tokens extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Map<Long, String> rows = new LinkedHashMap<>();
        try (Statement select = context.getConnection().createStatement();
             ResultSet rs = select.executeQuery("SELECT id, token FROM rh_activation_token")) {
            while (rs.next()) {
                rows.put(rs.getLong(1), rs.getString(2));
            }
        }
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        try (PreparedStatement update = context.getConnection()
                .prepareStatement("UPDATE rh_activation_token SET token = ? WHERE id = ?")) {
            for (Map.Entry<Long, String> row : rows.entrySet()) {
                byte[] digest = sha256.digest(row.getValue().getBytes(StandardCharsets.UTF_8));
                update.setString(1, HexFormat.of().formatHex(digest));
                update.setLong(2, row.getKey());
                update.addBatch();
            }
            update.executeBatch();
        }
    }
}
