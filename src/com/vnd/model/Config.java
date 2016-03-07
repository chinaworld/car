package com.vnd.model;

public class Config {
	public static boolean isDebug = false;
	public static boolean adaptiveBinary = true;
	public static float HEIGHT_SCALE = 1f;
//	public static int BINARY_EDGE_THRESHOLD = 15;
    public static int BINARY_EDGE_THRESHOLD = 7;

    public static float BACKGROUND_PERCENT = 0.75f;
    public static int MAX_BACKGROUND_DIFF = 10;
//	public static boolean adaptiveBinary = false;
//	public static float HEIGHT_SCALE = 2f;
//	public static int BINARY_EDGE_THRESHOLD = 5;
	public static int MIN_PIXELS = 12;
	
	public static boolean rotate = false;
	public static String DATA_PATH = "C:\\Users\\Administrator\\Desktop\\CarNum\\temp\\";
    public static String CUSTOM_PATH = "C:\\Users\\Administrator\\Desktop\\CarNum\\custom\\";
	public static float WIDTH_HEIGHT_RATIO = 440 / 90f; // (90 * HEIGHT_SCALE); //114/20 + 0.2f;
	public static float MAX_FONT_HEIGHT_WIDTH_RATIO = 3; // * HEIGHT_SCALE;
	public static float MIN_FONT_HEIGHT_WIDTH_RATIO = 0.8f; // * HEIGHT_SCALE;
	public static int MIN_WIDTH = 40;
	public static int MAX_WIDTH = 90;
//	public static int MAX_WIDTH = 120;
//	public static int MAX_PIXELS = 115;
	
	public static int MARGIN_TOP = 20;
	public static int MARGIN_LEFT = 0;
	public static int MARGIN_RIGHT = 10;
	public static int MARGIN_BOTTOM = 10;
	
	public static int TO_WIDTH = 480;
	
	public static int NOT_SURE_LIMIT = 3; //exclude
	
	public static int MAX_SCALE = 4; //include
	
	public static final int cnDiff = -5;
	public static final int enDiff = 8;
	
	public static final float MAX_WIDTH_HEIGHT_RATIO = 5.5f;
	public static final int MIN_FONT_WIDTH = 5;
	
	public static final int CHECK_HW_RATIO_MIN_HEIGHT = 40; //for single font
	public static final int MAX_HW_RATIO_DIFF = 3; //for single font 
	
	public static final float KNN_MAX_PERFECT_DIFF = 1000;
	public static final float KNN_MAX_GOOD_DIFF = 4000;
	public static final float KNN_MAX_MATCHED_DIFF = 9000;
	public static final float KNN_MAX_ALLOWED_DIFF = 20000;
	
	public static final float EN_FONT_THRESHOLD = 0.65f;
	public static final float CN_FONT_THRESHOLD = 0.5f;
	
	public static UseColorStrategy colorStrategy = UseColorStrategy.NotUseColor;
	
	public static final int CHAR_RECOGNIZE_ERROR_LEVEL = 100;
	
	public static boolean ONE_CAR = true;

    public static final float ALLOWED_MAX_SLOPE = 0.008f;

    public enum UseColorStrategy{UseColor, NotUseColor, Auto}
}
