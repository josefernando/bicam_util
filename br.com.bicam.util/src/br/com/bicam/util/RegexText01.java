package br.com.bicam.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexText01 {
   public static void main( String args[] ) {
	   String REGEX = "\\bENTRY_(LIST|SET)\\b::";
	  Pattern p = Pattern.compile(REGEX);
	  String line = "ENTRY_SET::    ";
	  System.err.println(line.length());
	  line = line.replaceFirst(REGEX, "");
	  System.err.println(line.length());
      Matcher m = p.matcher(line);
      System.err.format("LINE >%s< - REGEX >%s<%n", line,REGEX);
   	  if((m.matches())) System.err.println(true);
   	  else System.err.println(false);
   }
}