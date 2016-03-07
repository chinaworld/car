package com.vnd.logic;

import com.vnd.model.MainModel;
import com.vnd.util.Util;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

public class ColorRegionFinder implements RegionFinder {
	Mat image = null;
//	SubModel model;
//	
//	public ColorRegionFinder(SubModel model){
//		this.model = model;
//	}
	
	public ColorRegionFinder(Mat image){
//		this.model = model;
		this.image = image;
	}

	@Override
	public Rect getRegion() {
//		if(image == null)
//			image = model.gaussImage;
		
		Mat blue = getBlue();
		MainModel.saveImage("Blue", blue);
		Projection proj = new Projection(blue);
		proj.clip();
		MainModel.saveImage("Blue Clipped by Projection", proj.image);
		return proj.rect;
	}
	
	public static int countBlue(Mat image){
		int cols = image.cols();
		int rows = image.rows();
		int count = 0;
		
		for (int r = 0; r < rows; ++r) {
			for (int c = 0; c < cols; ++c) {
				double[] pixels = image.get(r, c);
				if(isBlue(pixels)){
					++count;
				}
			}
		}
		return count;
	}
	
	static boolean isBlue(double[] pixels){ //BGR
		int b = (int)pixels[0];
		int g = (int)pixels[1];
		int r = (int)pixels[2];
		
//		return (b > g + r) || (b > g + 50 && b > r + 50);
		return b*2 - g - r > 50;
	}
	
	private Mat getBlue(){
		int cols = image.cols();
		int rows = image.rows();
		Mat result = Mat.zeros(rows, cols, CvType.CV_8U);
		
		for (int r = 0; r < rows; ++r) {
			for (int c = 0; c < cols; ++c) {
				double[] pixels = image.get(r, c);
				if(isBlue(pixels)){
					result.put(r, c, 255);
				}
			}
		}
		result = Util.enhaceContinues(Util.dilate(result), 9);
		
		return result;
	}

}
