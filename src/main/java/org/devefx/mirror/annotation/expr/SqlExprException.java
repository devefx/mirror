package org.devefx.mirror.annotation.expr;

/**
 * Sql表达式异常
 * @author： youqian.yue
 * @date： 2015-12-3 下午2:40:50
 */
public class SqlExprException extends Exception {
	
	private static final long serialVersionUID = 1006859194442496719L;
	
	public SqlExprException(String messag) {
		super(messag);
	}
	@Override
	public String getMessage() {
		return "this sqlExpr is error, cause：" + super.getMessage();
	}
}
