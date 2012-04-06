package org.platformlayer.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AtomHelpers {

    public static int getKey(Connection connection, String tableName, String value) throws SQLException {
        // TODO: Caching (at least micro-caching)??
        while (true) {
            {
                String sql = "SELECT id FROM " + tableName + " WHERE key=?";

                PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = null;
                try {
                    ps.setString(1, value);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        return rs.getInt(1);
                    }
                } finally {
                    JdbcUtils.safeClose(rs);
                    JdbcUtils.safeClose(ps);
                }
            }

            if (tableName.equals("projects")) {
                // TODO: Yuk
                throw new UnsupportedOperationException();
            }

            {
                String sql = "INSERT INTO " + tableName + " (key) VALUES (?)";
                PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = null;
                try {
                    ps.setString(1, value);
                    ps.executeUpdate();
                } finally {
                    JdbcUtils.safeClose(rs);
                    JdbcUtils.safeClose(ps);
                }
            }
        }
    }

    public static String mapCodeToKey(Connection connection, String tableName, int id) throws SQLException {
        // TODO: Caching (at least micro-caching)??
        String sql = "SELECT key FROM " + tableName + " WHERE id=?";

        PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = null;
        try {
            ps.setInt(1, id);
            rs = ps.executeQuery();
            while (rs.next()) {
                return rs.getString(1);
            }
        } finally {
            JdbcUtils.safeClose(rs);
            JdbcUtils.safeClose(ps);
        }

        return null;
    }

}
