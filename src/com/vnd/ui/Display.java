package com.vnd.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.*;

import com.vnd.logic.Lbp2;
import com.vnd.logic.SampleGenerator2;
import com.vnd.model.Config;
import com.vnd.util.MatIO2;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;


public class Display extends Component {
	private static final long serialVersionUID = 1L;
	BufferedImage img;
    Mat mat;
    byte[] byteArray;
	
	public void setMouseMotionListener(MouseMotionListener mouseMoveListener){
		addMouseMotionListener(mouseMoveListener);
	}
	
	public Display(BufferedImage img){
		this.img = img;
	}
	
	public Display(Mat image){
        mat = image;
		MatOfByte matOfByte = new MatOfByte();
        Highgui.imencode(".jpg", image, matOfByte);
        byteArray = matOfByte.toArray();
        try {
            InputStream in = new ByteArrayInputStream(byteArray);
            img = ImageIO.read(in);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        addContextPopup();
	}

    void addContextPopup() {
        this.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    Icon icon = new ImageIcon(byteArray);
                    String text = (String)JOptionPane.showInputDialog(getParent(),
                            "Input Char", "Input Char", JOptionPane.OK_CANCEL_OPTION, icon,
                            null, null);
                    char c = text.trim().charAt(0);
                    try {
                        save(c);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }


    static String DIR = "C:\\Users\\Administrator\\Desktop\\CarNum\\new";
    //	static String TEMP = "C:\\Users\\Administrator\\Desktop\\CarNum\\temp\\";
    static String EN = Config.DATA_PATH + "en\\";
    static String CN = Config.DATA_PATH + "cn\\";
    static String NUM = Config.DATA_PATH + "num\\";
    static String AN = Config.DATA_PATH + "an\\";

    public static File getToDir(char c){
        String stoDir;
        if (!SampleGenerator2.isDigitOrLetter(c)) {
            stoDir = CN;
        } else if (SampleGenerator2.isLetter(c)) {
            stoDir = EN;
        } else {
            stoDir = NUM;
        }
        File toDir = new File(stoDir + c);
        if (!toDir.exists()) {
            toDir.mkdirs();
        }
        return toDir;
    }

    public static File getToAnDir(char c){
        String stoDir = AN;
        File toDir = new File(stoDir + c);
        if (!toDir.exists()) {
            toDir.mkdirs();
        }
        return toDir;
    }

    static int getAvailableNumInDir(char c){
        File toDir = getToDir(c);
        String[] names = toDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".dat");
            }
        });
        int[] nums = new int[names.length];
        for(int i=0; i<names.length; ++i){
            int pDot = names[i].lastIndexOf('.');
            String s = names[i].substring(0, pDot);
            nums[i] = Integer.parseInt(s);
        }
        if(nums.length == 0){
            return 1;
        }
        Arrays.sort(nums);
        return nums[nums.length - 1] + 1;
    }

    void save(File toDir, int count) throws IOException {
        String to = toDir.getAbsolutePath() + "\\" + count + ".jpg";
        MatIO2.writeMat(mat, to);

        Lbp2 lbpTool = new Lbp2(mat);
//			byte[] lbpImage = Lbp.getLbp(fonts[i].result);
        byte[] lbpImage = lbpTool.getLbp();
        Mat lbpMat = new Mat(mat.size(), CvType.CV_8UC1);
        lbpMat.put(0, 0, lbpImage);
        String lbpFile = toDir.getAbsolutePath() + "\\lbp" + count + ".jpg";
        MatIO2.writeMat(lbpMat, lbpFile);

//			float[] lbpData = Lbp.getLbpHistgram(fonts[i].result);
        float[] lbpData = lbpTool.getLbpHistgram();
        String dataTo = toDir.getAbsolutePath() + "\\" + count + ".dat";
        MatIO2.writeData(lbpData, dataTo);
    }

    void save(char c) throws IOException {

        File toDir = getToDir(c);

        int count = getAvailableNumInDir(c);
        save(toDir, count);

        if(SampleGenerator2.isDigitOrLetter(c)){
            save(getToAnDir(c), count);
        }
    }
	
