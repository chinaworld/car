package com.vnd.logic;

import org.opencv.core.Mat;

public class BinaryNeighbor {
	Mat image;
	int maxD;
//	int threshold;
	byte[] colPixel = new byte[1];
	int xsize = 8;
	int ysize = 6;
	int roundD = 3;
	int minSubCount = 4;
	int minPixelCount = 16;
	
	public BinaryNeighbor(Mat image, int maxD){
		if(image.channels() != 1){
			throw new RuntimeException("Error: image must be grey");
		}
		this.image = image;
		this.maxD = maxD;
	}
	
	public boolean hasAroundPixels(int row, int col){
		return pixelCountAround(row, col) > minPixelCount;
	}
	
	public int pixelCountAround(int row, int col){
		int topLeft = topLeftPixels(row, col);
		int topRight = topRightPixels(row, col);
		int bottomLeft = bottomLeftPixels(row, col);
		int bottomRight = bottomRightPixels(row, col);
		if(topLeft < minSubCount && topRight < minSubCount && bottomLeft >= minSubCount && bottomRight >= minSubCount){
			topLeft = bottomLeftPixels(row + ysize, col);
			topRight = bottomRightPixels(row + ysize, col);
		}else if(topLeft < minSubCount && topRight >= minSubCount && bottomLeft < minSubCount && bottomRight >= minSubCount){
			topLeft = topRightPixels(row, col + xsize);
			bottomLeft = bottomRightPixels(row, col + xsize);
		}else if(topLeft >= minSubCount && topRight >= minSubCount && bottomLeft < minSubCount && bottomRight < minSubCount){
			bottomLeft = topLeftPixels(row - ysize, col);
			bottomRight = topRightPixels(row - ysize, col);
		}else if(topLeft >= minSubCount && topRight < minSubCount && bottomLeft >= minSubCount && bottomRight < minSubCount){
			topRight = topLeftPixels(row, col - xsize);
			bottomRight = bottomLeftPixels(row, col - xsize);
		}else if(topLeft < minSubCount && topRight < minSubCount && bottomLeft >= minSubCount && bottomRight < minSubCount){
			int r = row + ysize;
			int c = col - xsize;
			topLeft = topLeftPixels(r, c);
			topRight = bottomLeftPixels(r, c);
			bottomRight = bottomRightPixels(r, c);
		}else if(topLeft >= minSubCount && topRight < minSubCount && bottomLeft < minSubCount && bottomRight < minSubCount){
			int r = row - ysize;
			int c = col - xsize;
			bottomRight = topLeftPixels(r, c);
			topRight = topRightPixels(r, c);
			bottomLeft = bottomLeftPixels(r, c);
		}else if(topLeft < minSubCount && topRight >= minSubCount && bottomLeft < minSubCount && bottomRight < minSubCount){
			int r = row - ysize;
			int c = col + xsize;
			topLeft = topLeftPixels(r, c);
			bottomRight = bottomRightPixels(r, c);
//			topRight = topRightPixels(r, c);
			bottomLeft = topRightPixels(r, c);
		}else if(topLeft < minSubCount && topRight < minSubCount && bottomLeft < minSubCount && bottomRight >= minSubCount){
			int r = row + ysize;
			int c = col + xsize;
			topLeft = bottomRightPixels(r, c);
//			bottomRight = bottomRightPixels(r, c);
			topRight = topRightPixels(r, c);
			bottomLeft = bottomLeftPixels(r, c);
		}
		
		return topLeft + topRight + bottomLeft + bottomRight;
	}
	
	int topLeftPixels(int row, int col){
		if(row < xsize || col < ysize){
			return 0;
		}
		int left = col - xsize;
		int top = row - ysize;
		return pixelCount(left, col - roundD, top, row - roundD);
	}
	
