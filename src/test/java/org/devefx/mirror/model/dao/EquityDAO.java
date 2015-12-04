package org.devefx.mirror.model.dao;

import java.util.List;
import java.util.Map;

import org.devefx.mirror.annotation.expr.Sql;
import org.devefx.mirror.annotation.expr.SqlExpr;
import org.devefx.mirror.model.Equity;
import org.devefx.mirror.sqlmap.client.SqlMapExecutor;

public interface EquityDAO extends SqlMapExecutor {
	
	@Sql({
		@SqlExpr(expr="select t06_equity.*, b.username, b.nickname, b.avatar_image from t06_equity, t06_member_info b"),
		@SqlExpr(expr="where t06_equity.sort > 0 and t06_equity.member_id = b.id"),
		@SqlExpr(expr="and t06_equity.audit_status in(2,3,4) order by t06_equity.sort")
	})
	List<Equity> getEquityByRecommend();
	
	@Sql({
		@SqlExpr(expr="select a.id, a.is_recommend,a.sort,a.name, a.description, a.main_picture, a.audit_status, a.limit_amount, a.collect_amount, a.audit_time, a.limit_day,"),
		@SqlExpr(expr="a.member_id, b.username, b.nickname, b.avatar_image from t06_equity a, t06_member_info b where a.member_id = b.id and a.audit_status not in (0,1,7,8)"),
		@SqlExpr(ifnotnull="search", expr="and a.name like '%#{search}%'"),
		@SqlExpr(foreach="industry", item="var", join=" or ", expr="instr(industry_id,'[#{var}]') > 0"),
		@SqlExpr(ifnotnull="stage", expr="and a.stage in(#{stage})"),
		@SqlExpr(ifnotnull="status", expr="and a.audit_status in(#{status})"),
		@SqlExpr(ifnotnull="address", expr="and province != '' and instr(#{address}, province)"),
		@SqlExpr(expr="order by"),
		@SqlExpr(ifnotnull="time", expr="audit_time #{time},"),
		@SqlExpr(ifnotnull="percent", expr="(collect_amount / limit_amount) ${percent},"),
		@SqlExpr(ifnotnull="amount", expr="limit_amount #{amount},"),
		@SqlExpr(expr="id desc limit #{startRow}, #{pageSize}")
	})
	List<Equity> getEquityByMapForPage(Map<String, Object> parameter);
}
