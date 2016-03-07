package com.vnd.logic;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import com.vnd.model.Config;
import com.vnd.util.MatIO2;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.ml.CvSVM;

public class SpecialCharSvm {
	public static SpecialCharSvm B8Svm = new SpecialCharSvm('B', '8', SvmDataGenerators.B8Data);
	public static SpecialCharSvm S5Svm = new SpecialCharSvm('S', '5', SvmDataGenerators.S5Data);

	public static char handleSpecial(char c, Mat img){
		if(c == '8' || c == 'B'){
			return B8Svm.recognize(img);
		}else if(c == '5' || c == 'S'){
			return S5Svm.recognize(img);
		}else if(c == 'D' || c == '0'){
			return c;
		}else{
			return c;
		}
	}
	
	char c1, c2;
	SvmDataGenerators.SvmTrainData dataGenerator;
	CvSVM svm;
	
	public SpecialCharSvm(char c1, char c2, SvmDataGenerators.SvmTrainData dataGenerator){
		if(c1 > c2){
			this.c1 = c1;
			this.c2 = c2;
		}else{
			this.c1 = c2;
			this.c2 = c1;
		}
		this.dataGenerator = dataGenerator;
		svm = new CvSVM();
		try {
			train();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	float[][] readData(char c, File dir) throws IOException{
		File cdir = new File(dir, String.valueOf(c));
		File[] children = cdir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".dat");
			}
		});
		float[][] result = new float[children.length][];
		for(int i=0; i<children.length; ++i){
			File f = children[i];
			if(f.getName().endsWith(".dat")){
				result[i] = MatIO2.readData(f);
			}
		}
		return result;
	}
	
	void train() throws IOException{
		File dir = new File(Config.DATA_PATH, String.valueOf(c1) + c2);
		float[][] c1data = readData(c1, dir);
		float[][] c2data = readData(c2, dir);
		if(c1data.length <= 0 || c2data.length <= 0 || c1data[0].length != c2data[0].length){
			throw new IOException("No sample data for " + c1 + " or " + c2);
		}
		int rows = c1data.length + c2data.length;
		int cols = c1data[0].length;
//		float[] adata = new float[rows * cols];
		Mat data = new Mat(rows, cols, CvType.CV_32FC1);
		float[] resp = new float[rows];
		for(int i=0; i<c1data.length; ++i){
			data.put(i, 0, c1data[i]);
			resp[i] = c1;
		}
		for(int i=c1data.length; i<rows; ++i){
			data.put(i, 0, c2data[i - c1data.length]);
			resp[i] = c2;
		}
		Mat resMat = new Mat(rows, 1, CvType.CV_32FC1);
		resMat.put(0, 0, resp);
		svm.train(data, resMat);
	}
	
	public char recognize(Mat img){
		float[] data = dataGenerator.getTrainData(img);
		Mat mdata = new Mat(1, data.length, CvType.CV_32FC1);
		mdata.put(0, 0, data);
		float res = svm.predict(mdata);
		return (char)res;
	}
}
