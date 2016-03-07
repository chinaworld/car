package com.vnd.logic;

import com.vnd.model.MinMax;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Projection {
	static final int MAX_ALLOWED_GAP = 3;
	static final int minWidth = 40;
	static final int minHeight = minWidth / 4;
	static final float ratio = 440/140;
	static final float MIN_D = 1;
	
	Mat image;
	int[] hProjection;
	int[] vProjection;

	int channels;
	int cols;
	int rows;
	
	int testTime = 0;
	float difference = MIN_D;
	
	Rect rect = new Rect(-1, -1, 0, 0);
	
	public static MinMax hclip(Mat image, int min){
		int[] hproj = horizontalProjection(image);
		hproj = filterProjection(hproj, min, image.height() / 2, 1);
		return MinMax.getCnMinMaxIndices(hproj);
	}

	public static MinMax vclip(Mat image, int min){
		int[] vproj = verticalProjection(image);
		vproj = filterProjection(vproj, min, image.width() / 2, 1);
		return MinMax.getCnMinMaxIndices(vproj);
	}
	
	public Projection(Mat image){
		this.image = image;
		channels = image.channels();
		cols = image.cols();
		rows = image.rows();
	}
	
//	Mat clip(int count){
//		Mat result = image;
//		for(int i=0; i<count; ++i){
//			result = clip();
//		}
//		return result;
//	}
	
	boolean clip(){
		++testTime;
		vProjection = filterVerticalProjection();
		hProjection = filterHorizontalProjection();
//		clipByProjection();
//		vProjection = verticalProjection();
//		hProjection = horizontalProjection();
		
		Set<Integer> hvalues = new HashSet<>();
		Set<Integer> vvalues = new HashSet<>();
		
		for(int v : hProjection){
			if(v != 0)
				hvalues.add(v);
		}
		for(int v : vProjection){
			if(v != 0)
				vvalues.add(v);
		}
		
		int hv = -1, vv = -1;
		float minD = MIN_D;
		
		
		for(int h : hvalues){
			for(int v : vvalues){
				float r = (float)v / (float)h;
				float d = Math.abs(ratio - r);
				if(d < minD){
					minD = d;
					hv = h;
					vv = v;
				}
			}
		}
		
		if(minD < MIN_D && hv < 50 && vv < 120){
			difference = minD;
			for(int i = 0; i < hProjection.length; ++i){
				if(hProjection[i] != hv){
					hProjection[i] = 0;
				}else if(rect.height == 0){
					rect.y = i;
					rect.height = hv;
				}
			}
			
			for(int i = 0; i < vProjection.length; ++i){
				if(vProjection[i] != vv){
					vProjection[i] = 0;
				}else if(rect.width == 0){
					rect.x = i;
					rect.width = vv;
				}
			}
			
			clipByProjection();
			return true;
		}else if(testTime < 3){
			clipByProjection();
			return clip();
		}
		clipByProjection();
		return false;
	}
	
	void clipByProjection(){
		byte[] rowPixels = new byte[cols * channels];
		Arrays.fill(rowPixels, (byte)0);
		
		for (int r = 0; r < rows; ++r) {
			if(hProjection[r] < 1){
				image.put(r, 0, rowPixels);
			}
		}
		
		byte[] zeros = new byte[channels];
		Arrays.fill(zeros, (byte)0);
		
		for (int c = 0; c < cols; ++c) {
			if(vProjection[c] < 1){
				for(int r=0; r<rows; ++r)
					image.put(r, c, zeros);
			}
		}
	}
	
//	void clip(){
//		horizontalClip();
//		verticalClip();
////		return image;
//	}
//	
//	void horizontalClip(){
////		Mat edges = image.clone();
//		hProjection = horizontalProjection();
//		hProjection = filterProjection(hProjection, minWidth, minHeight);
//
//		byte[] rowPixels = new byte[cols * channels];
//		Arrays.fill(rowPixels, (byte)0);
//		
//		for (int r = 0; r < rows; ++r) {
//			if(hProjection[r] < 1){
//				image.put(r, 0, rowPixels);
//			}
//		}
////		return image;
//	}
	
	boolean hasProjectionInRange(int[] projection, int left, int right){
		for(int i=left; i<right; ++i){
			if(projection[i] > 0){
				return true;
			}
		}
		return false;
	}
	
	boolean hasNeighborProjection(int[] projection, int pos, int maxD){
		int left = pos - maxD;
		if(left < 0)
			left = 0;
		
		int right = pos + maxD;
		if(right >= projection.length){
			right = projection.length;
		}
		
		return hasProjectionInRange(projection, left, pos) && hasProjectionInRange(projection, pos + 1, right);
	}
	
	public static int getFilteredLength(int[] projection, int filter, int lengthFilter, int maxAllowedGap){
		int[] filtered = filterProjection(projection, filter, lengthFilter, maxAllowedGap);
		int len = 0;
		for(int i=0; i<filtered.length; ++i){
			if(filtered[i] > 0){
				++len;
			}
		}
		return len;
	}
	
	static int[] filterProjection(int[] projection, int filter, int lengthFilter, int maxAllowedGap){
		for(int i=0; i<projection.length; ++i){
			if(projection[i] < filter){
				projection[i] = 0;
			}else{
				projection[i] = 1;
			}
		}
		
//		int maxNeighborDistance = 3;
//		for(int i=1; i<projection.length; ++i){
//			if(projection[i] == 0 && hasNeighborProjection(projection, i, maxNeighborDistance)){
//				projection[i] = 1;
//			}
//		}
		
		fillGaps(projection, maxAllowedGap);
		
		for(int i=1; i<projection.length; ++i){
			
			if(projection[i] != 0){
				projection[i] = projection[i-1] + 1;
			}
		}
		
		for(int i=projection.length-2; i>=0; --i){
			
			if(projection[i] != 0 && projection[i+1] != 0){
				projection[i] = projection[i+1];
			}
		}
		
		for(int i=0; i<projection.length; ++i){
			if(projection[i] < lengthFilter){
				projection[i] = 0;
			}
		}
		return projection;
	}
	
//	void verticalClip(){
//		vProjection = verticalProjection();
//		vProjection = filterProjection(vProjection, minHeight, minWidth);
//
//		byte[] zeros = new byte[channels];
//		Arrays.fill(zeros, (byte)0);
//		
//		for (int c = 0; c < cols; ++c) {
//			if(vProjection[c] < 1){
//				for(int r=0; r<rows; ++r)
//					image.put(r, c, zeros);
//			}
//		}
////		return image;
//	}
	int[] filterVerticalProjection() {
		int[] result = verticalProjection(image);
		result = filterProjection(result, minHeight, minWidth, MAX_ALLOWED_GAP);
		return result;
	}
	
	public static int[] verticalProjection(Mat image){
		int cols = image.cols();
		int rows = image.rows();
		
		byte[] pixels = new byte[(int)image.total()];
		image.get(0, 0, pixels);
		return verticalProjection(pixels, cols, rows);
//		int channels = image.channels();
//		
//		int[] result = new int[cols];
//		byte[] colPixel = new byte[channels];
//
//		for (int c = 0; c < cols; ++c) {
//			result[c] = 0;
//			for (int r = 0; r < rows; ++r) {
//				image.get(r, c, colPixel);
////				int pv = colPixel[0] & 0xff;
//				if (colPixel[0] != 0) {
//					++result[c];
//				}
//			}
//		}
//		return result;
	}
	
	static int[] verticalProjection(byte[] pixels, int cols, int rows){
		int[] result = new int[cols];

		for (int c = 0; c < cols; ++c) {
			result[c] = 0;
			for (int r = 0; r < rows; ++r) {
				int p = r * cols + c;
				if (pixels[p] != 0) {
					++result[c];
				}
			}
		}
		return result;
	}
	
	static int[] verticalValueProjection(Mat image){
		int cols = image.cols();
		int rows = image.rows();
		int channels = image.channels();
		
		int[] result = new int[cols];
		byte[] colPixel = new byte[channels];

		for (int c = 0; c < cols; ++c) {
			result[c] = 0;
			for (int r = 0; r < rows; ++r) {
				image.get(r, c, colPixel);
				int pv = colPixel[0] & 0xff;
				result[c] += pv;
			}
		}
		return result;
	}

	public static int[] horizontalProjection(Mat image) {
		return horizontalProjection(image, 0, image.cols());
	}
	
	static void fillGaps(int[] projection, int maxAllowedGap){
		if(maxAllowedGap < 2){
			return;
		}
		List<Integer> poses = new ArrayList<>();
		for(int i=0; i<projection.length; ++i){
			if(projection[i] > 0){
				poses.add(i);
			}
		}
		for(int i=0, last = poses.size() - 1; i<last; ++i){
			int pos = poses.get(i);
			int next = poses.get(i + 1);
			int d = next - pos;
			if(d <= maxAllowedGap && d > 1){
				int toFill = projection[pos];
				for(int p=pos+1; p<next; ++p){
					projection[p] = toFill;
				}
			}
		}
	}

	static int[] horizontalProjection(Mat image, int startCol, int endCol) {
//		int cols = image.cols();
        Mat colMat = startCol == 0 && endCol == image.cols() ? image : image.colRange(startCol, endCol);
		int rows = image.rows();
//		int channels = image.channels();
		int[] result = new int[rows];
//		byte[] colPixel = new byte[channels];

		for (int r = 0; r < rows; ++r) {
			Mat rowMat = colMat.row(r);
			result[r] = Core.countNonZero(rowMat);
		}
		return result;
	}

	static int[] horizontalProjection2(Mat image, int startCol, int endCol) {
//		int cols = image.cols();
		int rows = image.rows();
		int channels = image.channels();
		int[] result = new int[rows];
		byte[] colPixel = new byte[channels];

		for (int r = 0; r < rows; ++r) {
			result[r] = 0;
			for (int c = startCol; c < endCol; ++c) {
				image.get(r, c, colPixel);
//				int pv = colPixel[0] & 0xff;
				if (colPixel[0] != 0) {
					++result[r];
				}
			}
		}
		return result;
	}

	int[] filterHorizontalProjection() {
		int[] result = horizontalProjection(image);
		result = filterProjection(result, minWidth, minHeight, MAX_ALLOWED_GAP);
		return result;
	}
}
