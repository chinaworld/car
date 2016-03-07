package com.vnd.util;

import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Util {
	public static Mat threshold(Mat image, int threshold){
		Mat thresholdImage = new Mat();

		Imgproc.threshold(image, thresholdImage, threshold, 255, Imgproc.THRESH_TOZERO);
		return thresholdImage;
	}

	public static Mat dilate(Mat source) {
		Mat destination = new Mat(source.rows(), source.cols(), source.type());

		int dilation_size = 2;
		Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,
				new Size(2 * dilation_size + 1, 2 * dilation_size + 1));
		Imgproc.dilate(source, destination, element1);
		return destination;
	}

	public interface PixelHandler{
		void handle(int pixel);
	}

	public static void handleEachPixel(Mat image, PixelHandler handler){
		int cols = image.cols();
		int rows = image.rows();
		byte[] pixels = new byte[1];
		
		for (int r = 0; r < rows; ++r) {
			for (int c = 0; c < cols; ++c) {
				image.get(r, c, pixels);
				int pv = pixels[0] & 0xff;
				handler.handle(pv);
			}
		}
	}
	
//	static Mat adaptiveBinary(Mat src){
//		return adaptiveBinary(src, -5);
//	}
//	
//	static Mat adaptiveBinaryChinese(Mat src){
//		return adaptiveBinary(src, 10);
//	}
//	
//	static Mat adaptiveBinaryAscii(Mat src){
//		return adaptiveBinary(src, -5);
//	}
//
//	static Mat adaptiveBinary(Mat src, int diff){
//		return adaptiveBinary(src, diff, Imgproc.THRESH_BINARY);
//	}

	public static void mark(String message){
		//System.out.println(message);
	}

	public static void mark(Object... messages){
		StringBuilder sb = new StringBuilder();
		for(Object m : messages){
			sb.append(m);
		}
		mark(sb.toString());
	}

	public static Mat adaptiveBinary(Mat src, int diff, int thresholdType){
		if(src.rows() < 6){
			return src.clone();
		}
		Mat dst = new Mat();
		int blockSize = src.rows() / 3;
		if(blockSize % 2 == 0){
			++blockSize;
		}
		Imgproc.adaptiveThreshold(src, dst, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, thresholdType, blockSize, diff);
		return dst;
	}
	
	public static int getThreashold(Mat grey, float percent){

        MatOfInt histSize = new MatOfInt(256);
        MatOfFloat ranges = new MatOfFloat(0.0f, 256.0f);

        Mat mask = new Mat();
        List<Mat> images = new ArrayList<>();
        images.add(grey);
        MatOfInt channels = new MatOfInt(0);
        Mat hist = new Mat();
		Imgproc.calcHist(images, channels, mask, hist, histSize, ranges);

        int index = (int)(grey.total() * percent);
        float[] histArray = new float[256];
        hist.get(0, 0, histArray);
        int count = 0;
        for(int i=0; i<256; ++i){
            count += histArray[i];
            if(count >= index){
                return i;
            }
        }
        return 255;

//		int[] pixels = getValues(grey);
//		Arrays.sort(pixels);
//		int idx = (int)(pixels.length * percent);
//		return pixels[idx];
	}

	public static int getThreashold(Mat grey, float percent, int thresholdType){
		if(thresholdType == Imgproc.THRESH_BINARY_INV || thresholdType == Imgproc.THRESH_TOZERO_INV){
			percent = 1 - percent;
		}
		return getThreashold(grey, percent);
	}

    public static Rect getLargest(List<Rect> rects){
        Rect result = null;
        if(rects.size() == 0){
            return result;
        }else if(rects.size() == 1){
            return rects.get(0);
        }
        int max = 0;
        for(Rect r : rects){
            int area = r.width * r.height;
            if(area > max){
                max = area;
                result = r;
            }
        }
        return result;
    }

	public static Mat enhaceContinues(Mat image, int maxD) {
		int channels = image.channels();
		int cols = image.cols();
		int rows = image.rows();
		byte[] colPixel = new byte[channels];
		colPixel[0] = (byte)255;

		Mat result = image.clone();
		Neighbor neighbor = new Neighbor(image, maxD, 127);

		for (int c = 0; c < cols; ++c) {
			for (int r = 0; r < rows; ++r) {
				if(neighbor.hasNeighbor(r, c)){
					result.put(r, c, colPixel);
				}
			}
		}
		return result;
	}
	
	public static Mat stepImage(Mat image){
		Mat result = image.clone();

		int cols = image.cols();
		int rows = image.rows();
		int step = 85;
		byte[] pixels = new byte[1];
		
		for (int r = 0; r < rows; ++r) {
			for (int c = 0; c < cols; ++c) {
				image.get(r, c, pixels);
				int pv = pixels[0] & 0xff;
				result.put(r, c, pv/step * step);
			}
		}
		return result;
	}
	
