package com.vnd.logic;

import com.vnd.android.AndroidModel;
import com.vnd.util.TrainData;
import com.vnd.util.Util;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.ml.CvKNearest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class CharRecognizer2 {
	private static final int K = 3;
	private static final int HALF_K = K / 2;
	private static final int MAX_K = 6;
	static String EN = com.vnd.model.Config.DATA_PATH + "en\\";
	static String CN = com.vnd.model.Config.DATA_PATH + "cn\\";
	static String AN = com.vnd.model.Config.DATA_PATH + "an\\";
    static String NUM = com.vnd.model.Config.DATA_PATH + "num\\";
	
	static CharRecognizer2 chinese = new CharRecognizer2(CN);
	static CharRecognizer2 alphabetic = new CharRecognizer2(EN);
	static CharRecognizer2 alphaNumeric = new CharRecognizer2(AN);
	
	String samplePath;
	HashMap<Character, Integer> charMap = new HashMap<>();
	List<Character> chars = new ArrayList<>(); //must be sorted
	
	float[][][] trainData; //[index in sorted chars][char file index][content data]
	int imageCount = 0;
	CvKNearest knn;
	boolean isCn = false;
	
	public CharRecognizer2(String path){
		this.samplePath = path;
		isCn = samplePath.equals(CN);
		try {

            if(AndroidModel.instance != null){
                trainData = initFromZipStream(AndroidModel.instance.getTrainData());
            }else {
                trainData = init();
            }
//            if(path.equals(AN)){
//                trainData = concat(init(EN), init(NUM));
//            }else{
//                trainData = init(path);
//            }
		} catch (IOException e) {
			e.printStackTrace();
		}
		knn = new CvKNearest(createTrainData(), createTrainClasses(), new Mat(), false, MAX_K);
	}

    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public com.vnd.model.RecognizedResult recognize(Mat fontImage, boolean reverseColor, Mat original, boolean isLeft, boolean isRight, int averageWidth){
//		Mat fontImage = processedImg.result;
		return recognize(fontImage, reverseColor, 0, original, isLeft, isRight, averageWidth, false);
	}

    public com.vnd.model.RecognizedResult recognize(Mat fontImage, boolean reverseColor, Mat original, boolean isLeft, boolean isRight, int averageWidth, boolean isOptional){
//		Mat fontImage = processedImg.result;
        return recognize(fontImage, reverseColor, 0, original, isLeft, isRight, averageWidth, isOptional);
    }
	private com.vnd.model.RecognizedResult recognize(Mat fontImage, boolean reverseColor, int count, Mat original, boolean isLeft, boolean isRight, int averageWidth, boolean isOptional){
		Util.mark("recognize binary count: " + count);
//		Model.saveImage(fontImage, "to recognize", ++Util.mark );
        Lbp2 lbpTool = new Lbp2(fontImage);
//		float[] lbp = Lbp.getLbpHistgram(fontImage);
        float[] lbp = lbpTool.getLbpHistgram();
		Mat data = new Mat(1, lbp.length, CvType.CV_32FC1);
		fillData(lbp, 0, data);
		Mat nearests = new Mat(1, K, CvType.CV_32FC1);
		Mat dists = new Mat();
		Mat results = new Mat();
		knn.find_nearest(data, K, results, nearests, dists);

		float[] nss = new float[nearests.cols()];
		nearests.get(0, 0, nss);
		float response = nss[0];
		float[] dss = new float[dists.cols()];
		dists.get(0, 0, dss);

		if(com.vnd.model.Config.isDebug){
			Util.mark("Response: " + response);
			Util.mark("Distances: " + Arrays.toString(dss));
			Util.mark("Nearests: " + Arrays.toString(nss));
			char[] nsChars = new char[nss.length];
			for(int i=0; i<nss.length; ++i){
				nsChars[i] = chars.get((int)nss[i]);
			}
			Util.mark("Nearest chars: " + Arrays.toString(nsChars));
			Util.mark("------------------------------");
		}

		int accuracy = 0;
		for (int k = 0; k < K; k++) {
			if (nss[k] == response && dss[k] < com.vnd.model.Config.KNN_MAX_ALLOWED_DIFF)
				accuracy++;
		}

		if(accuracy == K){
			if(dss[0] < com.vnd.model.Config.KNN_MAX_PERFECT_DIFF){
				Util.mark("Perfect Match!");
                char c = chars.get((int)response);
                int diff = 0;
                if(c == '1'){
                    if(count == 0){
                        diff = 0;
                    }else if(count < 2){
                        diff = com.vnd.model.Config.CHAR_RECOGNIZE_ERROR_LEVEL;
                    }else{
                        diff = com.vnd.model.Config.CHAR_RECOGNIZE_ERROR_LEVEL;
                    }
                }
				return new com.vnd.model.RecognizedResult(c, diff);
			}else{
				Util.mark("All matched but not perfect");
				return new com.vnd.model.RecognizedResult(checkSpecialChar(nss, dss, fontImage), 1);
			}
		}
		else if(dss[0] < com.vnd.model.Config.KNN_MAX_PERFECT_DIFF){
			Util.mark("One Perfect Match");
			return new com.vnd.model.RecognizedResult(checkSpecialChar(nss, dss, fontImage), 2);
		}
		else if(dss[0] < com.vnd.model.Config.KNN_MAX_GOOD_DIFF){
			Util.mark("One Good Matched!");
			return new com.vnd.model.RecognizedResult(checkSpecialChar(nss, dss, fontImage), 3);
		}
		else if(dss[0] < com.vnd.model.Config.KNN_MAX_MATCHED_DIFF){
			Util.mark("One Matched!");
			return new com.vnd.model.RecognizedResult(checkSpecialChar(nss, dss, fontImage), 4);
		}
		else if(hasHalfAllowedDiff(dss, nss)){
			Util.mark("2 Min Matched!");
			return new com.vnd.model.RecognizedResult(checkSpecialChar(nss, dss, fontImage), 5);
		}
		else if(!isOptional){
			Util.mark("Not sure if matched, not optional char image");
			if(count > 5){
				if(!isCn){
					return new com.vnd.model.RecognizedResult(chars.get((int)response), com.vnd.model.Config.CHAR_RECOGNIZE_ERROR_LEVEL + 1);
				}
				return new com.vnd.model.RecognizedResult(chars.get((int)response), com.vnd.model.Config.CHAR_RECOGNIZE_ERROR_LEVEL);
			}else if(count < 3) {
//                FontBinaryer3 binaryer = new FontBinaryer3(info.original, isCn, reverseColor);
//                binaryer.setDiff(binaryer.getDiff() + 20 * (count + 1));
//                Mat bimg = binaryer.simpleBinary();
                SingleCharResizer resizer = new SingleCharResizer();
                com.vnd.model.RefinedFontInfo fontInfo = resizer.process(original, false, reverseColor, isCn, isLeft, isRight, 20 * (count + 1), averageWidth);
                Mat bimg = fontInfo.result;

//                MinMax minmax = Projection.hclip(bimg, 3);
//                if (minmax.min > 0 && minmax.max - minmax.min > bimg.height() * 0.7) {
//                    int end = minmax.max < bimg.height() ? minmax.max + 1 : minmax.max;
//                    bimg = bimg.rowRange(minmax.min, end);
//
//                    MinMax vmm = Projection.vclip(bimg, 3);
//                    if (vmm.min > 0 && vmm.max - vmm.min > bimg.width() * 0.7) {
//                        int ve = vmm.max < bimg.width() ? vmm.max + 1 : vmm.max;
//                        bimg = bimg.colRange(vmm.min, ve);
//                    }
//
//                    Mat to = new Mat();
//                    Imgproc.resize(bimg, to, MainModel.FONT_SIZE);
//                    bimg = to;
//                }
                com.vnd.model.MainModel.saveImage(bimg, "re-binary font less pixels", count);
                return recognize(bimg, reverseColor, ++count, original, isLeft, isRight, averageWidth, isOptional);
            }else{
//                MainModel.saveImage(info.original, "re-binary more pixels for original", count);
//                FontBinaryer3 binaryer = new FontBinaryer3(info.original, isCn, reverseColor);
//                binaryer.setDiff(binaryer.getDiff() - 20 * (count - 3 + 1));
//                Mat bimg = binaryer.simpleBinary();
                SingleCharResizer resizer = new SingleCharResizer();
                com.vnd.model.RefinedFontInfo fontInfo = resizer.process(original, false, reverseColor, isCn, isLeft, isRight,  - 20 * (count - 3 + 1), averageWidth);
                Mat bimg = fontInfo.result;
                com.vnd.model.MainModel.saveImage(bimg, "re-binary font more pixels", count);
                return recognize(bimg, reverseColor, ++count, original, isLeft, isRight, averageWidth, isOptional);
            }
//            else{
////                FontBinaryer3 binaryer = new FontBinaryer3(fontImage, isCn, reverseColor);
////                Mat bimg = binaryer.adaptiveBinary();
//                SingleCharResizer resizer = new SingleCharResizer();
//                RefinedFontInfo adaptiveFontInfo = resizer.process(info.original, true, info.reverseColor, isCn);
//                return recognize(adaptiveFontInfo.result, reverseColor, ++count, info);
//            }
		}
        else{
            Util.mark("Not sure if matched");
            return new com.vnd.model.RecognizedResult(chars.get((int) response), com.vnd.model.Config.CHAR_RECOGNIZE_ERROR_LEVEL);
        }

//		if(samplePath.equals(AN)){
//			return SpecialCharKnn.handleSpecial(c, fontImage);
//		}
//		return c;
	}

	char checkSpecialChar(float[] nss, float[] dss, Mat fontImage){
		char c = chars.get((int)nss[0]);
		int second = getSecondDiffIndex(nss);
		if(second < 0){
			return SpecialCharKnn.handleSpecial(c, fontImage);
		}
		float distanceDiff = dss[second] - dss[0];
		if(distanceDiff * 2 < dss[0]){
			char c2 = chars.get((int)nss[second]);
			return SpecialCharKnn.handleSpecial(c, c2, fontImage);
		}else{
			return c;
		}
	}
	
	int getSecondDiffIndex(float[] nss){
		float n0 = nss[0];
		for(int i = 1; i<nss.length; ++i){
			if(nss[i] != n0){
				return i;
			}
		}
		return -1;
	}
	
	boolean hasHalfAllowedDiff(float[] dss, float[] nss){
		int accuracy = 1;
		float n0 = nss[0];
		for(int i=1; i<nss.length; ++i){
			if(nss[i] == n0 && dss[i] < com.vnd.model.Config.KNN_MAX_ALLOWED_DIFF){
				++accuracy;
			}
		}
//		for(float d : dss){
//			if(d < Config.KNN_MAX_ALLOWED_DIFF){
//				++accuracy;
//			}
//		}
		return accuracy > HALF_K;
	}
	
//	void Util.mark(String message){
//		System.out.println(message);
//	}
	
	Mat createTrainData(){
		Mat data = new Mat(imageCount, Lbp2.HISTS_COUNT, CvType.CV_32FC1);
		int k = 0;
		for(int i=0; i<trainData.length; ++i){
			for(int j=0, len=trainData[i].length; j<len; ++j){
				fillData(trainData[i][j], k, data);
				++k;
			}
		}
		return data;
	}
	
	Mat createTrainClasses(){
		Mat trainClasses = new Mat(imageCount, 1, CvType.CV_32FC1);
		int k = 0;
		for(int i=0; i<trainData.length; ++i){
			Character c = chars.get(i);
			for(int j=0, len=trainData[i].length; j<len; ++j){
				trainClasses.put(k, 0, charMap.get(c));
				++k;
			}
		}
		return trainClasses;
	}
	
	void fillData(float[] content, int rowIdx, Mat data){
		data.put(rowIdx, 0, content);
	}
	
	float[][][] init() throws IOException{
		File dir = new File(samplePath);
		File[] fontDirs = dir.listFiles();
		for(File fd : fontDirs){
			chars.add(fd.getName().charAt(0));
		}
		Collections.sort(chars);
		float[][][] trainData = new float[chars.size()][][];
		for(int i=0; i<chars.size(); ++i){
			Character c = chars.get(i);
			charMap.put(c, i);
			File fontDir = new File(samplePath, c.toString());
			File[] charFiles = com.vnd.util.MatIO2.listFiles(fontDir, ".dat");
			float[][] datas = new float[charFiles.length][];
			trainData[i] = datas;
			for(int j=0; j<charFiles.length; ++j){
				datas[j] = com.vnd.util.MatIO2.readData(charFiles[j]);
				++imageCount;
			}
		}
        return trainData;
	}

    float[][][] initFromZipStream(TrainData savedData) throws IOException{
        String prefix;
        if(samplePath.equals(CN)){
            prefix = "cn/";
        }else if(samplePath.equals(AN)){
            prefix = "an/";
        }else{
            prefix = "en/";
        }
//        List<String> paths = new ArrayList<>();
        HashMap<String, String> allData = savedData.getAllData();
        for(String key : allData.keySet()){
            if(key.startsWith(prefix)){
//                paths.add(key);
                chars.add(key.charAt(prefix.length()));
            }
        }
        Collections.sort(chars);
        float[][][] trainData = new float[chars.size()][][];
        for(int i=0; i<chars.size(); ++i) {
            Character c = chars.get(i);
            charMap.put(c, i);
            String charPrefix = prefix + c + '/';
            List<String> paths = savedData.findKeys(charPrefix);
            float[][] datas = new float[paths.size()][];
            trainData[i] = datas;
            for(int j=0; j<paths.size(); ++j){
                String content = allData.get(paths.get(j));
                datas[j] = com.vnd.util.MatIO2.readData(content);
                ++imageCount;
            }
        }
        return trainData;
    }
}
