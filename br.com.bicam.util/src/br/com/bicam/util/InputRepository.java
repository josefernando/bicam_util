package br.com.bicam.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class InputRepository {
	private static final String UTF_8 = "UTF-8";
	private static final String TYPE = "TYPE";
	private static final String URL = "URL";
	private static final String FILE = "FILE";
    private static final String CHARSET = "CHARSET";
    private static final String LOCATION = "LOCATION";   
    private static final String INPUTSTREAM = "INPUTSTREAM";
    
    private Scanner scanner;
    private PropertyList properties;
    InputStream is;
    
    public InputRepository(URL url) {
        if (url == null) throw new IllegalArgumentException("url argument is null");
        try {
            URLConnection site = url.openConnection();
            is     = site.getInputStream();
            scanner            = new Scanner(new BufferedInputStream(is), UTF_8);
            scanner.useDelimiter(System.lineSeparator());
        }
        catch (IOException ioe) {
            throw new IllegalArgumentException("Could not open " + url, ioe);
        }
        properties = new PropertyList();
        properties.addProperty(TYPE, URL);
        properties.addProperty(INPUTSTREAM, is);
        properties.addProperty(LOCATION, is.toString());
    }
    
    public InputRepository(File file) {
        if (file == null) throw new IllegalArgumentException("file argument is null");
        try {
            FileInputStream fis = new FileInputStream(file);
            scanner = new Scanner(new BufferedInputStream(fis), UTF_8);
            scanner.useDelimiter(System.lineSeparator());
            is = fis;
        }
        catch (IOException ioe) {
            throw new IllegalArgumentException("Could not open " + file, ioe);
        }
        properties = new PropertyList();
        properties.addProperty(TYPE, FILE);
        properties.addProperty(INPUTSTREAM, is);
        properties.addProperty(LOCATION, file.toString());
    }
    
    public InputRepository(InputStream _is) {
    	is = _is;
        scanner = new Scanner(new BufferedInputStream(_is), UTF_8);
        scanner.useDelimiter(System.lineSeparator());
    }
    
    public boolean exists()  {
        return scanner != null;
    }
    
    public boolean isEmpty() {
        return !scanner.hasNext();
    }
    
    public boolean hasNextLine() {
        return scanner.hasNextLine();
    }
    
    public String readLine() {
        String line;
        try {
            line = scanner.nextLine();
        }
        catch (NoSuchElementException e) {
            line = null;
        }
        return line;
    }
    
    public String getHash() {
        try {
			return BicamSystem.getInputStreamHash(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
        return null;
    }
    
    
    public PropertyList getProperties() {
    	return properties;
    } 
    
    public InputStream getInputStream() {
    	return is;
    }

    public static void main(String[] args) {
    	run();       
    }
    
    private static void run() {
    	InputRepository in;
        String urlName = "http://www.mocky.io/v2/5ac62d3b4a000034007e06a4";
//        String urlName = "file:///C:/workspace/workspace_desenv_java8/visualbasic6/antlr4.vb6/input/TIPMA00/TIPMA0.VBP";

        String fileName = "C:\\workspace\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\TIPMA00\\TIPMA0.VBP";
        
    	URL url = null;
		try {
			url = new URL(urlName);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}

        // read one line at a time from URL
        System.out.println("readLine() from URL " + urlName);
        System.out.println("---------------------------------------------------------------------------");

        in = new InputRepository(url);
        while (!in.isEmpty()) {
            String s = in.readLine();
            System.out.println(s);
        }
        System.out.println();
        
        // read one line at a time from File
        System.out.println("readLine() from FILE " + fileName);
        System.out.println("---------------------------------------------------------------------------");

        in = new InputRepository(new File(fileName));
        while (!in.isEmpty()) {
            String s = in.readLine();
            System.out.println(s);
        }
        System.out.println(); 
//        ============================================================================
    }
}