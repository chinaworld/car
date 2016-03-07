package com.vnd.ui;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class FontSerialAdaptive extends FontSerial {
	public static final int fromDiff = -10;
	public static final int toDiff = 5;
	int fromBlockSize = 9;
	int toBlockSize = 15;
	
	public FontSerialAdaptive(Mat image){
		super(image);
	}
	
	Mat[] binary(){
//		Mat[] result = new Mat[toDiff - fromDiff + 1];
		List<Mat> result = new ArrayList<>();
		for(int b=fromBlockSize; b<=toBlockSize; b+=2){
		for(int i=fromDiff; i<=toDiff; i+=2){
			result.add(binary(i, b)); 
		}
		}
		return result.toArray(new Mat[0]);
	}
	
	Mat binary(int diff, int blockSize){
		Mat dst = new Mat();
//		int blockSize = image.cols() / 3; 
//		if(blockSize % 2 == 0){
//			++blockSize;
//		}
		Imgproc.adaptiveThreshold(image, dst, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, blockSize, diff);
		return dst;
	}
}
