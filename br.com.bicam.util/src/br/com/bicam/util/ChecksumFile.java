package br.com.bicam.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


/*
 * Veja médoto estático em BicamSystem
 * 
 */

public class ChecksumFile {
//	public static String getFileHash(File file) throws IOException {
		public static String getFileHash(InputStream _is) throws IOException {
		
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
//			md.update(plainText.getBytes("UTF-8"));
//			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
//		InputStream is = new FileInputStream(file);
		InputStream is = _is;

		byte[] buf = new byte[1024];
		try {
		  is = new DigestInputStream(is, md);
		  // read stream to EOF as normal...
		  while(is.read(buf) > 0);
		}
		finally {
		  is.close();
		}
		byte[] digest = md.digest();
//		byte raw[] = md.digest();
//		return new String(digest); // binary format não é transmitido via http protocol
		return Base64.getEncoder().encodeToString(digest);
	}

	public static void main(String[] args) throws IOException {
    	File f  = new File("C:\\workspace\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\TIPMA00\\TIPMA1.VBP1");
    	File f1 = new File("C:\\workspace\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\TIPMA00\\TIPMA1.VBP1");
    	URL url = new URL("file:///C:/workspace/workspace_desenv_java8/visualbasic6/antlr4.vb6/input/TIPMA00/TIPMA0.VBP");
    	InputStream is = new FileInputStream(f);
		InputStream is1 = new FileInputStream(f1);
		InputStream urlStream = url.openStream();

    	String hash = getFileHash(is);
    	System.err.println(hash);
		String hash1 = getFileHash(is1);
    	System.err.println(hash1);
		String hashUrl = getFileHash(urlStream);
    	System.err.println(hashUrl);
    	
    	
		System.err.println(hash.equals(hash1));
	}
}
