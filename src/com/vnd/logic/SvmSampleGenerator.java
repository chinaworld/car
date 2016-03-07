package com.vnd.logic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.vnd.model.Config;
import com.vnd.model.RefinedFontInfo;
import com.vnd.model.SubModel;
import com.vnd.util.MatIO2;
import com.vnd.util.Util;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import com.vnd.logic.SvmDataGenerators.SvmTrainData;

public class SvmSampleGenerator {
	static SvmSampleGenerator B8Generater = new SvmSampleGenerator('B', '8', SvmDataGenerators.B8DataCount0);
	static SvmSampleGenerator S5Generater = new SvmSampleGenerator('S', '5', SvmDataGenerators.S5Data);

	public static SvmSampleGenerator D0G = new SvmSampleGenerator('D', '0', SvmDataGenerators.D0DataCount0);
	public static SvmSampleGenerator Q0G = new SvmSampleGenerator('Q', '0', SvmDataGenerators.Q0Data);
	public static SvmSampleGenerator Z2G = new SvmSampleGenerator('Z', '2', SvmDataGenerators.Z2Data);
	
	public static void generateSamples() throws IOException{
		B8Generater.generate();
		S5Generater.generate();
		D0G.generate();
		Q0G.generate();
		Z2G.generate();
	}
	
	public static void main(String[] args) throws IOException{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		generateSamples();
        Util.mark("Success");
	}
	
	char c1, c2;
	File d1, d2;
	File dir;
	SvmTrainData dataGenerator;
	
	File prepareDir(File parent, String sub){
		File d = new File(parent, sub);
		if(d.exists()){
			MatIO2.deleteChildren(d);
		}
		if(!d.exists()){
			d.mkdir();
		}
		return d;
	}
	
	public SvmSampleGenerator(char c1, char c2, SvmTrainData dataGenerator){
		if(c1 > c2){
			this.c1 = c1;
			this.c2 = c2;
		}else{
			this.c1 = c2;
			this.c2 = c1;
		}
		dir = prepareDir(new File(Config.DATA_PATH), String.valueOf(c1) + c2);
		this.dataGenerator = dataGenerator;
	}
	
	String getTypeDir(char c){
		if('A' <= c && c <= 'Z'){
			return SampleGenerator2.EN;
		}else if('0' <= c && c <= '9'){
			return SampleGenerator2.AN;
		}else{
			return SampleGenerator2.CN;
		}
	}
	List<Mat> getSampleImages2(char c) throws IOException{
		List<Mat> result = new ArrayList<>();
		File dir = new File(SampleGenerator2.DIR);
		File[] files = dir.listFiles();
		for (File f : files) {
			String name = f.getName();
			int idx = name.indexOf(c);
			if(idx >= 0 && idx < 7){
				Mat image = MatIO2.readMat(f);
				RefinedFontInfo[] fonts = SampleGenerator2.extract(image);
				if(fonts.length != 7){
					continue;
				}
				while(idx >= 0 && idx < 7){
					result.add(getRebinaried(fonts[idx]));
					idx = name.indexOf(c, idx + 1);
				}
			}
		}
		return result;
	}
	
	static int PIXELS = (int)(SubModel.FONT_HEIGHT * SubModel.FONT_WIDTH * 0.4f);
	Mat getRebinaried2(RefinedFontInfo processedImg){
		Mat original = processedImg.getResizeClipped();
		int mean = (int)Core.mean(original).val[0];
		Mat result = Util.adaptiveBinary(original, 3, Imgproc.THRESH_BINARY);
		for(int d=30; d> -mean; --d){
			int pixels = Core.countNonZero(result);
			if(pixels < PIXELS){
				return result;
			}
//			FontBinaryer3 binaryer = new FontBinaryer3(original, d);
//			result = binaryer.binary();
			result = adaptiveBinary(original, d, Imgproc.THRESH_BINARY);
		}
		return result;
	}
	Mat getRebinaried(RefinedFontInfo processedImg){
		Mat original = processedImg.getResizeClipped();
		int mean = (int)Core.mean(original).val[0];
		Mat result = processedImg.result;
		for(int d=7; d + mean < 220; d += 2){
			int pixels = Core.countNonZero(result);
			if(pixels < PIXELS){
                Util.mark(d);
				return result;
			}
			FontBinaryer3 binaryer = new FontBinaryer3(original, d);
			result = binaryer.binary();
		}
		return result;
	}
	
	static Mat adaptiveBinary(Mat src, int diff, int thresholdType){
		Mat dst = new Mat();
		int blockSize = src.cols() / 3;
		if(blockSize % 2 == 0){
			++blockSize;
		}
//		int blockSize = 7; //src.cols() / 3;
		Imgproc.adaptiveThreshold(src, dst, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, thresholdType, blockSize, diff);
		return dst;
	}
	
	List<Mat> getSampleImages(char c) throws IOException{
		File dir = new File(getTypeDir(c), String.valueOf(c));
		List<Mat> result = new ArrayList<>();
		for(File f : dir.listFiles()){
			if(f.getName().endsWith(".jpg") && f.getName().length() == 5){ //1 + ".jpg".length() = 5
				result.add(MatIO2.readMat(f, CvType.CV_8UC1));
			}
		}
		return result;
	}
	
	void generate(char c) throws IOException{
		File toDir = prepareDir(dir, String.valueOf(c));
		List<Mat> imgs = getSampleImages(c);
		for(int i=0; i<imgs.size(); ++i){
			float[] data = dataGenerator.getTrainData(imgs.get(i));
			MatIO2.writeMat(imgs.get(i), toDir.getAbsolutePath() + "\\" + i + ".jpg");
			MatIO2.writeMat(dataGenerator.getTempImage(), toDir.getAbsolutePath() + "\\t" + i + ".jpg");
			MatIO2.writeData(data, toDir.getAbsolutePath() + "\\" + i + ".dat");
		}
	}
	
	public void generate() throws IOException{
		generate(c1);
		generate(c2);
	}
}
