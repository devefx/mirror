package org.devefx.mirror.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test7 {
	public static void main(String[] args) {
		
		String s = "12#{a}";
		
		StringBuffer sb = new StringBuffer();
		Pattern pattern = Pattern.compile("#\\{([a-zA-Z0-9_]+)\\}");
		Matcher matcher = pattern.matcher(s);
		while(matcher.find()) {
			matcher.appendReplacement(sb, matcher.group(1));
		}
		matcher.appendTail(sb);
		
		System.out.println(sb);
		
	}
}
