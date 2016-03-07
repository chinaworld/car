package com.test;

import org.opencv.core.Rect;

public class Test {
	
	public static void main(String[] args){
		Rect r = new Rect(10, 10, 110, 110);
		change(r);
		System.out.println(r);
	}
	
	static void change(Rect r){
		r = new Rect(0, 0, 10, 10);
	}
}
