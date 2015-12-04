package org.devefx.mirror.sqlmap.client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.devefx.mirror.cache.DatabaseCacheCentral;
import org.devefx.mirror.core.struct.Model;
import org.devefx.mirror.core.struct.Property;
import org.devefx.mirror.core.struct.impl.PrimitiveProperty;
import org.devefx.mirror.sqlmap.engine.builder.xml.ConfigParser;
import org.devefx.mirror.utils.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SqlMapClient
 * @author： youqian.yue
 * @date： 2015-12-4 下午4:48:03
 */
public class SqlMapClient implements SqlMapExecutor {
	private static final String SQL_DELETE_BY_KEY = "DELETE FROM %s WHERE %s = ?";
	private static final Logger LOGGER = LoggerFactory.getLogger(SqlMapClient.class);
	private ConfigParser configParser;
	private DatabaseCacheCentral cacheCentral;
	private SqlDataUtil dataUtil;
	private DataSource dataSource;
	
	public SqlMapClient(ConfigParser configParser) {
		this.configParser = configParser;
		this.dataUtil = new SqlDataUtil(configParser);
		this.dataSource = this.configParser.getBean("dataSource");
		this.cacheCentral = new DatabaseCacheCentral(null, this);
	}
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	public DataSource getDataSource() {
		return dataSource;
	}
	@Override
	public <T> T query(Class<T> type, Object key) throws SQLException {
		Model model = configParser.getModel(type);
		if (model != null) {
			String querySql = model.getQuerySql();
			T result = query(querySql, type, key);
			if (result != null) {
				cacheCentral.set(model.getToken(key), result);
			}
			return result;
		}
		throw new SQLException("");
	}
	@Override
	public boolean update(Object object) throws SQLException {
		if (object != null) {
			Class<?> modelClass = object.getClass();
			Model model = configParser.getModel(modelClass);
			if (model != null) {
				String primaryKey = model.getPrimaryKey();
				Object keyValue = ReflectionUtils.getValue(object, primaryKey);
				String tokenName = model.getToken(keyValue);
				Object memory = cacheCentral.get(tokenName, modelClass);
				
				List<Object> parameters = new ArrayList<Object>();
				StringBuffer sql = new StringBuffer("UPDATE ");
				sql.append(model.getTableName());
				sql.append(" SET ");
				boolean isChange = false;
				Map<String, Property> map = model.getColumnProperty();
				for (Map.Entry<String, Property> entry : map.entrySet()) {
					Property property = entry.getValue();
					if (property instanceof PrimitiveProperty) {
						String column = entry.getKey();
						if (!column.equals(primaryKey)) {
							Object value = ReflectionUtils.getValue(object, property.getName());
							// compare cache
							if (memory != null) {
								Object memoryValue = ReflectionUtils.getValue(memory, property.getName());
								if ((value == null && memoryValue == null) || (value != null && value.equals(memoryValue))
										|| (memoryValue != null && memoryValue.equals(value))) {
									continue;
								}
							}
							parameters.add(value);
							if (isChange)
								sql.append(", ");
							sql.append(column);
							sql.append(" = ?");
							isChange = true;
						}
					}
				}
				if (isChange) {
					parameters.add(keyValue);
					sql.append(" WHERE ");
					sql.append(primaryKey);
					sql.append(" = ?");
					boolean result = execute(sql.toString(), parameters) > 0;
					if (result) {
						cacheCentral.set(tokenName, object);
					}
					return result;
				}
			} else {
				throw new SQLException("");
			}
		}
		return false;
	}
	@Override
	public boolean delete(Object object) throws SQLException {
		if (object != null) {
			Class<?> modelClass = object.getClass();
			Model model = configParser.getModel(modelClass);
			if (model != null) {
				Object key = ReflectionUtils.getValue(object, model.getPrimaryKey());
				if (key == null)
					throw new SQLException("模型主键不能为空");
				boolean result = execute(String.format(SQL_DELETE_BY_KEY, model.getTableName(),
						model.getPrimaryKey()), key) > 0;
				if (result) {
					cacheCentral.delete(model.getToken(key));
				}
				return result;
			}
			throw new SQLException("");
		}
		return false;
	}
	@Override
	public boolean insert(Object object) throws SQLException {
		if (object != null) {
			Class<?> modelClass = object.getClass();
			Model model = configParser.getModel(modelClass);
			if (model != null) {
				List<Object> parameters = new ArrayList<Object>();
				StringBuffer sql = new StringBuffer("INSERT INTO ");
				sql.append(model.getTableName());
				StringBuffer fieldSql = new StringBuffer();
				StringBuffer valueSql = new StringBuffer();
				boolean isFirst = true;
				Map<String, Property> map = model.getColumnProperty();
				for (Map.Entry<String, Property> entry : map.entrySet()) {
					Property property = entry.getValue();
					if (property instanceof PrimitiveProperty) {
						String column = entry.getKey();
						if (!column.equals(model.getPrimaryKey())) {
							Object value = ReflectionUtils.getValue(object, property.getName());
							if (!isFirst) {
								fieldSql.append(", ");
								valueSql.append(", ");
							}
							fieldSql.append(column);
							valueSql.append("?");
							parameters.add(value);
							isFirst = false;
						}
					}
				}
				sql.append("(");
				sql.append(fieldSql);
				sql.append(") VALUES(");
				sql.append(valueSql);
				sql.append(");select @@identity");
				Integer id = query(sql.toString(), Integer.class, parameters);
				if (id != null) {
					ReflectionUtils.setValue(object, model.getPrimaryKey(), id);
					cacheCentral.set(model.getToken(id), object);
					return true;
				}
			}
		}
		return false;
	}
	@Override
	public <T> T query(String sql, Class<T> type, Object... parameters)
			throws SQLException {
		Connection conn = null;
		ResultSet rs = null;
		try {
			conn = dataSource.getConnection();
			PreparedStatement statement = conn.prepareStatement(sql);
			for (int i = 0, n = parameters.length; i < n; i++) {
				statement.setObject(i + 1, parameters[i]);
			}
			rs = statement.executeQuery();
			T data = dataUtil.extractData(rs, type);
			if (data != null) {
				// TODO set cache
			}
			return data;
		} finally {
			closeConnection(conn, rs);
		}
	}
	@Override
	public <T> List<T> queryList(String sql, Class<T> type,
			Object... parameters) throws SQLException {
		Connection conn = null;
		ResultSet rs = null;
		try {
			conn = dataSource.getConnection();
			PreparedStatement statement = conn.prepareStatement(sql);
			for (int i = 0, n = parameters.length; i < n; i++) {
				statement.setObject(i + 1, parameters[i]);
			}
			rs = statement.executeQuery();
			List<T> list = new ArrayList<T>(rs.getRow());			
			T data = null;
			do {
				data = dataUtil.extractData(rs, type);
				if (data != null) {
					// TODO set cache
					list.add(data);
				}
			} while (data != null);
			return list;
		} finally {
			closeConnection(conn, rs);
		}
	}
	@Override
	public int execute(String sql, Object... parameters) throws SQLException {
		Connection conn = null;
		ResultSet rs = null;
		try {
			conn = dataSource.getConnection();
			PreparedStatement statement = conn.prepareStatement(sql);
			for (int i = 0; i < parameters.length; i++) {
				statement.setObject(i + 1, parameters[i]);
			}
			int result = statement.executeUpdate();
			if (result != 0) {
				// TODO update cache
			}
			return result;
		} finally {
			closeConnection(conn, rs);
		}
	}
	@Override
	public int[] executeBatch(String sql, Object[]... parameters)
			throws SQLException {
		Connection conn = null;
		ResultSet rs = null;
		try {
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			PreparedStatement statement = conn.prepareStatement(sql);
			for (Object[] objects : parameters) {
				for (int i = 0; i < objects.length; i++) {
					statement.setObject(i + 1, objects[i]);
				}
				statement.addBatch();
			}
			int[] result = statement.executeBatch();
			conn.commit();
			conn.setAutoCommit(true);
			// TODO update cache
			return result;
		} catch (SQLException e) {
			if (!conn.isClosed()) {
				conn.rollback();
			}
			e.printStackTrace();
		} finally {
			closeConnection(conn, rs);
		}
		return null;
	}
	
	private void closeConnection(Connection conn, ResultSet rs) {
		try {
			if (conn != null)
				conn.close();
			if (rs != null)
				rs.close();
		} catch (SQLException e) {
			LOGGER.error("close connection fail");
		}
	}
}
