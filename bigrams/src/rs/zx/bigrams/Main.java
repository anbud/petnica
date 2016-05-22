package rs.zx.bigrams;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

public class Main {	
	private static double d = 0.5;
	private static HashMap<String, Integer> bigrams = new HashMap<String, Integer>();
	private static HashMap<String, Integer> count = new HashMap<String, Integer>();
	
	public static double prob(String b, String a) {
		Integer wi1 = bigrams.get(b+a);
		double max = Math.max((wi1 != null ? wi1 : 0)-d, 0);
		double pc = (wi1 != null ? wi1 : 0)/bigrams.size();
		double wi = count.get(b);
		double t = (d*count.size())/wi;
		
		return max/wi + t * pc;
	}
	public static void main(String args[]) {		
		try {
			BufferedReader in = new BufferedReader(new FileReader(args[0]));
			BufferedReader sin = new BufferedReader(new FileReader(args[1]));
			
			BufferedWriter out = new BufferedWriter(new PrintWriter(args[2]));
			BufferedWriter sout = new BufferedWriter(new PrintWriter(args[3]));
			
			String text = in.readLine();
			LinkedList<String> seq = new LinkedList<String>();
			String line;
			
			while((line = sin.readLine()) != null) 
				seq.push(line);
			
			
			for(int i = 0; i < text.length()-1; i++) {
				String ug = text.substring(i, i+1);
				if(count.containsKey(ug))
					count.put(ug, count.get(ug)+1);
				else
					count.put(ug, 1);
				
				String bg = text.substring(i, i+2);
				if(bigrams.containsKey(bg))
					bigrams.put(bg, bigrams.get(bg)+1);
				else
					bigrams.put(bg, 1);
			}
			
			for(Entry<String, Integer> e : bigrams.entrySet())
				out.write(e.getKey() + " " + e.getValue() + "\n");
			
			
			out.flush();
			out.close();
			
			for(String a : seq) {
				String nseq = "";
				
				String lc = a.substring(a.length()-1);
				double max = 0;
				
				for(String y0 : count.keySet()) {
					for(String y1 : count.keySet()) {
						for(String y2 : count.keySet()) {
							double val = prob(lc, y0)*prob(y0, y1)*prob(y1, y2);
							
							if(val > max) {
								max = val;
								nseq = y0+y1+y2;
							}
						}
					}
				}
				
				sout.write(a + nseq + "\n");
			}
			
			sout.flush();
			sout.close();
			
			in.close();
			sin.close();
			
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
