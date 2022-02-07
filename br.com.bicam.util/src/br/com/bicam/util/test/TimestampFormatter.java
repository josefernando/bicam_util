package br.com.bicam.util.test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
public class TimestampFormatter {
	   public static void main (String [] args) {
//	       long currentDateTime = System.currentTimeMillis();
	       Date currentDate = new Date(System.currentTimeMillis());

	       DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
	       System.out.println(dateFormat.format(currentDate));
	   }
}
