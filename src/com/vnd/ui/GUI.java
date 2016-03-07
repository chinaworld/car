package com.vnd.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import com.vnd.logic.Recognizer;
import org.opencv.core.Core;
import org.opencv.core.Mat;

public class GUI {
	static File[] files;
	static int current = 0;
	static String DIR = "C:\\Users\\Administrator\\Desktop\\CarNum\\car";

	static JFrame f;
	static JScrollPane scrollPane = new JScrollPane();
	static JLabel lblResult = new JLabel();
	static boolean exitOnClose = false;
	static JLabel lblPosition = new JLabel();

	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		exitOnClose = true;
		display(DIR);
	}

	public static JPanel createDispayPanel(DisplayBlock... displays) {
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		JPanel panel = new JPanel(layout);

		for (int i = 0; i < displays.length; ++i) {
			c.gridx = i % 3;
			c.gridy = i / 3;
			panel.add(displays[i], c);
		}
		return panel;
	}

	public static JPanel createDispayPanel() {
		List<Entry<String, Mat>> images = recognizer.mainModel.getImages();
		DisplayBlock[] ds = new DisplayBlock[images.size()];
		int i = 0;
		for (Entry<String, Mat> e : images) {
			Display d = new Display(e.getValue());
			d.setMouseMotionListener(new MouseMotionAdapter() {
				public void mouseMoved(MouseEvent e) {
					Point p = e.getPoint();
					lblPosition.setText(p.x + ", " + p.y);
				}
			});
			ds[i++] = new DisplayBlock(e.getKey(), d);
		}

		return createDispayPanel(ds);
	}

	static Recognizer recognizer;
	private static void drawCurrent() {
		try {
			recognizer = new Recognizer(files[current].getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		char[] chars = recognizer.detect();
//		char[] chars = Main.detect(files[current].getAbsolutePath());
		lblResult.setText(new String(chars));
		JPanel panel = createDispayPanel();
		scrollPane.setViewportView(panel);
		panel.revalidate();
		scrollPane.revalidate();
		scrollPane.repaint();
		f.revalidate();
		f.repaint();
	}
	
	public static JPanel createContentPanel(final File[] files, int index){
		GUI.files = files;
		GUI.current = index;
		JPanel container = new JPanel();
		container.setLayout(new BorderLayout());

		JToolBar toolbar = new JToolBar();
		JButton btnPrevious = new JButton("Previous");
		toolbar.add(btnPrevious);
		JButton btnNext = new JButton("Next");
		toolbar.add(btnNext);
		
		JButton btnSerial = new JButton("Serial Fonts");
		toolbar.add(btnSerial);
		
		final JCheckBox chkFix = new JCheckBox("Debug Current Image", false);
		toolbar.add(chkFix);
		toolbar.add(lblResult);
//		lblPosition.setText("Position");
		toolbar.add(new JLabel("  Position: "));
		toolbar.add(lblPosition);
		container.add(toolbar, BorderLayout.NORTH);

		container.add(scrollPane);
		drawCurrent();
		
		btnSerial.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				FontSerialFrame fSerial = new FontSerialFrame();
				fSerial.show(recognizer.mainModel.processed);
			}
		});

		btnNext.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(!chkFix.isSelected()){
					current = (current + 1) % files.length;
				}
				drawCurrent();
			}
		});

		btnPrevious.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				--current;
				if (current < 0) {
					current = files.length - 1;
				}
				drawCurrent();
			}
		});
		return container;
	}
	
	public static void display(final File[] files, int index){
		f = new JFrame("Image Display");
		Container container = f.getContentPane();
		container.add(createContentPanel(files, index));

		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if(exitOnClose)
					System.exit(0);
			}
		});
		f.pack();
		f.setExtendedState(f.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		f.setVisible(true);
	}

	public static void display(String DIR) {
		File dir = new File(DIR);
		files = dir.listFiles();
		display(files, current);
	}
}