	int topRightPixels(int row, int col){
		int right = col + xsize;
		int top = row - ysize;
		if(top < 0 || right >= image.cols()){
			return 0;
		}
		return pixelCount(col + roundD, right, top, row - roundD);
	}
	
	int bottomLeftPixels(int row, int col){
		int left = col - xsize;
		int bottom = row + ysize;
		if(left < 0 || bottom >= image.rows()){
			return 0;
		}
		return pixelCount(left, col - roundD, row + roundD, bottom);
	}
	
	int bottomRightPixels(int row, int col){
		int right = col + xsize;
		int bottom = row + ysize;
		if(right >= image.cols() || bottom >= image.rows()){
			return 0;
		}
		return pixelCount(col + roundD, right, row + roundD, bottom) - 1;
	}
	
	int pixelCount(int left, int right, int top, int bottom){
		int count = 0;
		for(int r=top; r<bottom; ++r){
			for(int c=left; c<right; ++c){
				image.get(r, c, colPixel);
//				int pv = colPixel[0] & 0xff;
				if(colPixel[0] != 0){
					++count;
				}
			}
		}
		return count;
	}
	
	boolean hasNeighbor(int row, int col){
		byte[] colPixel = new byte[1];
		image.get(row, col, colPixel);
//		int pv = colPixel[0] & 0xff;
		
		if(colPixel[0] != 0){
			return true;
		}
		if((hasLeftNeighbor(row, col) && hasRightNeighbor(row, col)) || 
				(hasAboveNeighbor(row, col) && hasDownNeighbor(row, col))){
			return true;
		}
		return false;
	}
	
	boolean hasRightNeighbor(int row, int col){
		return getRightNeighbor(row, col).value != 0;
	}
	boolean hasLeftNeighbor(int row, int col){
		return getLeftNeighbor(row, col).value != 0;
	}
	boolean hasAboveNeighbor(int row, int col){
		return getAboveNeighbor(row, col).value != 0;
	}
	boolean hasDownNeighbor(int row, int col){
		return getDownNeighbor(row, col).value != 0;
	}
	
	public static class NeighborPixel{
		byte value;
		int position;
		
		public NeighborPixel(){
			this((byte)0, -1);
		}
		
		public NeighborPixel(byte value, int position){
			this.value = value;
			this.position = position;
		}
	}
	
	NeighborPixel getRightNeighbor(int row, int col){
		int right = col+maxD;
		if(right >= image.cols())
			right = image.cols();
		for(int c=col + 1; c<right; ++c){
			image.get(row, c, colPixel);
//			int pv = colPixel[0] & 0xff;
			if(colPixel[0] != 0){
				return new NeighborPixel(colPixel[0], c);
			}
		}
		return new NeighborPixel();
	}
	NeighborPixel getLeftNeighbor(int row, int col){
		int left = col-maxD;
		if(left < 0)
			left = 0;
		for(int c=col - 1; c>=left; --c){
			image.get(row, c, colPixel);
//			int pv = colPixel[0] & 0xff;
			if(colPixel[0] != 0){
				return new NeighborPixel(colPixel[0], c);
			}
		}
		return new NeighborPixel();
	}
	NeighborPixel getAboveNeighbor(int row, int col){
		int above = row-maxD;
		if(above <= 0)
			above = 0;
		for(int r=row-1; r>=above; --r){
			image.get(r, col, colPixel);
//			int pv = colPixel[0] & 0xff;
			if(colPixel[0] != 0){
				return new NeighborPixel(colPixel[0], r);
			}
		}
		return new NeighborPixel();
	}
	NeighborPixel getDownNeighbor(int row, int col){
		int down = row+maxD;
		if(down >= image.rows())
			down = image.rows();
		for(int r=row + 1; r<down; ++r){
			image.get(r, col, colPixel);
//			int pv = colPixel[0] & 0xff;
			if(colPixel[0] != 0){
				return new NeighborPixel(colPixel[0], r);
			}
		}
		return new NeighborPixel();
	}
}
