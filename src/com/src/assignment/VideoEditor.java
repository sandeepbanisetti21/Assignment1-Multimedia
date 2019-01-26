package com.src.assignment;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class VideoEditor {

	public final static int width = 960;
	public final static int height = 540;

	public static String videoFilePath = null;
	public static float scaling_width = 0.0f;
	public static float scaling_height = 0.0f;
	public static int frame_rate = 0;
	public static boolean anti_alisasing = false;
	public static int option = 0;

	List<BufferedImage> videoList = new ArrayList<>();
	List<BufferedImage> newvideoList = new ArrayList<>();

	public static void main(String[] args) {
		System.out.println("Length of arguments are " + args.length);

		if (args.length < 6) {
			System.out.println("It should be video_path scaling_width scaling_height frame_rate anti_alisasing option");
		}
		VideoEditor videoEditor = new VideoEditor();
		videoEditor.readArguments(args);
		videoEditor.loadVideo();
		videoEditor.resize();
		videoEditor.playVideo();

	}

	private void loadVideo() {
		System.out.println("Loading input video");
		try {
			File file = new File(videoFilePath);
			InputStream inputStream = new FileInputStream(file);
			long len = file.length();
			byte[] bytes = new byte[(int) len];
			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length && (numRead = inputStream.read(bytes, offset, bytes.length - offset)) >= 0) {
				offset += numRead;
			}
			int start = 0;
			while (start + height * width * 2 < len) {
				BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						byte a = 0;
						byte r = bytes[start];
						byte g = bytes[start + height * width];
						byte b = bytes[start + height * width * 2];
						start++;
						int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
						img.setRGB(x, y, pix);
					}
				}
				start += height * width * 2; // Skip to the next frame
				videoList.add(img);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void playVideo() {
		JFrame frame = new JFrame();
		JLabel label = new JLabel(new ImageIcon(videoList.get(0)));
		frame.getContentPane().add(label, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);

		System.out.println(videoList.size());
		for (int i = 1; i < videoList.size(); i++) {
			label.setIcon(new ImageIcon(videoList.get(i)));
			try {
				System.out.println("sleeping");
				Thread.sleep(1000 / frame_rate); // 10000/30 should be default
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void resize() {
		for (int i = 0; i < videoList.size(); i++) {
			BufferedImage img = videoList.get(i); // Original frame
			int new_width = (int) (img.getWidth() * scaling_width);
			int new_height = (int) (img.getHeight() * scaling_height);
			BufferedImage new_img = new BufferedImage(new_width, new_height, BufferedImage.TYPE_INT_RGB); // New frame

			for (int y = 0; y < new_img.getHeight(); y++) {
				for (int x = 0; x < new_img.getWidth(); x++) {
					int x_orig = (int) ((double) x / scaling_width);
					int y_orig = (int) ((double) y / scaling_height);

					int rgb = img.getRGB(x_orig, y_orig);
					if (anti_alisasing) {
						rgb = avgRGB(x_orig, y_orig, img);
					}
					new_img.setRGB(x, y, rgb);
				}
			}
			newvideoList.add(new_img); // Add to output
		}
	}

	public int avgRGB(int x, int y, BufferedImage image) {
		

		return 0;
	}

	private void readArguments(String[] args) {
		System.out.println("Reading arguments");
		videoFilePath = args[0];
		scaling_width = Float.parseFloat(args[1]);
		scaling_height = Float.parseFloat(args[2]);
		frame_rate = Integer.parseInt(args[3]);
		anti_alisasing = Integer.parseInt(args[4]) == 1 ? true : false;
		option = Integer.parseInt(args[5]);

	}

}
