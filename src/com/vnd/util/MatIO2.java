package com.vnd.util;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;

public class MatIO2 {
	public static Mat readMat(File file) throws IOException {
		return readMat(file, CvType.CV_8UC3);
	}
	public static Mat readMat(File file, int type) throws IOException {
		BufferedImage img = ImageIO.read(file);
        if(img == null){
            return Mat.zeros(20, 20, CvType.CV_8UC3);
        }
		// int[] pixels = new int[img.getWidth() * img.getHeight()];
		byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
		// img.getData().getPixels(0, 0, img.getWidth(), img.getHeight(),
		// pixels);
		Mat mat = new Mat(img.getHeight(), img.getWidth(), type);
		mat.put(0, 0, pixels);
		return mat;
	}

	public static void writeMat(Mat image, String file){
		MatOfByte matOfByte = new MatOfByte();
        Highgui.imencode(".jpg", image, matOfByte);
        byte[] byteArray = matOfByte.toArray();
        try {
            InputStream in = new ByteArrayInputStream(byteArray);
            BufferedImage img = ImageIO.read(in);
            ImageIO.write(img, "jpg", new File(file));
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public static void copyFile(File s, File t) throws IOException {
        Files.copy(s.toPath(), t.toPath());
    }
	
	public static void copyChildrenToDir(String src, String dst) throws IOException{
		File fsrc = new File(src);
		File fdst = new File(dst);
		for(File f : fsrc.listFiles()){
			if(f.isFile()){
				FileUtils.copyFileToDirectory(f, fdst);
			}else{
				FileUtils.copyDirectoryToDirectory(f, fdst);
			}
		}
	}

	public static boolean deleteChildren(File dir) {
		File[] children = dir.listFiles();
		for (int i = 0; i < children.length; i++) {
			if (children[i].isDirectory()) {
				deleteChildren(children[i]);
			}
			boolean success = children[i].delete();
			if (!success) {
				return false;
			}
		}
		return true;
	}

	static final String SPLIT = "\n";
	public static void writeData(float[] data, String file) throws IOException {
		FileWriter writer = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(writer);
		try{
			for(float d : data){
				bw.write(String.valueOf((int)d));
				bw.write(SPLIT);
			}
		}finally{
			bw.close();
		}
	}

    public static float[] readData(String content){
        String[] lines = content.split(SPLIT);
        int size = lines[lines.length - 1].isEmpty() ? lines.length - 1 : lines.length;
        float[] result = new float[size];
        for(int i=0; i<size; ++i){
            result[i] = Float.parseFloat(lines[i]);
        }
        return result;
    }

	public static float[] readData(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		List<String> lines = new ArrayList<>();
		try{
			while((line=reader.readLine()) != null){
				lines.add(line);
			}
		}finally{
			reader.close();
		}
		float[] result = new float[lines.size()];
		for(int i=0; i<result.length; ++i){
			result[i] = Float.parseFloat(lines.get(i));
		}
		return result;
	}
	
	public static File[] listFiles(File dir, String extension){
		File[] files = dir.listFiles();
		List<File> result = new ArrayList<>();
		for(File f : files){
			if(f.getName().endsWith(extension)){
				result.add(f);
			}
		}
		return result.toArray(new File[0]);
	}
}