//	public static Mat clip(Mat image, Rect rect){
//		image.
//	}

	public static Mat erode(Mat source) {
		Mat destination = new Mat(source.rows(), source.cols(), source.type());

		int erosion_size = 1;
		Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,
				new Size(2 * erosion_size + 1, 2 * erosion_size + 1));
		Imgproc.erode(source, destination, element);
		return destination;
	}
	
	public static Mat binary(Mat source, int threshold, int thresholdType){
		Mat binary = new Mat();

		Imgproc.threshold(source, binary, threshold, 255, thresholdType);
		
		return binary;
	}
	
	public static Mat binary(Mat source, float percent, int thresholdType){
		if(thresholdType == Imgproc.THRESH_BINARY_INV || thresholdType == Imgproc.THRESH_TOZERO_INV){
			percent = 1 - percent;
		}
		int t = getThreashold(source, percent);
		return binary(source, t, thresholdType);
	}
	
	public static void binary(Mat source, Mat to, float percent, int thresholdType){
		if(thresholdType == Imgproc.THRESH_BINARY_INV || thresholdType == Imgproc.THRESH_TOZERO_INV){
			percent = 1 - percent;
		}
		int t = getThreashold(source, percent);
		Imgproc.threshold(source, to, t, 255, thresholdType);
	}

	static byte[] pixels = new byte[1];
	
