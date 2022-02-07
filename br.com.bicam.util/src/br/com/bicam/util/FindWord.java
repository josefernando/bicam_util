package br.com.bicam.util;
/**
 * 
 * @author JFP
 * Parameters
 *  _s  = String de pesquisa
 *  pos = Posição da palavra a ser encontrada
 *        0 = 1a. palavra da string
 *        999 = última palavra da string
 *        1 = antes da palavra definida em arg
 *        2 = depois da plavra definida em arg
 *        n = indexa palavra na string
 * arg = palavra de referência para pos = 1 ou 2 
 * 
 */

public class FindWord {
	static String s;
	static int pos;
	static String arg;
	
	public static String find(String _s, int _pos, String _arg){
		s = _s;
		pos = _pos;
		arg = _arg;
		
		if (_s == null){
			System.err.println(" Erro de parametro");	
		}
		
		s.replace('\t', ' ');
		
		switch(_pos){
		case 0: /* recupera 1a. palavra*/
			String word = s.toString().split(" ")[0];
			return word;
		case 999: /* recupera última palavra*/
			int len = s.toString().split(" ").length;
			word = s.toString().split(" ")[len-1];
			return word;
		default:
			return findByPosition();
		}
	}
	
	private static String findByPosition(){
		if(arg == null){
			String word;
			try {
				word = s.toString().split(" ")[pos-1];
			} catch (StringIndexOutOfBoundsException e) {
				System.err.println("Não existe palavra nesta posição");
				return null;
			}
			return word;
		}
		
		int i;
		i = s.indexOf(arg);
		if (i > -1){
	        if (pos == 1) return findWordBefore(s);	
	        if (pos == 2) return findWordAfter(s);
		}
		return null;
	}
	
	private static String findWordBefore(String s){
		int i;
		s = s.substring(0,s.indexOf(arg));
        String s1 = s.trim();
		s1 = " " + s1;

		/* encontra palavra antes */
			for (i = s1.length(); i > 0; i--) {
				if (!s1.substring(i - 1, i).equals(" "));
				else
					break;
			}
			return s1.substring(i,s1.length());
	}
	
	private static String findWordAfter(String s){
		int i;
		s = s.substring(s.indexOf(arg)+arg.length());
        String s1 = s.trim();
		s1 = s1 + " ";

		/* encontra palavra depois */
			for (i = 0; i < s1.length(); i++) {
				if (s1.substring(i, i+1).equals(" "))
					break;
			}
			return s1.substring(0,i);
	}
}