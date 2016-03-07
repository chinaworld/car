package com.vnd.model;

import java.util.*;
import java.util.Map.Entry;

import com.vnd.logic.EdgeRegion2;
import com.vnd.logic.ImageSegments;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class MainModel {
	
	public static final int FONT_WIDTH = 32;
	public static final int FONT_HEIGHT = 64;
	public static final Size FONT_SIZE = new Size(FONT_WIDTH, FONT_HEIGHT);
    static Size gaussianSize = new Size(3, 3);
	
	public static Mat FontOne = Mat.zeros(FONT_SIZE, CvType.CV_8UC1);
	public static Mat FontChuan = Mat.zeros(FONT_SIZE, CvType.CV_8UC1);
	static{
		int barWidth = 6;
		int s = (FONT_WIDTH - barWidth) / 2;
		int e = s + barWidth;
		Core.rectangle(FontOne, new Point(s, 0), new Point(e, FONT_HEIGHT), new Scalar(255), Core.FILLED);
		Core.rectangle(FontChuan, new Point(s, 0), new Point(e, FONT_HEIGHT), new Scalar(255), Core.FILLED);
		Core.rectangle(FontChuan, new Point(0, 0), new Point(barWidth, FONT_HEIGHT), new Scalar(255), Core.FILLED);
		Core.rectangle(FontChuan, new Point(FONT_WIDTH - barWidth, 0), new Point(FONT_WIDTH, FONT_HEIGHT), 
				new Scalar(255), Core.FILLED);
	}
	
//	public static final char NOT_CHAR = '?';
	public static int mark = 0;
	
	Mat grey0 = new Mat();
	Rect CarRect = new Rect(Config.MARGIN_LEFT, Config.MARGIN_TOP,
			Config.TO_WIDTH - Config.MARGIN_LEFT - Config.MARGIN_RIGHT, 300);
	public RefinedFontInfo[] processed;
	
	public Mat colorImage;
	public Mat gaussImage = new Mat();
	public Mat greyImage;
	public Mat equalizedImage = new Mat();
//	public boolean reverseColor = false;
//
//	public int getThresholdType(){
//		return reverseColor ? Imgproc.THRESH_BINARY_INV : Imgproc.THRESH_BINARY;
//	}
	
//	public static Mat plateRegion;
//	public static Rect[] fonts;
//	public static Rect plateRect;
	
	public Mat edges;
	public int scale = 1;
	public int fontsCount = 7;

    public ImageSegments imageSegments;
//	public int dotPos = 1; //dot position in intervals
	
//	public boolean[] isMerged = new boolean[7];
//	public List<Rect> mergedRects = new ArrayList<>();

	public char[] reconized = new char[7];
	public Mat finalPlate;
	
//	//---------------Got from CharRefine4----------------------
//	public float slope = 0;
//	public Rect slopePlateArea;
//	public Rect[] fontRects = new Rect[fontsCount];
//	public int slopeRefineCount = 0;
//	//---------------End got from CharRefine4----------------------
	
	private static LinkedHashMap<String, Mat> images = new LinkedHashMap<>();
	
	public int rows, cols;
	public int tiltDiff = 0;
	public int yLine = 1;
	
	public static List<Entry<String, Mat>> getImages(){
		List<Entry<String, Mat>> result = new ArrayList<>();
		result.addAll(images.entrySet());
		Collections.reverse(result);
		return result;
	}

    public static void addAllImages(Map<String, Mat> subImages){
        images.putAll(subImages);
    }
	
	static int unique = 1;
	public static void saveImage(String name, Mat image){
		if(Config.isDebug){
			images.put(name + " " + unique++, image.clone());
		}
	}
	
	public static void saveImage(Mat image, Object... title){
		if(Config.isDebug){
			StringBuilder sb = new StringBuilder();
			for(Object s : title){
				sb.append(s).append(" ");
			}
			images.put(sb.toString() + unique++, image.clone());
		}
	}
	
//	public static void clearImages(){
//		images.clear();
//	}
	
	public MainModel(Mat colorImage1){
//		slopeRefineCount = 0;
//		images.clear();
//		finalPlate = null;
//		reverseColor = false;
		images.clear();
		saveImage("Original 0", colorImage1);
		float resizeScale = (float)Config.TO_WIDTH / colorImage1.width();
		float heightScale = resizeScale * Config.HEIGHT_SCALE;
		int toHeight = (int)(colorImage1.height() * heightScale);
		Size size = new Size(Config.TO_WIDTH, toHeight);
		Mat temp = new Mat(size, colorImage1.type());
		Imgproc.resize(colorImage1, temp, size);
//		Model.colorImage = colorImage;
		CarRect.height = toHeight - Config.MARGIN_TOP - Config.MARGIN_BOTTOM;
		this.colorImage = temp.submat(CarRect);
		saveImage("Original 1", colorImage);
		
		rows = colorImage.rows();
		cols = colorImage.cols();

		Imgproc.cvtColor(colorImage, grey0, Imgproc.COLOR_BGRA2GRAY);
		
		greyImage = Mat.zeros(rows, cols, CvType.CV_8U);
		
		Imgproc.GaussianBlur(grey0, greyImage, gaussianSize, 0, 0, Imgproc.BORDER_DEFAULT);
		
		edges = EdgeRegion2.hdiff(greyImage);
		saveImage(edges, "Initial Edges");
//		saveImage("Gauss Image", gaussImage);

//		Imgproc.cvtColor(gaussImage, greyImage, Imgproc.COLOR_BGRA2GRAY);
//		saveImage("Grey Image", greyImage);
		
		Imgproc.equalizeHist(greyImage, equalizedImage);
		saveImage("Equalized", equalizedImage);
	}
}