//	public static int getValue(Mat image, int row, int col){
//		image.get(row, col, pixels);
//		return pixels[0] & 0xff;
//	}
	
	public static int[] getValues(Mat grey){
		int[] pixels = new int[(int)grey.total()];
        byte[] bytes = new byte[pixels.length];
        grey.get(0, 0, bytes);
        for(int i=0; i<bytes.length; ++i){
            pixels[i] = bytes[i] & 0xff;
        }

//		byte[] pixel = new byte[1];
//		int i=0;
//		for(int r=0; r<grey.rows(); ++r){
//			for(int c=0; c<grey.cols(); ++c){
//				grey.get(r, c, pixel);
//				int v = pixel[0] & 0xff;
//				pixels[i++] = v;
////				total += v;
//			}
//		}
		return pixels;
	}
	
	static int max(int[] arrays){
		int max = 0;
		for(int e : arrays){
			if(max < e){
				max = e;
			}
		}
		return max;
	}
	
	static int min(int[] arrays){
		int min = Integer.MAX_VALUE;
		for(int e : arrays){
			if(min > e){
				min = e;
			}
		}
		return min;
	}
	
	static double getThreshold2(Mat image) {
		int channels = image.channels();
		int cols = image.cols();
		int rows = image.rows();
		byte[] colPixel = new byte[channels];
		
		int[] colors = new int[256];
		Arrays.fill(colors, 0);

		for (int r = 0; r < rows; ++r) {
			for (int c = 0; c < cols; ++c) {
				image.get(r, c, colPixel);
				int pv = colPixel[0] & 0xff;
				++colors[pv];
			}
		}

		double total = 0;
		int count = 0;
		for(int c : colors){
			if(c > 0){
				++count;
				total += c;
			}
		}

		return total / count;
	}
	
	static Mat rotate(Mat src, double angle) {
	    Point pt = new Point(src.cols() / 2f, src.rows() / 2f);
//	    Point2f pt(src.cols/2., src.rows/2.);    
	    Mat r = Imgproc.getRotationMatrix2D(pt, angle, 1.0);
	    Mat dst = new Mat();
	    Imgproc.warpAffine(src, dst, r, new Size(src.cols(), src.rows()));
	    return dst;
	}

	static double getThreshold(Mat image) {
		int channels = image.channels();
		int cols = image.cols();
		int rows = image.rows();
		byte[] colPixel = new byte[channels];

		double total = 0;
		int count = 0;
		for (int r = 0; r < rows; ++r) {
			for (int c = 0; c < cols; ++c) {
				image.get(r, c, colPixel);
				int pv = colPixel[0] & 0xff;
				if(pv != 0){
					total += pv;
					++count;
				}
			}
		}

		double m = total / count;

		total = 0;

		for (int r = 0; r < rows; ++r) {
			for (int c = 0; c < cols; ++c) {
				image.get(r, c, colPixel);
				int pv = colPixel[0] & 0xff;
				double v = (pv - m);
				total += v * v;
			}
		}

		double mse = Math.sqrt(total / count);

        Util.mark(m);
        Util.mark(mse);
		return m;// + mse/2;
		// return 200;
	}
	public static int simpleaverage(int[] values){
		int total = 0;
		for(int i=0, len=values.length; i<len; ++i){
			total += values[i];
		}
		return total / values.length;
	}

	public static Mat transformSlope(Mat image, float slope){
//		float slope = calcSlope();
		if(slope == 0){
			return image;
		}
		Point ps0 = null, ps1 = null, ps2 = null;
		Point pd0 = null, pd1 = null, pd2 = null;
		if(slope < 0){
			ps2 = new Point(image.cols() - 1, 0);
			ps1 = new Point(0, image.rows() - 1);
			double y = ps2.y - slope * ps2.x;
			ps0 = new Point(0, y);
			
			pd0 = ps0.clone();
			pd1 = ps1.clone();
			pd2 = new Point(ps2.x, ps0.y);
		}else if(slope > 0){
			ps0 = new Point(0, 0);
			ps2 = new Point(image.cols() - 1, image.rows() - 1);
			double y = ps2.y - slope * ps2.x;
			ps1 = new Point(0, y);
			
			pd0 = ps0.clone();
			pd1 = ps1.clone();
			pd2 = new Point(ps2.x, ps1.y);
		}

		Mat transformedPlate = new Mat();
		Mat warpMat = Imgproc.getAffineTransform(new MatOfPoint2f(ps0, ps1, ps2), new MatOfPoint2f(pd0, pd1, pd2));
		Imgproc.warpAffine(image, transformedPlate, warpMat, image.size());
//		Model.saveImage("Transform slope only", transformedPlate);
		return transformedPlate;
	}
	
	public static Mat transformH(Mat mat, int xdiff){
		int halfdiff = xdiff / 2;
		int absdiff = Math.abs(halfdiff);
		int xleft = absdiff + 2;
		int xright = mat.width() - absdiff - 3;
		int xtop = xdiff % 2;
		int dh = mat.height() / 4;
		Point psLeft = new Point(xleft, dh);
		Point psRight = new Point(xright, dh);
		Point psBtm = new Point(xleft, mat.height() - 1 - dh);

		Point pdLeft = new Point(xleft + halfdiff + xtop, dh);
		Point pdRight = new Point(xright + halfdiff + xtop, dh);
		Point pdBtm = new Point(xleft - halfdiff, mat.height() - 1 - dh);
		
		Mat warpMat = Imgproc.getAffineTransform(
				new MatOfPoint2f(psLeft, psRight, psBtm), new MatOfPoint2f(pdLeft, pdRight, pdBtm));
		Mat corrected = new Mat();
		Imgproc.warpAffine(mat, corrected, warpMat, mat.size());
//		Model.saveImage(corrected, "Corrected: ", xdiff);
//		if(Model.isDebug){
//			Mat projs = CharExtrator.drawProjection(corrected);
//			Model.saveImage("corrected " + xdiff, projs);
//		}
		return corrected;
	}

	public static Mat transform(Mat mat, float slope, int tiltDiff){
//		float slope = calcSlope();
        float absSlope = Math.abs(slope);
		if(absSlope <= com.vnd.model.Config.ALLOWED_MAX_SLOPE && tiltDiff == 0){
			return mat;
		}else if(absSlope <= com.vnd.model.Config.ALLOWED_MAX_SLOPE && tiltDiff != 0){
			return transformH(mat, tiltDiff);
		}else if(absSlope > com.vnd.model.Config.ALLOWED_MAX_SLOPE && tiltDiff == 0){
			return transformSlope(mat, slope);
		}else{
			Mat tilt = transformH(mat, tiltDiff);
			return transformSlope(tilt, slope);
		}
	}
	
