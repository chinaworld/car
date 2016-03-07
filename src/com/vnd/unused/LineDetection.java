package com.vnd.unused;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class LineDetection {
	private Mat image;
	private Mat colorImage = new Mat();
	
	public LineDetection(Mat image){
		this.image = image;
		Imgproc.cvtColor(image, colorImage, Imgproc.COLOR_GRAY2BGR);
	}
	
	public Mat drawLine(){
		Mat lines = new Mat();
		Imgproc.HoughLinesP(image, lines , 1, Math.PI/180, 50, 50, 5);
		
		for (int x = 0; x < lines.cols(); x++) 
	    {
	          double[] vec = lines.get(0, x);
	          double x1 = vec[0], 
	                 y1 = vec[1],
	                 x2 = vec[2],
	                 y2 = vec[3];
	          Point start = new Point(x1, y1);
	          Point end = new Point(x2, y2);

	          Core.line(colorImage, start, end, new Scalar(0,0,255), 3);

	    }
		return colorImage;
	}
}
