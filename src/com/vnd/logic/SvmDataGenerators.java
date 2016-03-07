package com.vnd.logic;

import java.util.Arrays;

import com.vnd.model.Config;
import com.vnd.model.MainModel;
import com.vnd.model.MinMax;
import com.vnd.util.Util;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class SvmDataGenerators {
//	static int LINE_HEIGHT = 8;
	private static Size morphSize = new Size(3, 3);
	
	public static interface SvmTrainData{
		float[] getTrainData(Mat img);
		Mat getTempImage();
	}
	
	static int count = 0;
	static Mat erode(Mat img){
		Mat eroded = new Mat();
		Imgproc.erode(img, eroded, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, morphSize ));
		MainModel.saveImage(eroded, "eroded", ++count);
		return eroded;
	}
	
	public static SvmTrainData B8Data = new SvmTrainData() {
		@Override
		public float[] getTrainData(Mat img) {
			Mat sub = img.colRange(0, 24);
			sub = erode(sub);
			tempImage = sub;
//			return getLbpData(sub);
			Lbp2 lbp = new Lbp2(16, 4, sub);
			return lbp.getLbpHistgram();
		}
		
		private Mat tempImage;

		@Override
		public Mat getTempImage() {
			return tempImage;
		}
	};
	
	public static SvmTrainData Q0Data = new SvmTrainData() {

		@Override
		public float[] getTrainData(Mat img) {
//			Mat sub = img.rowRange(32, Model.FONT_HEIGHT);
			Mat sub = img.submat(32, 64, 8, 32);
			sub = erode(sub);
			tempImage = sub;
//			return getLbpData(sub);
			Lbp2 lbp = new Lbp2(8, 4, sub);
			return lbp.getLbpHistgram();
		}
		
		private Mat tempImage;

		@Override
		public Mat getTempImage() {
			return tempImage;
		}
	};
	
	public static SvmTrainData D0Data = new SvmTrainData() {

		@Override
		public float[] getTrainData(Mat img) {
			Mat sub1 = img.submat(0, 16, 0, 16);
			Mat sub2 = img.submat(48, 64, 0, 16);
			Mat sub = new Mat(32, 16, CvType.CV_8UC1);
			sub1.copyTo(sub.rowRange(0, 16));
			sub2.copyTo(sub.rowRange(16, 32));
			MainModel.saveImage(sub, "D 0 distinguish");
			tempImage = sub;
//			Mat sub = img.colRange(0, 24);
//			sub = erode(sub);
//			return getLbpData(sub);
			Lbp2 lbp = new Lbp2(8, 4, sub);
			return lbp.getLbpHistgram();
		}
		
		private Mat tempImage;

		@Override
		public Mat getTempImage() {
			return tempImage;
		}
	};

	public static SvmTrainData D0DataCount0 = new Count0Data();
	public static SvmTrainData B8DataCount0 = new B8Count0Data();
	
	static class B8Count0Data extends Count0Data{
		final static int LEN = 24;

		@Override
		public float[] getTrainData(Mat img) {
			float[] result = super.getTrainData(img);
			Mat mid = img.submat((MainModel.FONT_HEIGHT - LEN)/2, (MainModel.FONT_HEIGHT - LEN)/2 + LEN, 
					0, LEN);
			mid = Util.binary(mid, 0.45f, Imgproc.THRESH_BINARY);
//			tempImage = mid;
			int s = 0;
			for(; s<LEN; ++s){
				Mat col = mid.col(s);
				int nonZero = Core.countNonZero(col);
				if(nonZero > 2){
					break;
				}
			}
			Mat mid2 = mid.colRange(s, s + 4);
			tempImage = mid2;
			MainModel.saveImage(mid2, "B 8 middle image");
			int zeros = (int)mid2.total() - Core.countNonZero(mid2);
//			float[] r1 = new float[result.length + 1];
			float[] r1 = Arrays.copyOf(result, result.length + 1);
			System.out.println("B 8 middle zeros count----------------------: " + zeros);
			r1[result.length] = zeros;
			return r1;
		}
	}
	
	static Mat trimTop(Mat img, int maxAllowed){
		for(int r=0; r<img.rows(); ++r){
			Mat mr = img.row(r);
			int nonZero = Core.countNonZero(mr);
			if(nonZero > maxAllowed){
				return img.rowRange(r, img.rows());
			}
		}
		return img;
	}
	
	static Mat trimBottom(Mat img, int maxAllowed){
		for(int r=img.rows()-1; r>=0; --r){
			Mat mr = img.row(r);
			int nonZero = Core.countNonZero(mr);
			if(nonZero > maxAllowed){
				return img.rowRange(0, r+1);
			}
		}
		return img;
	}
	
	static class Count0Data implements SvmTrainData{
		
		final static int SIZE = 16;

		@Override
		public float[] getTrainData(Mat img) {
			Mat sub1 = img.submat(0, SIZE, 0, SIZE);
			sub1 = Util.binary(sub1, 0.5f, Imgproc.THRESH_BINARY);
			sub1 = trimTop(sub1, 2);
			
			Mat sub2 = img.submat(MainModel.FONT_HEIGHT - SIZE, MainModel.FONT_HEIGHT, 0, SIZE);
			sub2 = Util.binary(sub2, 0.5f, Imgproc.THRESH_BINARY);
			sub2 = trimBottom(sub2, 2);
			
			if(Config.isDebug){
				Mat sub = new Mat(sub1.rows() + sub2.rows(), SIZE, CvType.CV_8UC1);
				sub1.copyTo(sub.rowRange(0, sub1.rows()));
				sub2.copyTo(sub.rowRange(sub1.rows(), sub.rows()));
				MainModel.saveImage(sub, "D 0 distinguish");
				tempImage = sub;
			}else{
				tempImage = sub1;
			}
			
			int count00[] = new int[SIZE];
//			Arrays.fill(count00, 0);
			int count01[] = new int[SIZE];
			byte[] pixels = new byte[SIZE * sub1.rows()];
			sub1.get(0, 0, pixels);
			for(int c=0; c<SIZE; ++c){
//				sub1.get(0, c, pixels);
				for(int r=0; r<sub1.rows(); ++r){
					if(pixels[r * SIZE + c] == 0 || (r < sub1.rows() - 1 && pixels[(r+1) * SIZE + c] == 0)){
						++count00[c];
					}else{
						break;
					}
				}
			}
			
			pixels = new byte[SIZE * sub2.rows()];
			sub2.get(0, 0, pixels);
			for(int c=0; c<SIZE; ++c){
//				sub2.get(0, c, pixels);
				for(int r=sub2.rows() - 1; r>=0; --r){
					if(pixels[r*SIZE + c] == 0 || (r > 0 && pixels[(r-1) * SIZE + c] == 0)){
						++count01[c];
					}else{
						break;
					}
				}
			}
			int csize = 8;
//			float[] result = new float[csize + 1];
			float[] result0 = getCounts(count00, csize);
			float[] result1 = getCounts(count01, csize);
//			for(int i=0; i<result.length; ++i){
//				result[i] = result0[i] + result1[i];
//			}
			float count = result0[csize] + result1[csize];
			System.out.println("D 0 zero count-----------------: " + count);
			return new float[]{count};
		}
		
		protected float[] getCounts(int[] count00, int csize){
			int i=0;
			for(; i<SIZE - csize - 1; ++i){
				if(count00[i] < SIZE - 2){
					break;
				}
			}
			int end = csize + i;
			int s = i;
			
			float[] result = new float[csize + 1];
			result[csize] = 0;
			for(; i<end; ++i){
				result[i-s] = count00[i];
				result[csize] += count00[i];
			}
//			for(int i=0; i<SIZE; ++i){
//				result[i] = count00[i];
//			}
			return result;
		}
		
		protected Mat tempImage;

		@Override
		public Mat getTempImage() {
			return tempImage;
		}
	};

	public static SvmTrainData D0DataEdge = new SvmTrainData() {

		@Override
		public float[] getTrainData(Mat img) {
			Mat sub1 = img.submat(0, 12, 0, 16);
			Mat sub2 = img.submat(52, 64, 0, 16);
			Mat sub = new Mat(24, 16, CvType.CV_8UC1);
			sub1.copyTo(sub.rowRange(0, 12));
			sub2.copyTo(sub.rowRange(12, 24));
			MainModel.saveImage(sub, "D 0 distinguish");
			
			Mat hedge = EdgeRegion2.hdiff(sub);
			hedge = Util.binary(hedge, 0.8f, Imgproc.THRESH_BINARY);
			tempImage = hedge;
			int[] vproj = Projection.verticalProjection(hedge);
			

			Mat vedge = EdgeRegion2.vdiff(sub);
			vedge = Util.binary(vedge, 0.8f, Imgproc.THRESH_BINARY);
			int[] vproj2 = Projection.verticalProjection(vedge);
			
			float[] result = new float[vproj.length* 2 + 1];
			for(int i=0; i<vproj.length; ++i){
				result[i] = vproj[i];
			}
			result[vproj.length] = -1;
			int start = vproj.length + 1;
			for(int i=vproj.length + 1; i<result.length; ++i){
				result[i] = vproj2[i - start];
			}
			return result;
		}
		
		private Mat tempImage;

		@Override
		public Mat getTempImage() {
			return tempImage;
		}
	};
	
	public static SvmTrainData commonData = new SvmTrainData() {

		@Override
		public float[] getTrainData(Mat img) {
			img = erode(img);
			tempImage = img;
			return getLbpData(img);
//			Lbp2 lbp = new Lbp2(16, 8, img);
//			return lbp.getLbpHistgram();
		}
		
		private Mat tempImage;

		@Override
		public Mat getTempImage() {
			return tempImage;
		}
	};
	
	static int BLOCK_SIZE = 4;
	static float[] getLbpData(Mat img){
		int rows = img.rows() / BLOCK_SIZE;
		int cols = img.cols() / BLOCK_SIZE;
		Lbp2 lbp = new Lbp2(rows, cols, img);
		return lbp.getLbpHistgram();
	}
	