//	static int calcTilt(int y, int topTilt){
//		return Math.round(-(y - dh) / (fontHeight - 2) * Model.tiltDiff + topTilt);
//	}
//	
//	static void tiltPoint(Point pos, Point ps, int topTilt){
//		pos.x += calcTilt((int)ps.y, topTilt);
//	}
//	
//	static Mat transform(Mat greyPlateMat, float slope, int tiltDiff){
////		float slope = calcSlope();
//		if(slope == 0 && tiltDiff == 0){
//			return greyPlateMat;
//		}else if(slope == 0 && tiltDiff != 0){
//			return transformH(greyPlateMat, tiltDiff);
//		}
//
////		Model.tiltDiff = Model.tiltDiff + (Model.tiltDiff > 0 ? 1 : -1);
//		Point ps0 = null, ps1 = null, ps2 = null;
//		Point pd0 = null, pd1 = null, pd2 = null;
//		int btmTilt = tiltDiff / 2;
//		int topTilt = tiltDiff - btmTilt;
//		int left = Math.abs(calcTilt(0, topTilt));
//		int right = greyPlateMat.cols() - left - 1;
//		if(left >= right){
//			left = 0;
//			right = greyPlateMat.cols() - 1;
//			btmTilt = 0;
//			topTilt = 0;
//		}
//		if(slope < 0){
//			ps2 = new Point(right, 0);
//			ps1 = new Point(left, greyPlateMat.rows() - 1);
//			double y = ps2.y - slope * ps2.x;
//			ps0 = new Point(left, y);
//			
//			pd0 = ps0.clone();
//			pd1 = ps1.clone();
//			pd2 = new Point(ps2.x, ps0.y);
////			pd0.x += topTilt;
////			pd1.x -= btmTilt;
////			pd2.x += topTilt;
//		}else if(slope > 0){
//			ps0 = new Point(left, 0);
//			ps2 = new Point(right, greyPlateMat.rows() - 1);
//			double y = ps2.y - slope * ps2.x;
//			ps1 = new Point(left, y);
//			
//			pd0 = ps0.clone();
//			pd1 = ps1.clone();
//			pd2 = new Point(ps2.x, ps1.y);
////			pd0.x += topTilt;
////			pd1.x -= btmTilt;
////			pd2.x -= btmTilt;
//		}
//		
//		tiltPoint(pd0, ps0, topTilt);
//		tiltPoint(pd1, ps1, topTilt);
//		tiltPoint(pd2, ps2, topTilt);
//		
//		Mat warpMat = Imgproc.getAffineTransform(new MatOfPoint2f(ps0, ps1, ps2), new MatOfPoint2f(pd0, pd1, pd2));
//		Mat transformedPlate = new Mat();
//		Imgproc.warpAffine(greyPlateMat, transformedPlate , warpMat, greyPlateMat.size());
//		Model.saveImage(transformedPlate, "Transformed with tilt diff", tiltDiff);
//		return transformedPlate;
//	}

//	static byte[] colPixel = new byte[1];
//	static int T = 0;
//	public static Mat markRegion(Mat image){
//		Mat result = Mat.zeros(image.rows(), image.cols(), CvType.CV_8U);
//		
////		int num = 1;
//		int D = 1;
//		byte[] nums = {50};
//		for(int r=0; r<image.rows(); ++r){
//			for(int c=0; c<image.cols(); ++c){
//				image.get(r, c, colPixel);
//				int pv = colPixel[0] & 0xff;
//				if(pv > 0){
//					if(c > 0 && Main.hasLeftNeighbor(image, r, c, D)){
//						nums[0] = getLeftNeighbor(result, r, c, D);
//						result.put(r, c, nums);
//					}else if(r > 0 && Main.hasAboveNeighbor(image, r, c, D)){
//						nums[0] = getAboveNeighbor(result, r, c, D);
//						result.put(r, c, nums);
//					}else{
//						result.put(r, c, nums);
//					}
//					if(!Main.hasRightNeighbor(image, r, c, 1) && !Main.hasDownNeighbor(image, r, c, 1)){
//						nums[0] = (byte)(nums[0] * 2 % 128 + 127);
//					}
//				}else{
////					if(!Main.hasLeftNeighbor(image, r, c, D) && !Main.hasAboveNeighbor(image, r, c, D)){
////						nums[0] = (byte)(nums[0] * 2 % 256);
////					}
//				}
//			}
//		}
//		return result;
//	}
	
	
	
//	static Mat tempImage;
//	
//	static Mat getTempImage(){
//		return new Mat();
//	}
}