//	public static JPanel createDispayPanel(Display... displays){
//		GridBagLayout layout = new GridBagLayout();
//		GridBagConstraints c = new GridBagConstraints();
//		
//		JPanel panel = new JPanel(layout);
//        
//        for(int i=0; i<displays.length; ++i){
//        	c.gridx = i % 3;
//        	c.gridy = i / 3;
//        	panel.add(displays[i], c);
//        }
//        return panel;
//	}
//	
//	public static JPanel createDispayPanel(Mat... images){
//		Display[] ds = new Display[images.length];
//		for(int i=0; i<ds.length; ++i){
//			ds[i] = new Display(images[i]);
//		}
//		
//		return createDispayPanel(ds);
//	}
//	
//
//	static File[] files;
//	static int current = 0;
//	public static void display(String DIR){
//		File dir = new File(DIR);
//		files = dir.listFiles();
//		Mat[] images = Main.load(files[0].getAbsolutePath());
//		final JFrame f = new JFrame("Image Display");
//		Container container = f.getContentPane();
//		container.setLayout(new BorderLayout());
//		
//		JToolBar toolbar = new JToolBar();
//		JButton btnNext = new JButton("Next");
//		toolbar.add(btnNext);
//		container.add(toolbar, BorderLayout.NORTH);
//		
//		f.setPreferredSize(new Dimension(1320, 600));
//		JPanel panel = createDispayPanel(images);
//		final JScrollPane scrollPane = new JScrollPane(panel);
//		scrollPane.setPreferredSize(new Dimension(1320, 600));
//        
//        f.addWindowListener(new WindowAdapter(){
//                public void windowClosing(WindowEvent e) {
//                    System.exit(0);
//                }
//            });
//        container.add(scrollPane);
//        f.pack();
//        f.setVisible(true);
//
//		btnNext.addActionListener(new ActionListener() {
//			
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				current = (current + 1) % files.length;
//				Mat[] images = Main.load(files[current].getAbsolutePath());
//				JPanel panel = createDispayPanel(images);
//				scrollPane.setViewportView(panel);
//				panel.revalidate();
//				scrollPane.revalidate();
//				scrollPane.repaint();
//				f.revalidate();
//				f.repaint();
//			}
//		});
//	}
	
//	public static JPanel createDispayPanel(String imgFile){
//		try{
//			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//			Mat[] imgs = Main.load(imgFile);
//			return createDispayPanel(imgs);
//		}catch(Throwable e){
//			e.printStackTrace();
//			throw e;
//		}
//	}
	
//	public static void show(Display... displays){
//		JFrame f = new JFrame("Image Display");
//		GridBagLayout layout = new GridBagLayout();
//		GridBagConstraints c = new GridBagConstraints();
//		
////		f.setLayout(layout);
//		f.setPreferredSize(new Dimension(1320, 600));
//		JPanel panel = new JPanel(layout);
////		panel.setPreferredSize(new Dimension(1280, 600));
//		JScrollPane scrollPane = new JScrollPane(panel);
//		scrollPane.setPreferredSize(new Dimension(1320, 600));
////		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
//        
//        f.addWindowListener(new WindowAdapter(){
//                public void windowClosing(WindowEvent e) {
//                    System.exit(0);
//                }
//            });
//        for(int i=0; i<displays.length; ++i){
//        	c.gridx = i % 3;
//        	c.gridy = i / 3;
//        	panel.add(displays[i], c);
//        }
////        for(Display d : displays)
////        	panel.add(d);
//        f.add(scrollPane);
//        f.pack();
//        f.setVisible(true);
//	}
//	
//	public static void show(BufferedImage img){
//		show(new Display(img));
//	}
//	
//	public static void show(Mat... images){
//		Display[] ds = new Display[images.length];
//		for(int i=0; i<ds.length; ++i){
//			ds[i] = new Display(images[i]);
//		}
//		
//		show(ds);
//	}
	
	public Dimension getPreferredSize() {
        if (img == null) {
             return new Dimension(100,100);
        } else {
           return new Dimension(img.getWidth(null), img.getHeight(null));
       }
    }
	
	public void paint(Graphics g) {
        g.drawImage(img, 0, 0, null);
    }
}
