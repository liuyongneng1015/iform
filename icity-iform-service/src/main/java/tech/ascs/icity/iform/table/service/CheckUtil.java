package tech.ascs.icity.iform.table.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckUtil {
	
	public static boolean checkName(String name){
		if(name==null) return false;
	    String regEx = "[a-zA-Z]{1,}[a-zA-Z0-9_]{0,}";
	    Pattern pattern = Pattern.compile(regEx);
	    Matcher matcher = pattern.matcher(name);
	    return matcher.matches();
	}
}
