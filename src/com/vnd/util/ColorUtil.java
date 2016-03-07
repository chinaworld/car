package com.vnd.util;

import com.vnd.model.Config;
import com.vnd.model.MainModel;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.Arrays;

public class ColorUtil {
//	static boolean isBlue(int r, int g, int b){
//		return b*2 - g - r > 50;
//	}

    public static abstract class IsColor{
		public abstract boolean isColor(int r, int g, int b);

        public boolean isColor(Mat img){
            return isColor(img, 0.3f);
        }
		
		public boolean isColor(Mat img, float percent){
			if(img.channels() != 3){
				return false;
			}
			byte[] data = new byte[(int)img.total() * 3];
			img.get(0, 0, data);
			int count = 0;
			for(int i=0; i<data.length; i+=3){
				if(isColor(data[i + 2] & 0xff, data[i+1] & 0xff, data[i] & 0xff)){
					++count;
				}
			}
			return count / (float)img.total() > percent;
		}
	}
	
	public static class IsBlue extends IsColor{
		public static IsBlue instance = new IsBlue();

		@Override
		public boolean isColor(int r, int g, int b) {
			return b*2 - g - r > 50;
		}
		
	}

    public static class IsYellow extends IsColor{
		public static IsYellow instance = new IsYellow();

		@Override
		public boolean isColor(int r, int g, int b){
			if(r > 0x70 && g > 0x70 && b < 0xA0){
				if(Math.abs(r - g) < 0x30 && Math.abs(r + g - b * 2) > 0x25){
					return true;
				}
			}
			return false;
		}
		
	}
	
//	static Color getColor(int r, int g, int b){
//		if(isBlue(r, g, b)){
//			return Color.blue;
//		}else if(isYellow(r, g, b)){
//			return Color.yellow;
//		}
//	}
	
//	final static int SAMPLE_WIDTH = 16;
	
	public static boolean isRegionYellow(Mat img){
		return isRegionWithColor(img, IsYellow.instance);
	}
	
	public static boolean isRegionBlue(Mat img){
		return isRegionWithColor(img, IsBlue.instance);
	}

    public static IsColor getColorJudge(Color color){
//        switch (color){
//            case Color.BLUE:
//                return IsBlue.instance;
//        }
        if(color == Color.BLUE){
            return IsBlue.instance;
        }else if(color == Color.YELLOW){
            return IsYellow.instance;
        }else{
            return null;
        }
    }

    public static boolean isRegionBlack(Mat img){
        MainModel.saveImage(img, "Image to judge bg is black");
        Mat sample = getSample(img).clone();
//        Mat binary = Util.binary(sample, 0.7f, Imgproc.THRESH_BINARY);
//        int mean = (int)Core.mean(sample).val[0];
//        Mat binary = Util.binary(sample, mean, Imgproc.THRESH_BINARY);
//        Mat equalizedImage = new Mat();
//        Imgproc.equalizeHist(sample, equalizedImage);
//        Mat binary = Util.binary(equalizedImage, 127, Imgproc.THRESH_BINARY);
        Mat binary = Util.adaptiveBinary(sample, 0, Imgproc.THRESH_BINARY);
//        int dh = binary.height() / 8;
//        if(dh > 0){
//            binary = binary.rowRange(dh, binary.height() - dh);
//        }
        int nonZero = Core.countNonZero(binary);
        int zero = (int)binary.total() - nonZero;
        MainModel.saveImage(binary, "judge bg is black", zero > nonZero);
        return zero > nonZero;
    }

//	public static final int UNKNOWN = 0;
//	public static final int BLUE = 1;
//	public static final int YELLOW = 2;

	public enum Color {
		UNKNOWN, BLUE, YELLOW
	}

	public static Color getColor(Mat img){
		if(isRegionBlue(img)){
			return Color.BLUE;
		}else if(isRegionYellow(img)){
			return Color.YELLOW;
		}else{
			return Color.UNKNOWN;
		}
	}

    static final int BLOCKS = 4;
	public static boolean horizontalEquallyBright(Mat img){
        if(img.width() < Config.MIN_WIDTH / 4){
            return true;
        }
        int wid = img.width() / BLOCKS;
        double[] means = new double[BLOCKS];
        for(int i=0; i<BLOCKS; ++i) {
            Mat sub = img.colRange(i*wid, (i+1)*wid);
            //int t = Util.getThreashold(sub, Config.BACKGROUND_PERCENT);
            Mat bak = Util.binary(sub, Config.BACKGROUND_PERCENT, Imgproc.THRESH_TOZERO_INV);
            means[i] = Core.mean(bak).val[0];
        }
        Arrays.sort(means);
        return means[BLOCKS - 1] - means[0] < Config.MAX_BACKGROUND_DIFF;
    }

	private static Mat getSample(Mat img){
		int SAMPLE_WIDTH = (int)(img.height() * 1.6);
		Mat sample;
		if(img.width() < SAMPLE_WIDTH){
			sample = img;
		}else{
			int start = (img.width() - SAMPLE_WIDTH) / 2;
			sample = img.colRange(start, start + SAMPLE_WIDTH);
		}
		return sample;
	}
	
	private static boolean isRegionWithColor(Mat img, IsColor colorJudger){
		Mat sample = getSample(img);
		boolean result = colorJudger.isColor(sample);
		MainModel.saveImage(sample, "Image sample color matched ", result);
		MainModel.saveImage(img, "Image color matched ", result);
		return result;
	}

//	static boolean isColor(Mat img, IsColor colorFunc){
//		if(img.channels() != 3){
//			return false;
//		}
//		byte[] data = new byte[(int)img.total() * 3];
//		img.get(0, 0, data);
//		int count = 0;
//		for(int i=0; i<data.length; i+=3){
//			if(colorFunc.isColor(data[i + 2], data[i+1], data[i])){
//				++count;
//			}
//		}
//		return count / (float)img.total() > 0.5f;
//	}
	
}
