package org.devefx.mirror.test;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.devefx.mirror.model.Equity;
import org.devefx.mirror.model.Member;
import org.devefx.mirror.model.dao.EquityDAO;
import org.devefx.mirror.proxy.MapperProxy;
import org.devefx.mirror.sqlmap.client.SqlMapClient;
import org.devefx.mirror.sqlmap.engine.builder.xml.ConfigParser;

import com.alibaba.fastjson.JSON;

public class Test5 {
	public static void main(String[] args) throws SQLException, SecurityException, NoSuchMethodException {
		// init mirror
		ClassLoader loader = Test4.class.getClassLoader();
		InputStream is = loader.getResourceAsStream("SqlConfiguration.xml");
		
		ConfigParser configParser = new ConfigParser();
		final SqlMapClient sqlMapClient = configParser.parse(is);
		
	 	EquityDAO equityDAO = MapperProxy.newMapperProxy(EquityDAO.class, sqlMapClient);
	 	
	 	
	 	long t0 = System.currentTimeMillis();
	 	
	 	Member member = equityDAO.query(Member.class, 1);
	 	
	 	
	 	/*member = equityDAO.query("select * from t06_member_info a where id = ?", Member.class, 1);
	 	
	 	System.out.println(member);*/
	 	
	 	
	 	//equityDAO.getEquityByMapForPage(null);
	 	
	 	List<Equity> list = equityDAO.getEquityByRecommend();
	 	System.out.println(list);
	 	
	 	Map<String, Object> parameter = new HashMap<String, Object>();
	 	parameter.put("startRow", 0);
	 	parameter.put("pageSize", 10);
	 	List<Equity> equities = equityDAO.getEquityByMapForPage(parameter);
	 	
	 	
	 	System.out.println(JSON.toJSONString(member));
	 	
	 	/*for (int i = 0; i < 100; i++) {
	 		equityDAO.findEquityLikeName("test", "VALID", new String[] {"id", "name"});
		}*/
		long t1 = System.currentTimeMillis();
		
		System.out.println(t1-t0);
	}
	public List<String> getStringList(){
	    return null;
	}
}
