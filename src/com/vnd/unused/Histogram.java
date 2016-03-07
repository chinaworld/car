package com.vnd.unused;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class Histogram {
    private MatOfInt channels;
    private Mat mask = new Mat();
    private Mat hist = new Mat();
    private MatOfInt histSize = new MatOfInt(256);
    private MatOfFloat ranges;
	private Mat greyImage;

    public Histogram(Mat greyImage) {
        ranges = new MatOfFloat(0.0f, 255.0f);
                                //0.0f, 255.0f,
                               // 0.0f, 255.0f); 
        channels = new MatOfInt(0); //, 1);
        this.greyImage = greyImage;
    }
    
    public static Mat draw(Mat greyImage){
    	Histogram instance = new Histogram(greyImage);
    	return instance.draw();
    }
    
    public Mat draw(){
    	List<Mat> images = new ArrayList<>();
        images.add(greyImage);
        Imgproc.calcHist(images, channels, mask, hist, histSize, ranges);
    	// Draw the histograms for B, G and R
    	  int hist_w = 256; 
    	  int hist_h = 300;
    	  int bin_w = Math.round( (float) hist_w/256 );
    	  
    	Mat histImage = new Mat(hist_h, hist_w, CvType.CV_8UC3, new Scalar( 0,0,0) );
    	Core.normalize(hist, hist, 0, histImage.rows(), Core.NORM_MINMAX);
    	/// Draw for each channel
    	for( int i = 1; i < 256; i++ )
    	{
    		
    	    Core.line( histImage, new Point( bin_w*(i-1), hist_h - Math.round(hist.get(i-1, 0)[0]) ) ,
    	                     new Point( bin_w*(i), hist_h - Math.round(hist.get(i, 0)[0]) ),
    	                     new Scalar( 255, 0, 0), 1, 8, 0  );
    	}
//    	Model.saveImage("histogram", histImage);
    	return histImage;
    }
}
