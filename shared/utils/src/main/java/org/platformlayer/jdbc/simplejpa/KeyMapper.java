package org.platformlayer.jdbc.simplejpa;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class KeyMapper {
    public abstract Object getKey(ResultSet rs) throws SQLException;
}
