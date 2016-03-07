package com.vnd.logic;

import com.vnd.model.MainModel;
import com.vnd.model.SubModel;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class SpecialChar {
	static Size morphSize = new Size(3, 3);
	static Mat erode(Mat img){
		Mat eroded = new Mat();
		Imgproc.erode(img, eroded, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, morphSize));
		MainModel.saveImage(eroded, "Eroded", SubModel.mark++);
		return eroded;
	}
	public static char handleSpecial(char c, Mat img){
		if(c == '8' || c == 'B'){
			return B8(erode(img));
		}else if(c == '5' || c == 'S'){
			return S5(erode(img));
		}else if(c == 'D' || c == '0'){
			return D0(erode(img));
		}else{
			return c;
		}
	}
	static boolean hasStartBar(int[] proj, int minLen){
		int count = 0;

		int left = 0;
		int i=0;
		for(; i<proj.length; ++i){
			if(proj[i] > 3){
				break;
			}
		}
		for(; i<proj.length; ++i){
			if(proj[i] < minLen){
				++left;
			}else{
				break;
			}
		}
		for(; i<proj.length; ++i){
			if(proj[i] >= minLen){
				++count;
			}else{
				break;
			}
		}
		return left <= 2 && count >= 3;
	}
	static boolean hasEndBar(int[] proj, int minLen){
		int count = 0;

		int end = 0;
		int i=proj.length-1;
		for(; i>=0; --i){
			if(proj[i] > 3){
				break;
			}
		}
		for(; i>=0; --i){
			if(proj[i] < minLen){
				++end;
			}else{
				break;
			}
		}
		for(; i>=0; --i){
			if(proj[i] >= minLen){
				++count;
			}else{
				break;
			}
		}
		return end <= 2 && count >= 3;
	}
	static boolean hasTopHBar(int[] proj, int width){
//		int[] proj = Projection.horizontalProjection(img);
		return hasStartBar(proj, width);
	}
	static boolean hasBottomHBar(int[] proj, int width){
//		int[] proj = Projection.horizontalProjection(img);
		return hasEndBar(proj, width);
	}
	static boolean hasLeftVBar(Mat img, int height){
		int[] vproj = Projection.verticalProjection(img);
		return hasStartBar(vproj, height);
//		int count = 0;
//
//		int left = 0;
//		int i=0;
//		for(; i<vproj.length; ++i){
//			if(vproj[i] != 0){
//				break;
//			}
//		}
//		for(; i<vproj.length; ++i){
//			if(vproj[i] < height){
//				++left;
//			}else{
//				break;
//			}
//		}
//		for(; i<vproj.length; ++i){
//			if(vproj[i] >= height){
//				++count;
//			}
//		}
//		return left <= 2 && count >= 4;
	}
	static boolean hasLeftBottomCorner(Mat img){
		int size = img.cols() / 2;
		Mat sub = img.submat(img.rows() / 2, img.rows(), 0, size);
		int[] hproj = Projection.horizontalProjection(sub);
		int vstart = sub.rows()-1;
		for(; vstart>=0; --vstart){
			if(hproj[vstart] != 0){
				break;
			}
		}
		if(vstart < 7){
			return false;
		}
		return hasLeftVBar(sub, vstart) && hasBottomHBar(hproj, sub.width() - 4);
	}
	static boolean hasLeftUpCorner(Mat img){
		int size = img.cols() / 2;
		Mat sub = img.submat(0, img.rows() / 2, 0, size);
		int[] hproj = Projection.horizontalProjection(sub);
		int vstart = 0;
		for(int hp : hproj){
			if(hp == 0){
				++vstart;
			}else{
				break;
			}
		}
		if(vstart > sub.rows() - 7){
			return false;
		}

		return hasLeftVBar(sub, sub.rows() - vstart - 4) && hasTopHBar(hproj, sub.width() - 6);
	}
	static char B8(Mat img){
		int vs = img.rows() / 4;
		int ve = img.rows() - vs;
		Mat sub = img.submat(vs, ve, 0, img.cols() / 3);
		return hasLeftVBar(sub, sub.rows()) ? 'B' : '8';
//		return hasLeftUpCorner(img) && hasLeftBottomCorner(img) ? 'B' : '8';
//		int[] proj = Projection.verticalProjection(sub);
//		int count=0;
//		int height = sub.rows();
//		int left = 0;
//		int i=0;
//		for(; i<proj.length; ++i){
//			if(proj[i] != 0){
//				break;
//			}
//		}
//		for(; i<proj.length; ++i){
//			if(proj[i] < height){
//				++left;
//			}else{
//				break;
//			}
//		}
//		for(; i<proj.length; ++i){
//			if(proj[i] == height){
//				++count;
//			}
//		}
//		return (left <= 2 && count >= 3) ? 'B' : '8';
	}
	
	static char S5(Mat img){
		Mat sub = img.submat(0, img.rows() / 4, 6, img.cols() - img.cols()/4);
		int[] proj = Projection.horizontalProjection(sub);
		return hasTopHBar(proj, sub.width()) ? '5' : 'S';
//		return hasLeftUpCorner(img) ? '5' : 'S';
	}
	
	static char D0(Mat img){
		return hasLeftUpCorner(img) ? 'D' : '0';
//		if(img.cols() < 10){
//			return Model.NOT_CHAR;
//		}
//		int[] proj = Projection.verticalProjection(img);
//		int[] left = Arrays.copyOfRange(proj, 0, proj.length / 2);
//		int[] right = Arrays.copyOfRange(proj, left.length, proj.length);
//		Arrays.sort(left);
//		Arrays.sort(right);
//		if(left[0] > right[0] && left[1] > right[1] && left[2] > right[2] && 
//				left[0] + left[1] + left[2] - right[0] - right[1] - right[2] > 5){
//			return 'D';
//		}else{
//			return '0';
//		}
	}
}
