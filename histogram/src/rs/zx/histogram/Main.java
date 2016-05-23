package rs.zx.histogram;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Optional;

import javax.imageio.ImageIO;

public class Main {
	private static File in;
	private static File out;
	
	private static HashMap<String, BufferedImage> images = new HashMap<String, BufferedImage>();
	private static HashMap<String, String> crops = new HashMap<String, String>();
	
	private static HashMap<Integer, Integer> histogram = new HashMap<Integer, Integer>();
	private static HashMap<Integer, Integer> mhistogram = new HashMap<Integer, Integer>();
	
	public static String getExt(File f) {
		int i = f.getName().lastIndexOf('.');
		if (i > 0) 
		    return f.getName().substring(i+1);
		
		return "";
	}
	
	public static String getName(File f) {
		int i = f.getName().lastIndexOf('.');
		if (i > 0) 
		    return f.getName().substring(0, i);
		
		return "";
	}
	
	public static void readFiles(File in) {
		for(File fileEntry : in.listFiles()) {
	        if (fileEntry.isDirectory()) {
	            readFiles(fileEntry);
	        } else {
	            try {
	            	if(getExt(fileEntry).equals("bmp")) {
	            		BufferedImage i = ImageIO.read(fileEntry);
	            		images.put(getName(fileEntry), i);
	            	} else {
	            		BufferedReader in1 = new BufferedReader(new FileReader(fileEntry));
	            		crops.put(getName(fileEntry), in1.readLine());
	            		in1.close();
	            	}
				} catch (IOException e) {
				}
	        }
	    }
	}
	
	public static void main(String args[]) {
		in = new File(args[0]);
		out = new File(args[1]);
		
		readFiles(in);
		
		for(int i = 0; i < 256; i++)
			histogram.put(i, 0);
		
		for(Entry<String, BufferedImage> e : images.entrySet()) {
			BufferedImage image = e.getValue();
			
			Integer crop[] = Arrays.stream(crops.get(e.getKey()).split(" ")).map(a -> Integer.parseInt(a)).toArray(Integer[]::new);
			
			for(int i = crop[1]; i < crop[1]+crop[3]; i++) {
				for(int j = crop[0]; j < crop[0]+crop[2]; j++) {
					int pixel = image.getRGB(i, j);
					
					int grayscale = (int)Math.round(0.2126 * ((pixel >> 16) & 0xFF) + 0.7152 * ((pixel >> 8) & 0xFF) + 0.0722 * (pixel & 0xFF));
					
					Integer prev = histogram.get(grayscale);
					
					histogram.put(grayscale, prev != null ? prev+1 : 1);
				}
			}
		}
		
		out.mkdir();
		
		try {
			BufferedWriter bw  = new BufferedWriter(new PrintWriter(new File(out.getAbsolutePath() + "\\GlobalHisto.csv")));
			BufferedWriter mbw  = new BufferedWriter(new PrintWriter(new File(out.getAbsolutePath() + "\\MatchingHisto.csv")));
			
			double factor = 10000.0/histogram.values().stream().reduce((i1,  i2) -> i1+i2).get();
			int sum = 0;
			
			for(int i = 0; i < 256; i++) {
				Integer a = histogram.get(i);
				
				bw.write((a != null ? a : 0) + (i != 255 ? "," : ""));
				
				Integer b = (a != null ? (int)Math.round(a*factor) : 0);
				sum+=b;
				
				mhistogram.put(i, b);
			}
			
			bw.flush();
			bw.close();
			
			//smoothing
			if(sum < 10000) {
				for(int i = 0; i < 10000-sum; i++) {
					int in = i%256;
					mhistogram.put(in, mhistogram.get(in)+1);
				}
			}
			if(sum > 10000) {
				for(int i = 0; i < sum-10000; i++) {
					int in = i%256;
					mhistogram.put(in, mhistogram.get(in)-1);
				}
			}	
			
			for(int i = 0; i < 256; i++) {
				mbw.write(mhistogram.get(i) + (i != 255 ? "," : ""));
			}
			
			mbw.flush();
			mbw.close();
			
			BufferedImage ne = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
			
			int all = 0;
			
			for(int i = 0; i < 256; i++) {
				Integer a = mhistogram.get(i);

				if(a > 0) {
					for(int j = all+1; j < a+all; j++) 
						ne.setRGB(j%100, j/100, (i << 16) + (i << 8) + i);
					
					all += a;
				}
			}
			
			ImageIO.write(ne, "BMP", new File(out.getAbsolutePath() + "\\MatchingImage.bmp"));
		} catch (IOException e1) {
		}
	}
}