//	public static SvmTrainData B8Data = commonData;
	
//	static float[] merge(int[]... as){
//		int c = 0;
//		for(int[] a : as){
//			c += a.length;
//		}
//		float[] result = new float[c];
//		int i=0;
//		for(int[] a : as){
//			for(int v : a){
//				result[i++] = v;
//			}
//		}
//		return result;
//	}
	
	public static SvmTrainData S5Data = new SvmTrainData() {
		
		@Override
		public float[] getTrainData(Mat img) {
            Util.mark("get S5 data");
//			img = erode(erode(img));
			int[] hproj = Projection.horizontalProjection(img);
			hproj = Projection.filterProjection(hproj, 5, 10, 1);
			MinMax td = MinMax.getMinMaxIndices(hproj);
			
			Mat sub = img.rowRange(td.min, 40 + td.min);
			tempImage = sub;
			Lbp2 lbp = new Lbp2(8, 8, sub);
			return lbp.getLbpHistgram();
//			int[] proj = Projection.verticalProjection(sub);
//			LeftRight lr = CharSizeRefine4.getMinMaxIndices(proj);
//			
//			Mat left = sub.colRange(lr.left + 1, 9 + lr.left);
////			Mat top = sub.rowRange(0, LINE_HEIGHT);
////			Mat top = sub.submat(0, LINE_HEIGHT, LINE_HEIGHT, sub.cols() - 3);
//			int[] lp = Projection.verticalProjection(left);
////			int[] tp = Projection.horizontalProjection(top);
//			
//			return merge(lp);
		}
		
		private Mat tempImage;

		@Override
		public Mat getTempImage() {
			return tempImage;
		}
	};
	public static SvmTrainData Z2Data = S5Data;
}
