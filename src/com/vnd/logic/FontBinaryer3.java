package com.vnd.logic;

import com.vnd.model.Config;
import com.vnd.model.MinMax;
import com.vnd.util.Util;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class FontBinaryer3 {
	Mat font;
//	byte[] pixels;
//	int[] pvalues;
//	int splitIndex;
	int diff = Config.enDiff;
	int splitCount = 3;
	int thresholdType = Imgproc.THRESH_TOZERO;
	float thresholdRatio = Config.EN_FONT_THRESHOLD;
	boolean isChinese;

    MinMax sampleLeftRight;
//	float maxWhiteRatio = 0.4f;
//	float minWhiteRatio = 0.35f;

//	public FontBinaryer3(Mat greyFont, boolean isChinese, int moreDiff){
//		this(greyFont, isChinese);
//		
//		if(Model.reverseColor){
//			diff -= moreDiff;
//		}else{
//			diff += moreDiff;
//		}
//	}

    //Not used in app
	public FontBinaryer3(Mat greyFont, int diff){
		this.font = greyFont;
		this.diff = diff;
	}

    public FontBinaryer3(Mat greyFont, boolean isChinese, boolean reverseColor){
        this(greyFont, isChinese, reverseColor, null);
    }
	
	public FontBinaryer3(Mat greyFont, boolean isChinese, boolean reverseColor, MinMax sampleLeftRight){
		this.font = greyFont;
//		diff = isChinese ? Config.cnDiff : Config.enDiff;
		this.isChinese = isChinese;
        if(sampleLeftRight != null) {
            if (sampleLeftRight.min < 0) {
                sampleLeftRight.min = 0;
            }
            if (sampleLeftRight.max >= greyFont.width()) {
                sampleLeftRight.max = greyFont.width() - 1;
            }
        }
        this.sampleLeftRight = sampleLeftRight;
		if(isChinese){
//			maxWhiteRatio = 0.4f;
			diff = Config.cnDiff;
			thresholdRatio = Config.CN_FONT_THRESHOLD;
		}
		if(reverseColor){
//			diff = isChinese ? -2 : -Config.cnDiff;
			thresholdType = Imgproc.THRESH_TOZERO_INV;
		}
	}
	
	void binary(Mat from, Mat to){
        Mat sample = from;
        int d = diff;
//        if(isChinese){
//            sample = from.colRange(from.cols() / 3, from.cols());
//        }
        if(sampleLeftRight != null){
            sample = from.colRange(sampleLeftRight.min, sampleLeftRight.max + 1);
            d -= 5;
        }
		double mean = Core.mean(sample).val[0] + d; //(Model.reverseColor ? diff : -diff);
		Imgproc.threshold(from, to, mean, 255, thresholdType);
		
//		Mat check = isChinese ? to.colRange(to.cols() / 3, to.cols()) : to;
//		int noneZero = Core.countNonZero(check);
//		float r = noneZero / (float)check.total();
//		if(r > 1 - thresholdRatio){
//			Util.mark("+++++++Using ratio binary");
//			Mat ratioSrc = isChinese ? from.colRange(to.cols() / 3, to.cols()) : from;
//			int t = Util.getThreashold(ratioSrc, thresholdRatio, thresholdType);
//			Imgproc.threshold(from, to, t, 255, thresholdType);
//		}
		
//		int step = Model.reverseColor ? -5 : 5;
//		for(int i=1; i<=10; ++i){
//			Util.mark("More binary threshold ", i);
//			double thresh = mean + i*step;
//			int noneZero = Core.countNonZero(to);
//			float r = noneZero / (float)to.total();
//			if(r > maxWhiteRatio){
//				Imgproc.threshold(from, to, thresh, 255, thresholdType);
//			}else if(r < minWhiteRatio){
//				Imgproc.threshold(from, to, thresh - step, 255, thresholdType);
//				break;
//			}else{
//				break;
//			}
//		}
	}
	
	void binary(Mat from, Mat to, int startRow, int endRow){
		Mat src = from.rowRange(startRow, endRow);
		Mat dst = to.rowRange(startRow, endRow);
		binary(src, dst);
	}
	
	public Mat simpleBinary(){
		Mat result = new Mat(font.size(), CvType.CV_8UC1);
		binary(font, result);
		return result;
	}

    public Mat adaptiveBinary(){
        return Util.adaptiveBinary(font, diff - 2, thresholdType == Imgproc.THRESH_TOZERO ? Imgproc.THRESH_BINARY : Imgproc.THRESH_BINARY_INV);
    }
	
	public Mat binary(){
		Mat result = new Mat(font.size(), CvType.CV_8UC1);
		int height = font.height() / splitCount;
		int last = splitCount - 1;
		for(int i=0; i<last; ++i){
			int start = i * height;
			binary(font, result, start, start + height);
		}
		binary(font, result, last * height, font.height());
		
		return result;
	}

	public int getDiff() {
		return diff;
	}

	public void setDiff(int diff) {
		this.diff = diff;
	}
}
