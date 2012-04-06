package org.platformlayer.jdbc.simplejpa;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SimpleKeyMapper extends KeyMapper {

    final int columnNumber;

    public SimpleKeyMapper(int columnNumber) {
        this.columnNumber = columnNumber;
    }

    @Override
    public Object getKey(ResultSet rs) throws SQLException {
        return rs.getObject(columnNumber);
    }

}
