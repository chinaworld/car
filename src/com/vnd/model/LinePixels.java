package com.vnd.model;

public class LinePixels {
    byte[] linePixels;
	int current = -1;
    int start, end;
	public LinePixels(byte[] linePixels){
        this(linePixels, 0, linePixels.length);
//		this.linePixels = linePixels;
//        this.start = 0;
//        this.end = linePixels.length;
	}

    public LinePixels(byte[] pixels, int start, int end){
        this.linePixels = pixels;
//        this.start = start;
        this.end = end;
        this.current = start - 1;
    }

    public void setCurrent(int value){
        current = value;
    }
	
	public int nextPixelPos(){
		++current;
		while(current < end){
			if(linePixels[current] != 0){
				return current;
			}else{
				++current;
			}
		}
		return -1;
	}
}