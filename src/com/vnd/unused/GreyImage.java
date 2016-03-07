package com.vnd.unused;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class GreyImage {
	Mat image;
	int cols;
	int rows;
	Mat temp = new Mat();
	
	public GreyImage(Mat image){
		this.image = image;
		cols = image.cols();
		rows = image.rows();
	}
	
	private void swapTemp(){
		Mat t = image;
		image = temp;
		temp = t;
	}
	
	public void getThreashold(){
		Imgproc.threshold(image, temp, getThreshold(), 255, Imgproc.THRESH_TOZERO);
		swapTemp();
	}
	
	private double getThreshold(){
		int channels = image.channels();
		byte[] colPixel = new byte[channels];
		
		double total = 0;
		for(int r=0; r<rows; ++r){
			for(int c=0; c<cols; c+=channels){
				image.get(r, c, colPixel);
				int pv = colPixel[0] & 0xff;
				total += pv;
			}
		}
		
		double m = total / (rows * cols);
		
		total = 0;
		
		for(int r=0; r<rows; ++r){
			for(int c=0; c<cols; c+=channels){
				image.get(r, c, colPixel);
				int pv = colPixel[0] & 0xff;
				double v = (pv - m);
				total += v * v;
			}
		}
		
		double mse = Math.sqrt(total) / (rows * cols - 1);
		return m + mse;
//		return 200;
	}
}
