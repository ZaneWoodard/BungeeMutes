package com.beastsmc.bungeemutes;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.HashMap;

public class Storage {

    private final static String MUTE_TABLE = "bungeemutes";
    private final HikariDataSource ds;
    private final BungeeMutes plugin;

    private HashMap<String, Mute> cache;

    public Storage(BungeeMutes plugin) {
        this.plugin = plugin;
        cache = new HashMap<>();

        HikariConfig config = new HikariConfig();
        String jdbcURL = String.format("jdbc:mysql://%s:%d/%s",
                                       this.plugin.getConfig().getString("mysql.host"),
                                       this.plugin.getConfig().getInt("mysql.port"),
                                       this.plugin.getConfig().getString("mysql.database"));
        config.setJdbcUrl(jdbcURL);
        config.setUsername(this.plugin.getConfig().getString("mysql.username"));
        config.setPassword(this.plugin.getConfig().getString("mysql.password"));
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "50");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "1024");
        config.addDataSourceProperty("connectionTimeout", "7500");
        ds = new HikariDataSource(config);

        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        try(Connection conn = ds.getConnection();
            Statement stmt = conn.createStatement()
        ){
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS `" + MUTE_TABLE + "` (\n"
                    + "  `muted` VARCHAR(36) NOT NULL,\n"
                    + "  `muter` VARCHAR(36) NOT NULL,\n"
                    + "  `expiration` TIMESTAMP,\n"
                    + "  `reason` TEXT,\n"
                    + "  PRIMARY KEY (`muted`)\n"
                    + ");"
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void storeMute(Mute record) {
        if(this.plugin.debug) this.plugin.getLogger().info("Storing mute: " + record.toString());
        cache.put(record.getMutedUUID(), record);
        try (Connection conn = ds.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement("INSERT INTO " + MUTE_TABLE + " VALUES (?, ?, ?, ?) "
                                           + "ON DUPLICATE KEY UPDATE "
                                           + "muter=VALUES(muter), "
                                           + "expiration=VALUES(expiration), "
                                           + "reason=VALUES(reason);")
        ) {
            ps.setString(1, record.getMutedUUID());
            ps.setString(2, record.getMuterName());
            Timestamp expiration = (record.isPermanent()) ? null : new Timestamp(record.getExpiration().getTime());
            ps.setTimestamp(3, expiration);
            ps.setString(4, record.getReason());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves mute. Does not consult the cache, sometimes cached data is known to be bad.
     */
    public Mute fetchMute(String uuid) {
        if(this.plugin.debug) this.plugin.getLogger().info("Fetching mute for: " + uuid);
        Mute mute = null;

        try(Connection conn = ds.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM " + MUTE_TABLE + " WHERE muted=? LIMIT 1;")
        ) {

            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            if (rs.first()) {
                mute = new Mute(rs.getString("muted"),
                                rs.getString("muter"),
                                rs.getTimestamp("expiration"),
                                rs.getString("reason"));
                cache.put(uuid, mute);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(this.plugin.debug) this.plugin.getLogger().info("Found mute: " + mute);

        return mute;
    }

    public Mute retrieveMuteFromCache(String uuid) {
        return cache.get(uuid);
    }
    
    public void deleteMute(Mute record) {
        if(this.plugin.debug) this.plugin.getLogger().info("Deleting mute: " + record.toString());
        if(cache.values().contains(record)) {
            //In cache, remove from cache
            cache.remove(record.getMutedUUID());
        }

        try(Connection conn = ds.getConnection();
            PreparedStatement ps = conn.prepareStatement("DELETE FROM " + MUTE_TABLE + " WHERE muted=? AND muter=? AND expiration=?")
        ){

            ps.setString(1, record.getMutedUUID());
            ps.setString(2, record.getMuterName());
            ps.setTimestamp(3, new Timestamp(record.getExpiration().getTime()));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeFromCache(String uuid) {
        cache.remove(uuid);
    }
}
