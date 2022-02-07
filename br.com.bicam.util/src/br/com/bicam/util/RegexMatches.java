package br.com.bicam.util;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexMatches {

   private static final String REGEX = "\\bcat\\b";
   private static final String INPUT = "cat cat cat cattie cat";
   
   private static final String REGEX1 = "^Attribute VB_Name = \\\"R1FAB001\\\"$";
   private static final String REGEX2 = "^Attribute VB_Name = \\\"([\\d\\w]+)\\\"$";
   private static final String REGEX3 = "^Option\\s+Explicit$";
   private static final String INPUT1 = "Attribute VB_Name = \"R1FAB001\"";
//   private static final String REGEX4 = "^Name=\"([\\d\\w]+)\"$";

 

   private static boolean isOptionExpicit(String _file) {
	   String REGEX22 = "^Option\\s+Explicit$";
	  //_file => "C:\\workspace\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\R1PAB0\\R1FAB001.FRM"
	  Pattern p = Pattern.compile(REGEX22);
      try
      {   BufferedReader in = new BufferedReader(new FileReader(new File(_file)));
          for (String line = in.readLine(); line != null; line = in.readLine())
          {	 
              Matcher m = p.matcher(line);
        	  if((m.matches())) return true;
          }
      } catch (IOException e)
      { System.out.println("File I/O error!");
      }
      return false;
  } 
   
   private static Map<String,Integer> arrayInVbForm(String _file) {
/* want search for "Index" then record "lb_Label" 
 * Begin VB.Label lb_Label 
       Appearance      =   0  'Flat
       AutoSize        =   -1  'True
       Caption         =   "Produto"
       ForeColor       =   &H00800000&
       Height          =   195
       Index           =   4
       Left            =   105
       TabIndex        =   93
       Top             =   0
       Width           =   675
    End	   
*/	   Set<String> controls = new LinkedHashSet<String>();
       Map<String,Integer>  occurs = new HashMap<String,Integer>();
       String control = null;
	   String REGEXBegin = "^\\s*Begin\\s+\\w+\\.\\w+\\s+(\\w+)\\s*";
	   String REGEXIndex = "^\\s*Index\\s+=\\s+(\\d+)\\s*";
	   Integer lineNum=0;

	  //_file => "C:\\workspace\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\R1PAB0\\R1FAB001.FRM"
	  Pattern pBegin = Pattern.compile(REGEXBegin);
	  Pattern pIndex = Pattern.compile(REGEXIndex);

      try
      {   BufferedReader in = new BufferedReader(new FileReader(new File(_file)));
          for (String line = in.readLine(); line != null; line = in.readLine())
          {	 
        	  lineNum++;
              Matcher mBegin = pBegin.matcher(line);
              Matcher mIndex = pIndex.matcher(line);
        	  if((mBegin.matches())) {
        		  control = mBegin.group(1);
        	  }
        	  if((mIndex.matches())) {
    			  Integer index = occurs.get(control);
        		  if(index != null) {
        			  if(Integer.parseInt(mIndex.group(1)) + 1 > index ) {
        				  occurs.put(control, Integer.parseInt(mIndex.group(1)) + 1);
        			  }
        		  }
        		  else {
        			  occurs.put(control, Integer.parseInt(mIndex.group(1)) + 1);
        		  }
        		  control = null;
        	  }
          }
      } catch (IOException e)
      { System.out.println("File I/O error!");
      }
      return occurs;
  }     
   
  private static String getVbName(String _file) {
	   String REGEX22 = "^Attribute\\s+VB_Name\\s+=\\s+\\\"([\\d\\w]+)\\\"$";
	  //_file => "C:\\workspace\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\R1PAB0\\R1FAB001.FRM"
	  if(_file == null) return null;
	  String vbName = _file.split("\\\\")[_file.split("\\\\").length-1];
	  vbName = vbName.split("\\.")[0];
	  Pattern p = Pattern.compile(REGEX2);
      try
      {   BufferedReader in = new BufferedReader(new FileReader(new File(_file)));
          for (String line = in.readLine(); line != null; line = in.readLine())
          {	 
              Matcher m = p.matcher(line);
        	  if((m.matches())) vbName = m.group(1);
          }
      } catch (IOException e)
      { System.out.println("File I/O error!");
      }
      return vbName;
  }
  
 private static String getExecProc(String _file) {
//	   String REGEX22 = "^Exec\\s+VB_Name\\s+=\\s+\\\"([\\d\\w]+)\\\"$";
	   String REGEX23 = "^EXEC[^.]+[.]+([\\d\\w_]+).+";
	   String REGEX24 = "^EXEC[^.]+[.]+([\\d\\w_]+).+"; // do not use [.]+, it doesn't work!!

	  //_file => "C:\\workspace\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\R1PAB0\\R1FAB001.FRM"
	  if(_file == null) return null;
	  Pattern p = Pattern.compile(REGEX23);
	  Pattern p1 = Pattern.compile(REGEX24);

	  String proc = null;
     try
     {   BufferedReader in = new BufferedReader(new FileReader(new File(_file)));
         for (String line = in.readLine(); line != null; line = in.readLine())
         {	 
             Matcher m = p.matcher(line);
              if((m.matches())) proc = m.group(1);
         }
         in.close();
     } catch (IOException e)
     { System.out.println("File I/O error!");
     }
     
     return proc;
 }  
   
   public static void main( String args[] ) {
	   String fileName = "C:\\workspace\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\R1PAB0\\R1FAB013.FRM";
	   String fileNameMatch = "C:\\workspace\\workspace_desenv_java8\\sybase\\antlr4.transactSql\\input\\PR_CONTROLE_PROC_G01719.QRY";

//	   String fileName = "C:\\workspace\\workspace_desenv_java8\\visualbasic6\\antlr4.vb6\\input\\R1PAB0TESTE\\GEMOSY01.BAS";


	   System.err.println("==> VBName " + getVbName(fileName)); 
	   System.err.println("==> Explicity " + isOptionExpicit(fileName)); 
	   System.err.println("==> Array " + arrayInVbForm(fileName)); 
	   
	   System.err.println("==> proc " + getExecProc(fileNameMatch)); 



	   
      Pattern p = Pattern.compile(REGEX2);
      Matcher m = p.matcher(INPUT1);   // get a matcher object
      int count = 0;

//		System.out.println(m.lookingAt());
//		System.out.println(m.matches());
//	    m.reset();
      
//      System.out.println(m.groupCount());
//	  System.out.println("group: " + m.group(0));
      if((m.matches()))
    	  System.err.println("group: " + m.group(1));
      else System.err.println("NO MATCHES!");
//      System.out.println("group: " + m.group(2));
      
      m.reset();
      
/*      while(m.find()) {
         count++;
         System.out.println("Match number "+count);
         System.out.println("start(): "+m.start());
         System.out.println("end(): "+m.end());
      }*/
   }
}