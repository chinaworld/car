package com.vnd.ui;

import com.vnd.logic.FontBinaryer3;
import org.opencv.core.Mat;

public class FontSerial {
//	public static final int COUNT = 20;
	public Mat image;
	public static final int fromDiff = -10;
	public static final int toDiff = 20;
	
	public FontSerial(Mat image){
		this.image = image;
	}
	
	Mat[] binary(){
		Mat[] result = new Mat[toDiff - fromDiff + 1];
		for(int i=0; i<result.length; ++i){
			result[i] = binary(i + fromDiff);
		}
		return result;
	}
	
	Mat binary(int diff){
		FontBinaryer3 binarier = new FontBinaryer3(image, diff);
		return binarier.binary();
	}
}
