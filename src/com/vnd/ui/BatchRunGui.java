package com.vnd.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.vnd.model.Config;
import com.vnd.logic.Recognizer;
import org.opencv.core.Core;

public class BatchRunGui {
	static final int FIELDS = 5;
	static File[] files;
	static String DIR = "C:\\Users\\Administrator\\Desktop\\CarNum\\plates";
//    static String DIR = "C:\\Users\\Administrator\\Pictures\\id";

	
	static JFrame f = new JFrame("Image Display");
	static JScrollPane scrollPane = new JScrollPane();

	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		display(DIR);
		batchRecognize();
	}
	
	static JPanel[][] cells;
	
	static JPanel createDisplayTable(){
		Config.isDebug = false;
		cells = new JPanel[files.length][FIELDS];
		JPanel panel = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints constrain = new GridBagConstraints();
		constrain.fill = GridBagConstraints.BOTH;
//		layout.setHgap(0);
//		layout.setVgap(0);
		panel.setLayout(layout);
		for(int i=0; i<files.length; ++i){
			for(int j=0; j<FIELDS; ++j){
				JPanel cell = new JPanel();
				constrain.gridwidth = (j == FIELDS-1 ? GridBagConstraints.REMAINDER : 1);
				layout.setConstraints(cell, constrain);
				panel.add(cell);
				cells[i][j] = cell;
			}
		}
		for(int i=0; i<files.length; ++i){
			cells[i][0].add(new JLabel(files[i].getName()));
		}
		return panel;
	}
	
	static void batchRecognize(){
		for(int i=0; i<files.length; ++i){
			Date start = new Date();
			Recognizer recognizer;
			try {
				recognizer = new Recognizer(files[i].getAbsolutePath());
			} catch (IOException e1) {
				e1.printStackTrace();
				continue;
			}
//			char[] chars = Main.detect(files[i].getAbsolutePath());
			char[] chars = recognizer.detect();
			long time = new Date().getTime() - start.getTime();

			String recognized = new String(chars);
			JLabel lbl = new JLabel(recognized);
			if(!files[i].getName().startsWith(recognized)){
				lbl.setForeground(Color.red);
			}
			cells[i][1].add(lbl);
			cells[i][2].add(new JLabel(String.valueOf(time)));
			if(recognizer.mainModel.finalPlate != null){
				cells[i][3].add(new Display(recognizer.mainModel.finalPlate));
			}
			JButton btn = new JButton("Debug");
			cells[i][4].add(btn);
			final int index = i;
			btn.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					Config.isDebug = true;
					GUI.display(files, index);
				}
			});
			f.revalidate();
		}
	}
	
	public static void display(String DIR) {
		File dir = new File(DIR);
		files = dir.listFiles();
		Container container = f.getContentPane();

		scrollPane.setViewportView(createDisplayTable());
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		container.add(scrollPane);
		f.setExtendedState(f.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		f.pack();
		f.setVisible(true);
	}
}
