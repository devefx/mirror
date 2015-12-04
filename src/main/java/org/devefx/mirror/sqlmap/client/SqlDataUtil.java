package org.devefx.mirror.sqlmap.client;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.devefx.mirror.core.struct.Model;
import org.devefx.mirror.core.struct.Property;
import org.devefx.mirror.core.struct.impl.EntityProperty;
import org.devefx.mirror.core.struct.impl.PrimitiveProperty;
import org.devefx.mirror.sqlmap.engine.builder.xml.ConfigParser;
import org.devefx.mirror.utils.ReflectionUtils;

public class SqlDataUtil {
	
	private ConfigParser configParser;
	
	public SqlDataUtil(ConfigParser configParser) {
		this.configParser = configParser;
	}
	
	public<T> T extractData(ResultSet rs, Class<T> requiredType) throws SQLException {
		String type = SqlMapType.getType(requiredType);
		// base type
		if (type != null && rs.next()) {
			ResultSetMetaData rsmd = rs.getMetaData();
			if (rsmd.getColumnCount() != 1)
				throw new RuntimeException("结果集列数大于1.");
			return (T) getColumnValue(rs, 1, requiredType);
		// map type
		} else if (Map.class.isAssignableFrom(requiredType) && rs.next()) {
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			Map<String, Object> result = new HashMap<String, Object>(columnCount);
			for (int i = 1; i <= columnCount; i++) {
				result.put(rsmd.getColumnName(i), rs.getObject(i));
			}
			return (T) result;
		} else if (rs.next()) {
			ResultSetMetaData rsmd = rs.getMetaData();
			try {
				T object = requiredType.newInstance();
				// is data model
				Model model = configParser.getModel(requiredType);
				if (model != null) {
					Map<String, Map<String, Integer>> tableColumnMap = new HashMap<String, Map<String,Integer>>();
					for (int i = 1, n = rsmd.getColumnCount() + 1; i < n; i++) {
						String tableName = rsmd.getTableName(i);
						Map<String, Integer> columnMap = tableColumnMap.get(tableName);
						if (columnMap == null) {
							columnMap = new HashMap<String, Integer>();
							tableColumnMap.put(tableName, columnMap);
						}
						columnMap.put(rsmd.getColumnName(i), i);
					}
					List<String> closeList = new ArrayList<String>();
					extractData(object, model, rs, tableColumnMap, closeList);
					return object;
				}
				// not data model
				return object;
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		return null;
	}
	
	private void extractData(Object object, Model model, ResultSet rs, Map<String, Map<String, Integer>> tableColumnMap, List<String> closeList) {
		Map<String, Property> map = model.getColumnProperty();
		for (Map.Entry<String, Property> entry : map.entrySet()) {
			String column = entry.getKey();
			Property property = entry.getValue();
			if (property instanceof EntityProperty) {
				EntityProperty entityProperty = (EntityProperty) property;
				if (!entityProperty.isCollection()) {
					Model childModel = entityProperty.getModel();
					String addr = property.getClass() + "." + property.getName();
					if (childModel != null && !closeList.contains(addr)) {
						closeList.add(addr);
						try {
							Object childObject = childModel.getModelClass().newInstance();
							extractData(childObject, childModel, rs, tableColumnMap, closeList);
							ReflectionUtils.setValue(object, property.getName(), childObject);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			} else if (property instanceof PrimitiveProperty) {
				Map<String, Integer> columnMap = tableColumnMap.get(model.getTableName());
				if (columnMap == null && tableColumnMap.size() == 1) {
					for (Map<String, Integer> value : tableColumnMap.values())
						columnMap = value;
				}
				if (columnMap != null) {
					Integer columnIndex = columnMap.get(column);
					if (columnIndex != null) {
						Object value = getColumnValue(rs, columnIndex, property.getType());
						ReflectionUtils.setValue(object, property.getName(), value);
					}
				}
			}
		}
	}
	
	private<T> T getColumnValue(ResultSet rs, int index, Class<T> requiredType) {
		String type = SqlMapType.getType(requiredType);
		if (type != null) {
			Class<ResultSet> clazz = ResultSet.class;
			try {
				Method method = clazz.getMethod("get" + type, int.class);
				return (T) method.invoke(rs, index);
			} catch (Exception e) { }
		}
		return null;
	}
}
