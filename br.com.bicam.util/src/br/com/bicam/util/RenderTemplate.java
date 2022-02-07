package br.com.bicam.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class RenderTemplate {
	public static void toFile(File file , String toRender) throws FileNotFoundException{
		FileOutputStream fos = new FileOutputStream(file);
		PrintStream ps = new PrintStream(fos);
		System.setOut(ps);
		System.out.println(toRender);
	}
}