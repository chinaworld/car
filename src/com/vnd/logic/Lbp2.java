package com.vnd.logic;

import java.util.Arrays;

import com.vnd.model.SubModel;
import org.opencv.core.Mat;

public class Lbp2 {
	static byte[] table59 = new byte[256];

	static {
		int v = 1;
		for (int i = 0; i < 256; ++i) {
			if (getHopCount(i) <= 2) {
				table59[i] = (byte)v;
				v++;
			}
		}
	}

	static int getHopCount(int i) {
		int a[] = new int[8];
		int cnt = 0;
		for (int k = 7; i != 0; --k) {
			a[k] = i & 1;
			i >>>= 1;
		}
		for (int k = 0; k < 7; ++k) {
			if (a[k] != a[k + 1]) {
				++cnt;
			}
		}
		if (a[6] != a[7]) {
			++cnt;
		}
		return cnt;
	}
	
//	static int LEVEL1 = 15;
//	static int LEVEL2 = 30;
//	static int LEVEL3 = 45;
//	static int LEVEL4 = 60;

    static int HIST_COLS = 4;
    static int HIST_ROWS = 8;
    static int HISTS_COUNT = HIST_COLS * HIST_ROWS * 4;
	
	int histCols = HIST_COLS;
	int histRows = HIST_ROWS;
	int histsCount = histCols * histRows * 4;
	Mat src;
//	int imgWidth, imgHeight;
//	static int blockCols = Model.FONT_WIDTH / histCols;
//	static int blockRows = Model.FONT_HEIGHT / histRows;
	Directions directions;
	
	static class Directions{
		int cols = SubModel.FONT_WIDTH;
		int up = -cols;
		int upleft = -cols - 1;
		int upright = -cols + 1;
		int left = -1;
		int right = 1;
		int down = cols;
		int downleft = cols - 1;
		int downright = cols + 1;
		int cols_1 = cols - 1;
		
		public Directions(int cols){
			this.cols = cols;
			up = -cols;
			upleft = -cols - 1;
			upright = -cols + 1;
			left = -1;
			right = 1;
			down = cols;
			downleft = cols - 1;
			downright = cols + 1;
			cols_1 = cols - 1;
		}
	}
	
	static int hist4Index[] = new int[60];
	static{
		Arrays.fill(hist4Index, 0, 16, 0);
		Arrays.fill(hist4Index, 16, 32, 1);
		Arrays.fill(hist4Index, 32, 48, 2);
		Arrays.fill(hist4Index, 48, 60, 3);
	}

    public Lbp2(Mat img){
        this(HIST_ROWS, HIST_COLS, img);
    }
	
	public Lbp2(int histRows, int histCols, Mat img){
		directions = new Directions(img.cols());
		src = img;
		this.histCols = histCols;
		this.histRows = histRows;
		histsCount = this.histCols * this.histRows * 4;
	}
	
	public float[] getLbpHistgram(){
		int blockCols = src.cols() / histCols;
		int blockRows = src.rows() / histRows;
		byte[] lbp = getLbp();
		float[] result = new float[histsCount];
		for(int br=0; br< histRows; ++br){
			int rowstart = br * blockRows;
			int rowend = rowstart + blockRows;
			for(int bc=0; bc< histCols; ++bc){
				int colstart = bc * blockCols;
				int colend = colstart + blockCols;
				int blockIndex = br * histCols + bc;
				int histIndexStart = blockIndex * 4;
				for(int r=rowstart; r<rowend; ++r){
					for(int c=colstart; c<colend; ++c){
						int idx = r * directions.cols + c;
						byte lbpValue = lbp[idx];
						int histIndex = hist4Index[lbpValue] + histIndexStart;
						++result[histIndex];
					}
				}
			}
		}
		return result;
	}
	public byte[] getLbp(){
		byte[] buffer = new byte[(int)src.total()];
		src.get(0, 0, buffer);
		int[] values = new int[buffer.length];
		for(int i=0; i<buffer.length; ++i){
			values[i] = buffer[i] & 0xff;
		}
		byte[] result = new byte[buffer.length];
		int[] neighbors = new int[8];
		
		int cols = directions.cols;
		int cols_1 = cols - 1;
		for(int i=cols+1, len=buffer.length - cols - 1; i<len; ++i){
			int rem = i % cols;
			if(rem == 0 || rem == cols_1){
				continue;
			}
			neighbors[0] = values[i + directions.left];
			neighbors[1] = values[i + directions.downleft];
			neighbors[2] = values[i + directions.down];
			neighbors[3] = values[i + directions.downright];
			neighbors[4] = values[i + directions.right];
			neighbors[5] = values[i + directions.upright];
			neighbors[6] = values[i + directions.up];
			neighbors[7] = values[i + directions.upleft];
			
			int center = values[i];
			int temp = 0;

			for (int k = 0; k < 8; k++) {
				temp += (neighbors[k] >= center ? 1 : 0) << k;
			}
			
			result[i] = table59[temp];
		}
		return result;
	}
	
//	Mat LBP(Mat src) {
//		directions = new Directions(src.cols());
//		int width = src.width() - 1;
//		int height = src.height() - 1;
//		Mat result = Mat.zeros(src.rows(), src.cols(), CvType.CV_8UC1);
//		// uchar table[256];
//		// lbp59table(table);
//		byte[] pixel = { 0 };
//		int[] neighborhood = new int[8];
//		for (int j = 1; j < width; j++) {
//			for (int i = 1; i < height; i++) {
//				neighborhood[7] = Util.getValue(src, i, j);
//				neighborhood[6] = Util.getValue(src, i - 1, j);
//				neighborhood[5] = Util.getValue(src, i - 1, j + 1);
//				neighborhood[4] = Util.getValue(src, i, j + 1);
//				neighborhood[3] = Util.getValue(src, i + 1, j + 1);
//				neighborhood[2] = Util.getValue(src, i + 1, j);
//				neighborhood[1] = Util.getValue(src, i + 1, j - 1);
//				neighborhood[0] = Util.getValue(src, i, j - 1);
//				int center = Util.getValue(src, i, j);
//				int temp = 0;
//
//				for (int k = 0; k < 8; k++) {
//					temp += (neighborhood[k] >= center ? 1 : 0) << k;
//				}
//				// Util.getValue( dst, i, j)=temp;
//				// Util.getValue( dst, i, j)=table[temp];
//				pixel[0] = table59[temp];
//				result.put(i, j, pixel);
//			}
//		}
//		return result;
//	}
}
