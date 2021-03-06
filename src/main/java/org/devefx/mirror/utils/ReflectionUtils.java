package org.devefx.mirror.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

import org.devefx.mirror.annotation.Column;
import org.devefx.mirror.sqlmap.client.SqlMapType;

public class ReflectionUtils {
	private static final String SET_METHOD_PREFIX = "set";
	private static final String GET_METHOD_PREFIX = "get";
	
	public static Object setValue(Object object, String name, Object ...values) {
		if (!Pattern.matches(SET_METHOD_PREFIX + "[A-Z].*", name)) {
			name = SET_METHOD_PREFIX + StringUtils.firstToUpperCase(name);
		}
		Object returnValue = call(object, name, values);
		if (returnValue != null)
			return returnValue;
		Class<?> clazz = object.getClass();
		name = StringUtils.firstToLowerCase(name.substring(3));
		if (values.length == 1) {
			try {
				Field field = clazz.getDeclaredField(name);
				field.setAccessible(true);
				try {
					Object value = convert(values[0], field.getType());
					field.set(object, value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (NoSuchFieldException e) { }
		}
		return null;
	}
	
	public static<T> T getValue(Object object, String name) {
		Class<?> clazz = object.getClass();
		if (!Pattern.matches(GET_METHOD_PREFIX + "[A-Z].*", name)) {
			name = GET_METHOD_PREFIX + StringUtils.firstToUpperCase(name);
		}
		Method method = getMethod(clazz, name);
		if (method != null) {
			method.setAccessible(true);
			try {
				Object value = method.invoke(object);
				return value != null ? (T) value : null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		name = StringUtils.firstToLowerCase(name.substring(3));
		try {
			Field field = clazz.getDeclaredField(name);
			field.setAccessible(true);
			try {
				Object value = field.get(object);
				return value != null ? (T) value : null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (NoSuchFieldException e) { }
		return null;
	}
	
	public static Object call(Object object, String methodName, Object ...values) {
		Class<?> clazz = object.getClass();
		Class<?>[] classes = new Class<?>[values.length];
		for (int i = 0; i < classes.length; i++) {
			classes[i] = values[i] != null ? values[i].getClass() : null;
		}
		Method method = getMethod(clazz, methodName, classes);
		if (method != null) {
			try {
				classes = method.getParameterTypes();
				for (int i = 0; i < values.length; i++) {
					values[i] = convert(values[i], classes[i]);
				}
				method.setAccessible(true);
				return method.invoke(object, values);
			} catch (Exception e) { }
		}
		return null;
	}
	
	public static String findField(Class<?> clazz, String sqlColunm) {
		for (Field field : clazz.getDeclaredFields()) {
			if (field.isAnnotationPresent(Column.class)) {
				Column column = field.getAnnotation(Column.class);
				if (column.value().equals(sqlColunm)
						|| field.getName().equals(sqlColunm))
					return field.getName();
			}
		}
		return null;
	}
	
	public static Object convert(Object value, Class<?> type) {
		String name = SqlMapType.getType(type);
		if (value != null && name != null) {
			String s = value.toString();
			if (name.equals("Boolean"))
				return Boolean.parseBoolean(s);
			if (name.equals("Byte"))
				return Byte.parseByte(s);
			if (name.equals("Short"))
				return Short.parseShort(s);
			if (name.equals("Int"))
				return Integer.parseInt(s);
			if (name.equals("Long"))
				return Long.parseLong(s);
			if (name.equals("Float"))
				return Float.parseFloat(s);
			if (name.equals("Double"))
				return Double.parseDouble(s);
			if (name.equals("BigDecimal"))
				return new BigDecimal(s);
			if (name.equals("String"))
				return s;
			if (name.equals("URL"))
				try {
					return new URL(s);
				} catch (MalformedURLException e) { }
		}
		return value;
	}
	
	private static Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
		try {
			return clazz.getMethod(name, parameterTypes);
		} catch (NoSuchMethodException e) {
			for (Method method : clazz.getMethods()) {
				if (name.equals(method.getName()) && 
						method.getParameterTypes().length == parameterTypes.length) 
					return method;
			}
		}
		return null;
	}
	
}
