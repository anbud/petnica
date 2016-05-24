package rs.zx.tictactoe;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import org.opencv.highgui.Highgui;

public class Main {
	private enum Type {
		X, O, N;
	}
	
	private static HashMap<Point, Type> map = new HashMap<Point, Type>();
	
	public static void main(String args[]) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

	    Mat img = Highgui.imread(args[0]);	    

	    if(!img.empty()) {
	        Mat bimg = preprocess(img);
	        
	        List<MatOfPoint> contourVector = new ArrayList<MatOfPoint>();
	        
	        Imgproc.findContours(bimg, contourVector, new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_TC89_L1);      
	        
	        for(int i = 0; i != contourVector.size(); i++){
		        recognize(new MatOfPoint2f(contourVector.get(i).toArray()));  
		    }
	        
	        try {
	        	File outF1 = new File(args[1]);
				if(outF1.getParentFile() != null)
					outF1.getParentFile().mkdirs();
				
				BufferedWriter bw = new BufferedWriter(new PrintWriter(outF1));
				
				for(int i = 0; i < 3; i++) {
		        	for(int j = 0; j < 3; j++) {
		        		Type t = map.get(new Point(j, i));
		        		
		        		bw.write((t != null && t != Type.N ? t : "-") + (j != 2 ? " " : ""));
		        	}
		        	bw.write("\n");
		        }
				
				bw.flush();
				bw.close();
			} catch (IOException e) {}
	    }
	}
	
	public static Mat preprocess(Mat img) {
	    Mat gimg = new Mat();
	    Imgproc.cvtColor(img, gimg, Imgproc.COLOR_BGR2GRAY);
	    
	    Imgproc.GaussianBlur(gimg, gimg, new Size(5, 5), 2, 2);

	    Mat bimg = gimg;
	    Imgproc.threshold(gimg, bimg, 0, 255, Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU);
	    Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(4, 4));
	    Imgproc.morphologyEx(bimg, bimg, Imgproc.MORPH_CROSS, kernel);

	    return bimg;
	}
	
	public static MatOfPoint2f getPoly(MatOfPoint2f contour) {
		MatOfPoint2f polyContour = new MatOfPoint2f();
	    double eps = Imgproc.arcLength(contour, true) * 0.02;

	    Imgproc.approxPolyDP(contour, polyContour, eps, true);

	    return polyContour;
	}
	
	public static void recognize(MatOfPoint2f contour) {
	    double contourArea = Imgproc.contourArea(contour);

	    MatOfPoint2f poly = getPoly(contour);

	    Type type = Type.N;
	    
	    //magic numbers, yay
	    if((poly.height() > 6 && poly.height() < 10) && contourArea > 900)
	        type = Type.O;
	    else if(poly.height() >= 9 && contourArea < 10000)
	        type = Type.X;
	    
	    Point b = Imgproc.boundingRect(new MatOfPoint(contour.toArray())).tl();
	    
	    Point p = new Point(Math.round(b.x / 100), Math.round(b.y / 100));
	   
	    Type tip = map.get(p);
	    if(tip == null || tip == Type.N) {
	    	map.put(p, type);
	    }
	}
}
