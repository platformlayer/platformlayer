package org.platformlayer.ops.databases;

import java.util.List;
import java.util.Map;

import org.slf4j.*;

public class SqlResults {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(SqlResults.class);

	private final List<Map<String, Object>> rows;

	private final int updateCount;

	public SqlResults(List<Map<String, Object>> rows) {
		this.rows = rows;
		this.updateCount = -1;
	}

	public SqlResults(int updateCount) {
		this.rows = null;
		this.updateCount = updateCount;
	}
}
