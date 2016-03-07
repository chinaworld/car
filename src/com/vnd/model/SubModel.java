package com.vnd.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import com.vnd.logic.ColorFilter;
import com.vnd.logic.EdgeRegion2;
import com.vnd.util.ColorUtil;
import com.vnd.util.Util;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class SubModel {
	
	public static final int FONT_WIDTH = 32;
	public static final int FONT_HEIGHT = 64;
	public static final Size FONT_SIZE = new Size(FONT_WIDTH, FONT_HEIGHT);
	
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
	
	public static final char NOT_CHAR = '?';
	public static final char ERROR_CHAR = '~';
	public static int mark = 0;
	
	public Mat grey0 = new Mat();
	static Size gaussianSize = new Size(3, 3);
	Rect CarRect = new Rect(Config.MARGIN_LEFT, Config.MARGIN_TOP,
			Config.TO_WIDTH - Config.MARGIN_LEFT - Config.MARGIN_RIGHT, 300);
	public RefinedFontInfo[] processed;
	
	public Mat colorImage;
	public Mat gaussImage = new Mat();
	public Mat greyImage;
//	public Mat equalizedImage = new Mat();
	private boolean reverseColor = false;
	public boolean reverseColor(){
		return reverseColor;
	}
	
	public int getThresholdType(){
		return reverseColor() ? Imgproc.THRESH_BINARY_INV : Imgproc.THRESH_BINARY;
	}
	
//	public static Mat plateRegion;
//	public static Rect[] fonts;
//	public static Rect plateRect;
	
	public MainModel mainModel;
	
	public Mat edges;
	private Rect plate;
//	public int scale = 1;
	public int fontsCount = 7;
	public int dotPos = 1; //dot position in intervals

    public boolean oneRowPlate = true;
	
//	public boolean[] isMerged = new boolean[7];
//	public List<Rect> mergedRects = new ArrayList<>();
//	public static boolean isDebug = true;
	
	public char[] reconized = new char[7];
	public Mat finalPlate;
	
	//---------------Got from CharRefine4----------------------
	public float slope = 0;
	public Rect slopePlateArea;
	public Rect[] fontRects = new Rect[fontsCount];
	public int slopeRefineCount = 0;
	//---------------End got from CharRefine4----------------------
	
	private LinkedHashMap<String, Mat> images = new LinkedHashMap<>();

    public LinkedHashMap<String, Mat> getSavedImages(){
        return images;
    }
	
	public int rows, cols;
	public int tiltDiff = 0;
	public int yLine = 1;
	
	int edgeThreshold = 0;
	public Mat binaryEdges;

    public Mat[] rawFontImages;
	
	public List<Entry<String, Mat>> getImages(){
		List<Entry<String, Mat>> result = new ArrayList<>();
		result.addAll(images.entrySet());
		Collections.reverse(result);
		return result;
	}
	
	static int unique = 1;
	public void saveImage(String name, Mat image){
		if(Config.isDebug){
			//MainModel.saveImage(name, image);
			images.put(name + " " + unique++, image.clone());
			
		}
	}
	
	public void saveImage(Mat image, Object... title){
		if(Config.isDebug){
			//MainModel.saveImage(image, title);
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
	
	public int getScale(){
		return mainModel.scale;
	}
	
	public void setImage(Mat image){
		colorImage = image;
		saveImage("Sub Original", colorImage);
		
		rows = colorImage.rows();
		cols = colorImage.cols();

		Imgproc.cvtColor(colorImage, grey0, Imgproc.COLOR_BGRA2GRAY);
		
		greyImage = Mat.zeros(rows, cols, CvType.CV_8U);
		
		Imgproc.GaussianBlur(grey0, greyImage, gaussianSize, 0, 0, Imgproc.BORDER_DEFAULT);
//        System.out.println("*********reverseColor: " + reverseColor);
		
		edges = EdgeRegion2.hdiff(greyImage);
		saveImage(edges, "Initial Edges");
//		saveImage("Gauss Image", gaussImage);

//		Imgproc.cvtColor(gaussImage, greyImage, Imgproc.COLOR_BGRA2GRAY);
//		saveImage("Grey Image", greyImage);
		
//		Imgproc.equalizeHist(greyImage, equalizedImage);
//		saveImage("Equalized", equalizedImage);
		
		binaryEdges = Util.binary(edges, getEdgeThreshold(), Imgproc.THRESH_BINARY);
		saveImage("binary edges in submodel", binaryEdges);
	}
	
	int getEdgeThreshold(){
		if(edgeThreshold == 0){
			Mat plateEdges = edges.submat(plate);
			float enPercent = getScale() < 2 ? 0.6f : 0.72f;
			edgeThreshold = Util.getThreashold(plateEdges, enPercent);
		}
		return edgeThreshold;
	}
	
	public Rect containRect;
    private Rect enlarged;
    public ColorUtil.Color backColor;
    public boolean useColorFilter = false;

	public SubModel(MainModel mainModel, Rect plate, ColorUtil.Color color, boolean useColorFilter){
		this.mainModel = mainModel;
        this.useColorFilter = useColorFilter;
		containRect = expand(plate);
        enlarged = containRect;
		plate.x -= containRect.x;
		plate.y -= containRect.y;
		this.setPlate(plate);
        Mat subImg = mainModel.colorImage.submat(containRect).clone();
        if(plate.height < 16){
            enlarged.width *= 2;
            enlarged.height *= 2;

            Mat resized = new Mat();
            Size size = new Size(enlarged.width, enlarged.height);
            Imgproc.resize(subImg, resized, size);

            subImg = resized;
            plate.x *= 2;
            plate.y *= 2;
            plate.width *= 2;
            plate.height *= 2;
        }
		setImage(subImg);
//        resizePlate();
        backColor = color;
        judgeReverseColor();
	}

    public Rect resizePlate(){
        Rect rect = plate;
//		Model.edges
//		Mat plateEdges = EdgeRegion2.hdiff(Model.greyImage.submat(rect));
//		Mat plateEdges = subModel.edges.submat(rect);
//		Mat plateEdges = Model.edges.submat(rect).clone();
//		Mat binaryEdges = new Mat();
        Mat bEdges = binaryEdges.submat(rect);//Util.binary(plateEdges, 0.8f, Imgproc.THRESH_BINARY);
        saveImage(bEdges, "binary edges", ++SubModel.mark);
        Rect r = refine(bEdges, fontsCount);
        Rect result = rect.clone();
        result.x += r.x;
        result.width = r.width;
        result.y += r.y;
        result.height = r.height;
        setPlate(result);
        paddingCn();
        return plate;
    }

    Rect refine(Mat edges, int fontCount){
        EdgeRegion2 regionFinder = new EdgeRegion2(mainModel.scale, edges, fontCount, false, -1);
        List<Rect> rects = regionFinder.getRegions();
        if(Config.isDebug){
            regionFinder.markFounds("Resize in Recognizer ");
        }
        if(rects.size() > 0){
            Rect rect = rects.get(0);
            if(!useColorFilter){
                return rect;
            }
            return ColorFilter.filter(colorImage.submat(rect), rect, backColor);
        }else{
            return new Rect(0, 0, edges.cols(), edges.rows());
        }
    }

    void paddingCn(){
//        Rect plate = subModel.getPlate();
        int oldx = plate.x;
        int d = plate.width / 8;
        plate.x -= d;
        if(plate.x < 0){
            plate.x = 0;
        }
        plate.width += oldx - plate.x;
        if(plate.x + plate.width > containRect.width){
            plate.width = containRect.width - plate.x;
        }
    }
	
	private Rect expand(Rect rect){
//		Rect containRect = rect.clone();
		int dw = rect.height;
		int dh = rect.height;
        Rect containRect = expandHeight(rect, mainModel.colorImage.size(), dh);
		containRect.x -= dw;
		if(containRect.x < 0){
			containRect.x = 0;
		}
		containRect.width += dw * 2;
		if(containRect.width + containRect.x >= mainModel.colorImage.width()){
			containRect.width = mainModel.colorImage.width() - containRect.x;
		}
//		containRect.y -= dh;
//		if(containRect.y < 0){
//			containRect.y = 0;
//		}
//		containRect.height += dh * 2;
//		if(containRect.y + containRect.height >= mainModel.colorImage.height()){
//			containRect.height = mainModel.colorImage.height() - containRect.y;
//		}
		return containRect;
	}

    private static Rect expandHeight(Rect rect, Size container, int dh){
        Rect result = rect.clone();
        result.y -= dh;
        if(result.y < 0){
            result.y = 0;
        }
        result.height += dh * 2;
        if(result.y + result.height >= container.height){
            result.height = (int)container.height - result.y;
        }
        return result;
    }

	public Rect getPlate() {
		return plate;
	}

	public void setPlate(Rect plate) {
//		int d = plate.height / 2;
////		plate.x -= d;
////		if(plate.x < 0){
////			plate.x = 0;
////		}
//		plate.width += d;
//		if(plate.x + plate.width > colorImage.width()){
//			plate.width = colorImage.width() - plate.x;
//		}
		
		this.plate = plate;
	}

    public void judgeReverseColor(){
        if(backColor == ColorUtil.Color.BLUE){
            reverseColor = false;
        }else if(backColor == ColorUtil.Color.YELLOW){
            reverseColor = true;
        }else {
            Rect toJudge = expandHeight(plate, containRect.size(), 1);
            reverseColor = !ColorUtil.isRegionBlack(greyImage.submat(toJudge));
        }
    }
}
