package org.devefx.mirror.annotation.expr;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 * SqlExprParse 表达式解析器
 * @author： youqian.yue
 * @date： 2015-12-4 下午3:38:03
 */
public class SqlExprParse {
	/** velocity template */
	private static final String FOREACH = "#foreach($%s in $%s)#if($velocityCount > 1)%s#end %s#end";
	private static final String IFNOTNULL = "#if($%s)%s#end";
	private static final String IFNULL = "#if(!$%s)%s#end";
	/** constant */
	private static final String PARAM_PREFIX = "var_";
	private static final String SPACE = "\n";
	private static final VelocityEngine velocityEngine = new VelocityEngine();
	
	public String compiler(Sql sql) throws SqlExprException {
		if (sql != null) {
			StringBuffer dynamicSql = new StringBuffer();
			for (SqlExpr sqlExpr : sql.value()) {
				String expr = sqlExpr.expr();
				String join = sqlExpr.join();
				String foreach = sqlExpr.foreach();
				String item = sqlExpr.item();
				String ifnull = sqlExpr.ifnull();
				String ifnotnull = sqlExpr.ifnotnull();
				
				if (!foreach.isEmpty()) {
					if (!ifnull.isEmpty() || !ifnotnull.isEmpty())
						throw new SqlExprException("'foreach' conflict with 'ifnull' and 'ifnotnull'");
					if (dynamicSql.length() != 0)
						dynamicSql.append(SPACE);
					dynamicSql.append(String.format(FOREACH, item, foreach, join, expr));
				} else if (!ifnull.isEmpty()) {
					if (!ifnotnull.isEmpty())
						throw new SqlExprException("'ifnull' and 'ifnotnull' conflict");
					if (dynamicSql.length() != 0)
						dynamicSql.append(SPACE);
					dynamicSql.append(String.format(IFNULL, ifnull, expr));
				} else if (!ifnotnull.isEmpty()) {
					dynamicSql.append(SPACE);
					dynamicSql.append(String.format(IFNOTNULL, ifnotnull, expr));
				} else {
					if (dynamicSql.length() != 0)
						dynamicSql.append(SPACE);
					dynamicSql.append(expr);
				}
			}
			if (dynamicSql.length() != 0) {
				StringBuffer sb = new StringBuffer(dynamicSql.length());
				Pattern pattern = Pattern.compile("\\$\\{([a-zA-Z0-9_]+)\\}");
				Matcher matcher = pattern.matcher(dynamicSql);
				while(matcher.find()) {
					matcher.appendReplacement(sb, "\\$" + matcher.group(1));
				}
				matcher.appendTail(sb);
				return sb.toString();
				
			}
		}
		return null;
	}
	
	public String evaluate(String content, Object[] args, List<Object> parameter) {
		VelocityContext velocityContext = null;
		if (args != null) {
			if (args.length == 1 && args[0] instanceof Map) {
				velocityContext = new VelocityContext((Map<?, ?>) args[0]);
			} else {
				velocityContext = new VelocityContext();
				for (int i = 0, n = args.length; i < n; i++) {
					velocityContext.put(PARAM_PREFIX + (i + 1), args[i]);
				}
			}
		} else {
			velocityContext = new VelocityContext();
		}
		StringWriter writer = new StringWriter();
		velocityEngine.evaluate(velocityContext, writer, "", content);
		
		StringBuffer sb = new StringBuffer();
		Pattern pattern = Pattern.compile("#\\{([a-zA-Z0-9_]+)\\}");
		Matcher matcher = pattern.matcher(writer.toString());
		while (matcher.find()) {
			parameter.add(velocityContext.get(matcher.group(1)));
			matcher.appendReplacement(sb, "?");
		}
		matcher.appendTail(sb);
		return sb.toString();
	}
}
