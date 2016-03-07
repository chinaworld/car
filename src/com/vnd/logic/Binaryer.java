package com.vnd.logic;

import java.util.Arrays;

import org.opencv.core.Mat;

public class Binaryer {
	
	Mat grey;
//	int top, bottom;
	float lowT;
	float highT;
	int threshold;
	int avgWhite;
	
	public Binaryer(Mat grey){
		this(grey, 0.5f, 0.8f);
	}
	
	public Binaryer(Mat grey, float lowT, float highT){
		this.grey = grey;
		this.lowT = lowT;
		this.highT = highT;
	}
	
	private void calcThresholds(int top, int bottom){
		int[] pixels = new int[(bottom - top) * grey.cols()];
		byte[] pixel = new byte[1];
		int i=0;
		for(int r=top; r<bottom; ++r){
			for(int c=0; c<grey.cols(); ++c){
				grey.get(r, c, pixel);
				int v = pixel[0] & 0xff;
				pixels[i++] = v;
//				total += v;
			}
		}
		Arrays.sort(pixels);
		int idx = (int)(pixels.length * highT);
		long total = 0;
		for(i=idx; i<pixels.length; ++i){
			total += pixels[i];
		}
		avgWhite = (int)(total / (pixels.length - idx));
		
		int tidx = (int)(pixels.length * lowT);
		threshold = pixels[tidx];
//		return new int[]{threshold, avgWhite};
	}
	
	public void binary(int top, int bottom){
		calcThresholds(top, bottom);
//		threshold = thres[0];
//		avgWhite = thres[1];
		float alpha = (255 - threshold) / (float)(avgWhite - threshold);
		
//		Mat dest = new Mat(grey.rows(), grey.cols(), CvType.CV_8UC1);
		
		byte[] greyp = new byte[1];
		for(int r=top; r<bottom; ++r){
			for(int c=0; c<grey.cols(); ++c){
				grey.get(r, c, greyp);
				int v = greyp[0] & 0xff;
				if(v >= threshold){
					int tov = Math.round((v - threshold) * alpha + threshold);
					if(tov > 255){
						tov = 255;
					}
					greyp[0] = (byte)tov;
				}else{
					greyp[0] = 0;
				}
				grey.put(r, c, greyp);
			}
		}
//		return dest;
	}
}
