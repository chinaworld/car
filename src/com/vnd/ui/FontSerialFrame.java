package com.vnd.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.vnd.model.RefinedFontInfo;
import org.opencv.core.Mat;

public class FontSerialFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	public FontSerialFrame(){
		super("Font Serial");
		setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
	}
	
	void createUI(RefinedFontInfo[] processedImages) {
		JPanel panel = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints constrain = new GridBagConstraints();
		constrain.fill = GridBagConstraints.BOTH;
		panel.setLayout(layout);
		for(int i=0; i<processedImages.length; ++i){
			FontSerial fs = new FontSerial(processedImages[i].getResizeClipped().clone());
			Mat[] binaried = fs.binary();
			for(int j=0; j<binaried.length; ++j){
				Display d = new Display(binaried[j]);
				constrain.gridwidth = (j == binaried.length-1 ? GridBagConstraints.REMAINDER : 1);
				layout.setConstraints(d, constrain);
				panel.add(d);
			}
		}
		getContentPane().add(panel);
	}
	
	public void show(RefinedFontInfo[] processedImages) {
		getContentPane().removeAll();
		createUI(processedImages);

		pack();
		setVisible(true);
	}
}
