package org.devefx.mirror.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.devefx.mirror.annotation.expr.Sql;
import org.devefx.mirror.annotation.expr.SqlExprParse;
import org.devefx.mirror.sqlmap.client.SqlMapClient;
import org.devefx.mirror.sqlmap.client.SqlMapExecutor;

public class MapperProxy implements InvocationHandler {
	private static final Map<Method, String> SQL_MAP = new HashMap<Method, String>();
	
	private SqlExprParse sqlExprParse;
	private SqlMapClient sqlMapClient;
	public MapperProxy(SqlMapClient sqlMapClient) {
		this.sqlExprParse = new SqlExprParse();
		this.sqlMapClient = sqlMapClient;
	}
	
	public static<T> T newMapperProxy(Class<T> mapperInterface, SqlMapClient sqlMapClient) {
		ClassLoader classLoader = mapperInterface.getClassLoader();
		Class<?>[] interfaces = new Class[] {mapperInterface};
		MapperProxy proxy = new MapperProxy(sqlMapClient);
		return (T) Proxy.newProxyInstance(classLoader, interfaces, proxy);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		if (method.getDeclaringClass() == SqlMapExecutor.class) {
			return method.invoke(sqlMapClient, args);
		}
		String content = SQL_MAP.get(method);
		if (content == null && method.isAnnotationPresent(Sql.class)) {
			content = sqlExprParse.compiler(method.getAnnotation(Sql.class));
			SQL_MAP.put(method, content);
		}
		if (content != null) {
			Class<?> returnType = method.getReturnType();
			List<Object> parameters = new ArrayList<Object>();
			String sql = sqlExprParse.evaluate(content, args, parameters);
			/** List */
			if (List.class.isAssignableFrom(returnType)) {
				Class<?> returnClass = Map.class;
				Type type = method.getGenericReturnType();
				if (type instanceof ParameterizedType) {
					ParameterizedType pt = (ParameterizedType) type;
					Type[] typeArguments = pt.getActualTypeArguments();
					if (typeArguments.length != 0) {
						type = typeArguments[0];
						if (type instanceof Class) {
							returnClass = (Class<?>) type;
						} else if (type instanceof ParameterizedType) {
							returnClass = (Class<?>) ((ParameterizedType) type).getRawType();
						}
					}
				}
				return sqlMapClient.queryList(sql, returnClass, parameters.toArray());
			} else if (returnType == int.class || returnType == Integer.class) {
				return sqlMapClient.execute(sql, parameters.toArray());
			} else if (returnType == boolean.class || returnType == Boolean.class) {
				return sqlMapClient.execute(sql, parameters.toArray()) > 0;
			} else if (returnType == int[].class || returnType == Integer[].class) {
				return sqlMapClient.executeBatch(sql, parameters.toArray());
			} else if (returnType == boolean.class || returnType == Boolean.class) {
				for (int i : sqlMapClient.executeBatch(sql, parameters.toArray())) {
					if (i == 0) return false;
				}
				return true;
			}
			return sqlMapClient.query(sql, returnType, parameters.toArray());
		}
		return null;
	}
}
