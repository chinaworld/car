package com.vnd.logic;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.vnd.model.Config;
import com.vnd.model.RefinedFontInfo;
import com.vnd.model.SubModel;
import com.vnd.util.ColorUtil;
import com.vnd.util.MatIO2;
import com.vnd.util.Util;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

public class SampleGenerator2 {
	static String DIR = "C:\\Users\\Administrator\\Desktop\\CarNum\\new";
//	static String TEMP = "C:\\Users\\Administrator\\Desktop\\CarNum\\temp\\";
	static String EN = Config.DATA_PATH + "en\\";
	static String CN = Config.DATA_PATH + "cn\\";
	static String NUM = Config.DATA_PATH + "num\\";
	static String AN = Config.DATA_PATH + "an\\";

	static void prepareFolder(String sdir) {
		File temp = new File(sdir);
		if (!temp.exists())
			temp.mkdirs();
		else
			MatIO2.deleteChildren(temp);
	}

	public static void main(String[] args) throws IOException {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		Config.isDebug = false;
		File dir = new File(DIR);
		File[] files = dir.listFiles();
		prepareFolder(EN);
		prepareFolder(CN);
		prepareFolder(NUM);
		prepareFolder(AN);
		for (File f : files) {
			process(f);
		}
		MatIO2.copyChildrenToDir(NUM, AN);
		MatIO2.copyChildrenToDir(EN, AN);
        Util.mark("Finished");
	}

	static void process(File file) throws IOException {
		String name = file.getName();
		doProcess(file, name);
	}

	public static boolean isDigitOrLetter(char c0) {
		return ('A' <= c0 && c0 <= 'Z') || Character.isDigit(c0);
	}

	public static boolean isLetter(char c0) {
		return ('A' <= c0 && c0 <= 'Z');
	}
	
	public static File getToDir(char c){
		String stoDir;
		if (!isDigitOrLetter(c)) {
			stoDir = CN;
		} else if (isLetter(c)) {
			stoDir = EN;
		} else {
			stoDir = NUM;
		}
		File toDir = new File(stoDir + c);
		if (!toDir.exists()) {
			toDir.mkdir();
		}
		return toDir;
	}

	static void doProcess(File file, String name) throws IOException {
		Mat image = MatIO2.readMat(file);
		RefinedFontInfo[] fonts = extract(image);
		if(fonts.length != 7 || name.length() != 7 + 4){ //4 is ".jpg".length()
			return;
		}
		for (int i = 0; i < fonts.length; ++i) {
			File toDir = getToDir(name.charAt(i));
			
			int count = toDir.list().length / 3;
			String to = toDir.getAbsolutePath() + "\\" + count + ".jpg";
			MatIO2.writeMat(fonts[i].result, to);

            Lbp2 lbpTool = new Lbp2(fonts[i].result);
//			byte[] lbpImage = Lbp.getLbp(fonts[i].result);
            byte[] lbpImage = lbpTool.getLbp();
			Mat lbpMat = new Mat(fonts[i].result.size(), CvType.CV_8UC1);
			lbpMat.put(0, 0, lbpImage);
			String lbpFile = toDir.getAbsolutePath() + "\\lbp" + count + ".jpg";
			MatIO2.writeMat(lbpMat, lbpFile);
			
//			float[] lbpData = Lbp.getLbpHistgram(fonts[i].result);
            float[] lbpData = lbpTool.getLbpHistgram();
			String dataTo = toDir.getAbsolutePath() + "\\" + count + ".dat";
			MatIO2.writeData(lbpData, dataTo);
		}
	}

	// ------------------------------------------------------------------------------------------
	static RefinedFontInfo[] extract(EdgeRegion2 finder) {
		List<Rect> rects = finder.getRegions();
		Rect blue = Recognizer.getBlue(rects, recognizer.mainModel.colorImage);
//		if (blue != null) {
        SubModel subModel = recognizer.extract(blue, ColorUtil.Color.BLUE, false);
        RefinedFontInfo[] result = recognizer.refineSize(subModel, false);
//			RefinedFontInfo[] result = recognizer.extract(blue, ColorUtil.Color.BLUE, false);
			if (result.length == 7) {
				return result;
			}
//		} else {
			for (Rect rect : rects) {
				if (rect == blue) {
					continue;
				}
                subModel = recognizer.extract(rect, ColorUtil.Color.UNKNOWN, false);
                result = recognizer.refineSize(subModel, false);
//				result = recognizer.extract(rect, ColorUtil.Color.UNKNOWN, false);
				if (result.length == 7) {
					return result;
				}
			}
//		}
		return new RefinedFontInfo[0];
	}

	static Recognizer recognizer;
	public static RefinedFontInfo[] extract(Mat image) {
		Recognizer recognizer = new Recognizer(image);
//		Model.init(image);
//		Model.edges = EdgeRegion2.hdiff(Model.greyImage);
		Mat binaryEdges = Util.binary(recognizer.mainModel.edges, Config.BINARY_EDGE_THRESHOLD, Imgproc.THRESH_BINARY);
		
		RefinedFontInfo[] result = new RefinedFontInfo[0];
		for (int scale = 1; result.length != 7; ++scale) {
			recognizer.mainModel.scale = scale;
			EdgeRegion2 finder = new EdgeRegion2(scale, binaryEdges, recognizer.mainModel.imageSegments);
			if (!finder.isScaleAllowed() || scale > Config.MAX_SCALE) {
				return new RefinedFontInfo[0];
			}
			result = extract(finder);
		}
		return result;
	}
}
