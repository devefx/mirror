package org.devefx.mirror.annotation.expr;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SqlExpr {
	/** general */
	String expr() default "";
	String join() default "";
	/** foreach */
	String foreach() default "";
	String item() default "";
	/** if */
	String ifnull() default "";
	String ifnotnull() default "";
}
