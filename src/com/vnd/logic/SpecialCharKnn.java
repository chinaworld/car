package com.vnd.logic;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;

import com.vnd.model.Config;
import com.vnd.util.MatIO2;
import com.vnd.util.Util;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.ml.CvKNearest;

public class SpecialCharKnn {
	private static final int K = 3;
	private static final int MAX_K = 6;
	
	public static SpecialCharKnn B8Knn = new SpecialCharKnn('B', '8', SvmDataGenerators.B8DataCount0);
	public static SpecialCharKnn S5Knn = new SpecialCharKnn('S', '5', SvmDataGenerators.S5Data);
	public static SpecialCharKnn D0Knn = new SpecialCharKnn('D', '0', SvmDataGenerators.D0DataCount0);
	public static SpecialCharKnn Q0Knn = new SpecialCharKnn('Q', '0', SvmDataGenerators.Q0Data);
	public static SpecialCharKnn Z2Knn = new SpecialCharKnn('Z', '2', SvmDataGenerators.Z2Data);

	public static char handleSpecial(char c, Mat img){
		if(c == '8' || c == 'B'){
			return B8Knn.recognize(img);
		}else if(c == '5' || c == 'S'){
			return S5Knn.recognize(img);
		}else if(c == 'D' || c == '0'){
			return D0Knn.recognize(img);
		}else if(c == 'Q' || c == '0'){
			return Q0Knn.recognize(img);
		}else if(c == 'Z' || c == '2'){
			return Z2Knn.recognize(img);
		}else{
			return c;
		}
	}
	

	public static char handleSpecial(char c, char c2, Mat img){
		if((c == '8' && c2 == 'B') || (c == 'B' && c2 == '8')){
			return B8Knn.recognize(img);
		}else if((c == '5' && c2 == 'S') || (c == 'S' && c2 == '5')){
			return S5Knn.recognize(img);
		}else if((c == '0' && c2 == 'D') || (c == 'D' && c2 == '0')){
			return D0Knn.recognize(img);
		}else if((c == 'O' && c2 == 'D') || (c == 'D' && c2 == 'O')){
			return D0Knn.recognize(img) == '0' ? 'O' : 'D';
		}else if((c == 'Q' && c2 == '0') || (c == '0' && c2 == 'Q')){
			return Q0Knn.recognize(img);
		}else if((c == 'Q' && c2 == 'O') || (c == 'O' && c2 == 'Q')){
			return D0Knn.recognize(img) == '0' ? 'O' : 'Q';
		}else if((c == 'Z' && c2 == '2') || (c == '2' && c2 == 'Z')){
			return Z2Knn.recognize(img);
		}else{
			return c;
		}
	}
	
	char c1, c2;
	SvmDataGenerators.SvmTrainData dataGenerator;
	CvKNearest knn;
	
	public SpecialCharKnn(char c1, char c2, SvmDataGenerators.SvmTrainData dataGenerator){
		if(c1 > c2){
			this.c1 = c1;
			this.c2 = c2;
		}else{
			this.c1 = c2;
			this.c2 = c1;
		}
		this.dataGenerator = dataGenerator;
//		knn = new CvKNearest(createTrainData(), createTrainClasses(), new Mat(), false, MAX_K);
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
		knn = new CvKNearest(data, resMat, new Mat(), false, MAX_K);
	}
	
	public char recognize(Mat img){
		float[] data = dataGenerator.getTrainData(img);
		Mat mdata = new Mat(1, data.length, CvType.CV_32FC1);
		mdata.put(0, 0, data);
		Mat nearests = new Mat(1, K, CvType.CV_32FC1);
		Mat dists = new Mat();
		Mat results = new Mat();
		float response = knn.find_nearest(mdata, K, results, nearests, dists);
		
		
		float[] nss = new float[nearests.cols()];
		nearests.get(0, 0, nss);
//		float response = nss[0];
		float[] dss = new float[dists.cols()];
		dists.get(0, 0, dss);
		
		if(Config.isDebug){
			Util.mark("***********************SpecialKnn******");
			Util.mark("Response char: " + (char)response);
			Util.mark("Response: " + response);
			Util.mark("Distances: " + Arrays.toString(dss));
			Util.mark("Nearests: " + Arrays.toString(nss));
			char[] nsChars = new char[nss.length];
			for(int i=0; i<nss.length; ++i){
				nsChars[i] = (char)nss[i];
			}
			Util.mark("Nearest chars: " + Arrays.toString(nsChars));
			Util.mark("***********************");
		}
//		float res = knn.predict(mdata);
		return (char)response;
	}
}
