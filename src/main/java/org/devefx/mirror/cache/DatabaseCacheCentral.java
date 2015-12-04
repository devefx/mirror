package org.devefx.mirror.cache;

import org.devefx.mirror.sqlmap.client.SqlMapClient;

public class DatabaseCacheCentral {
	
	private DatabaseCache databaseCache;
	private SqlMapClient sqlMapClient;
	
	public DatabaseCacheCentral(DatabaseCache databaseCache, SqlMapClient sqlMapClient) {
		this.databaseCache = databaseCache;
		this.sqlMapClient = sqlMapClient;
	}
	
	public void set(String key, Object value) {
		
	}
	
	public<T> T get(String key, Class<?> requireType) {
		return null;
	}
	
	public void delete(String key) {
		
	}
	
}
