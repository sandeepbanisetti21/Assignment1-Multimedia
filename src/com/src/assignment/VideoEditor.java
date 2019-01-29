package com.src.assignment;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
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
		args = new String[] { "prison_960_540.rgb", "1", "2", "10", "0", "1" };
		// args = new String[] { "aliasing_960_540.rgb", "1", "1", "10", "0", "0" };
		VideoEditor videoEditor = new VideoEditor();
		videoEditor.readArguments(args);
		videoEditor.loadVideo();
		if (option == 1) {
			videoEditor.reAdjustAspectRatio();
		} else {
			videoEditor.resize();
		}
		videoEditor.playVideo();
	}

	private void loadVideo() {
		System.out.println("Loading input video");
		File file = new File(videoFilePath);
		try {
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			raf.seek(0);
			long len = file.length();
			long noOfFrames = len / (width * height * 3);
			byte[] bytes = new byte[(int) len];
			raf.read(bytes);
			int start = 0;
			int frameNo = 0;
			while (frameNo < noOfFrames) {
				BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						byte r = bytes[start];
						byte g = bytes[start + height * width];
						byte b = bytes[start + height * width * 2];
						start++;
						int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
						img.setRGB(x, y, pix);
					}
				}
				start += height * width * 2;
				videoList.add(img);
				frameNo++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void reAdjustAspectRatio() {

		int nonLinearWidth = (int) (width * scaling_width);
		int nonLinearHeight = (int) (height * scaling_height);

		if (scaling_height == scaling_width) {
			resize();
		} else {
			if (scaling_width > scaling_height) {
				// adjustWidth
				double centralAspectRatio = ((double) (0.6 * width)) / (double) height;
				int centralWidth = (int) (nonLinearHeight * centralAspectRatio);
				int peripheralWidth = (nonLinearWidth - centralWidth) / 2;

				System.out.println("Non linear width: " + nonLinearWidth + "non linear height:" + nonLinearHeight
						+ "central aspect ratio:" + centralAspectRatio + "central width:" + centralWidth
						+ "peripheral width:" + peripheralWidth);

				for (int i = 0; i < videoList.size(); i++) {
					BufferedImage oldImage = videoList.get(i);
					BufferedImage newImage = new BufferedImage(nonLinearWidth, nonLinearHeight,
							BufferedImage.TYPE_INT_RGB);
					for (int y = 0; y < nonLinearHeight; y++) {
						newImage = nonLinearWidthResize(0, peripheralWidth, (double) (peripheralWidth / (0.2 * width)),
								scaling_height, oldImage, newImage, y);
						BufferedImage center = oldImage.getSubimage((int) (0.2 * width), 0,
								(int) (0.6 * (double) width), height);
						newImage = nonLinearWidthResize(peripheralWidth, centralWidth,
								(double) (centralWidth / (0.6 * width)), scaling_height, center, newImage, y);
						BufferedImage right = oldImage.getSubimage((int) (0.8 * width), 0, (int) (0.2 * width), height);
						newImage = nonLinearWidthResize(peripheralWidth + centralWidth, peripheralWidth,
								(double) (peripheralWidth / (0.2 * width)), scaling_height, right, newImage, y);
					}
					newvideoList.add(newImage);
				}
			} else {
				// adjustHeight
				System.out.println("adjusting height");
				double centralAspectRatio = ((double) width) / (double) (0.6 * height);
				int centralHeight = (int) ((double) nonLinearWidth / (double) centralAspectRatio);
				int peripheralHeight = (nonLinearHeight - centralHeight) / 2;

				System.out.println("Non linear width: " + nonLinearWidth + "non linear height:" + nonLinearHeight
						+ "central aspect ratio:" + centralAspectRatio + "central width:" + centralHeight
						+ "peripheral width:" + peripheralHeight);

				for (int i = 0; i < videoList.size(); i++) {
					BufferedImage oldImage = videoList.get(i);
					BufferedImage newImage = new BufferedImage(nonLinearWidth, nonLinearHeight,
							BufferedImage.TYPE_INT_RGB);
					for (int x = 0; x < nonLinearWidth; x++) {
						newImage = nonLinearHeightResize(0, peripheralHeight,
								(double) (peripheralHeight / (0.2 * height)), scaling_width, oldImage, newImage, x);
						BufferedImage center = oldImage.getSubimage(0, (int) (0.2 * height), width,
								(int) (0.6 * height));
						newImage = nonLinearHeightResize(peripheralHeight, centralHeight,
								(double) (centralHeight / (0.6 * height)), scaling_width, center, newImage, x);
						BufferedImage bottom = oldImage.getSubimage(0, (int) (0.8 * height), width,
								(int) (0.2 * height));
						newImage = nonLinearHeightResize(peripheralHeight + centralHeight, peripheralHeight,
								(double) (peripheralHeight / (0.2 * height)), scaling_width, bottom, newImage, x);
					}
					newvideoList.add(newImage);
				}

			}
		}
	}

	private BufferedImage nonLinearWidthResize(int xLowerBound, int length, double xScalingFactor, float yScalingFactor,
			BufferedImage Oldimage, BufferedImage newImage, int yCoordinate) {

		// System.out.println("xLowerBound:" + xLowerBound + "length:" + length +
		// "xScalingFactor:" + xScalingFactor
		// + "yScalingFactor:" + yScalingFactor + "yCoordinate:" + yCoordinate);
		for (int x = 0; x < length; x++) {
			int actualX = (int) ((double) x / xScalingFactor);
			int actualY = (int) ((double) yCoordinate / yScalingFactor);
			if (actualX >= Oldimage.getWidth())
				continue;
			int rgb = Oldimage.getRGB(actualX, actualY);
			if (anti_alisasing) {
				rgb = avgRGB(actualX, actualY, Oldimage);
			}
			if (x + xLowerBound >= newImage.getWidth() || yCoordinate >= newImage.getHeight())
				continue;
			newImage.setRGB(x + xLowerBound, yCoordinate, rgb);
		}
		return newImage;
	}

	private BufferedImage nonLinearHeightResize(int yLowerBound, int length, double yScalingFactor,
			float xScalingFactor, BufferedImage Oldimage, BufferedImage newImage, int xCoordinate) {

		// System.out.println("xLowerBound:" + xLowerBound + "length:" + length +
		// "xScalingFactor:" + xScalingFactor
		// + "yScalingFactor:" + yScalingFactor + "yCoordinate:" + yCoordinate);
		for (int y = 0; y < length; y++) {
			int actualX = (int) ((double) xCoordinate / xScalingFactor);
			int actualY = (int) ((double) y / yScalingFactor);
			if (actualY >= Oldimage.getHeight())
				continue;
			int rgb = Oldimage.getRGB(actualX, actualY);
			if (anti_alisasing) {
				rgb = avgRGB(actualX, actualY, Oldimage);
			}
			if (y + yLowerBound >= newImage.getHeight() || xCoordinate >= newImage.getWidth())
				continue;
			newImage.setRGB(xCoordinate, y + yLowerBound, rgb);
		}
		return newImage;
	}

	public void resize() {
		for (int i = 0; i < videoList.size(); i++) {
			BufferedImage img = videoList.get(i);
			int resizedWidth = (int) (img.getWidth() * scaling_width);
			int resizedHeight = (int) (img.getHeight() * scaling_height);
			BufferedImage new_img = new BufferedImage(resizedWidth, resizedHeight, BufferedImage.TYPE_INT_RGB);
			for (int y = 0; y < resizedHeight; y++) {
				for (int x = 0; x < resizedWidth; x++) {
					int actualX = (int) ((float) x / scaling_width);
					int actualY = (int) ((float) y / scaling_height);
					int rgb = img.getRGB(actualX, actualY);
					if (anti_alisasing) {
						rgb = avgRGB(actualX, actualY, img);
					}
					new_img.setRGB(x, y, rgb);
				}
			}
			newvideoList.add(new_img); // Add to output
		}
	}

	public int avgRGB(int x, int y, BufferedImage image) {
		int height = image.getHeight();
		int width = image.getWidth();

		int red = 0;
		int green = 0;
		int blue = 0;
		int count = 0;
		for (int i = -1; i < 2; i++) {
			for (int j = -1; j < 2; j++) {
				if (x + i < width && y + j < height && x + i >= 0 && y + j >= 0) {
					int clr = image.getRGB(x + i, y + j);
					red += (clr & 0x00ff0000) >> 16;
					green += (clr & 0x0000ff00) >> 8;
					blue += clr & 0x000000ff;
					count++;
				}
			}
		}
		return ((0 << 24) + ((red / count) << 16) + ((green / count) << 8) + (blue / count));
	}

	private void playVideo() {
		JFrame frame = new JFrame();
		JLabel label = new JLabel(new ImageIcon(newvideoList.get(0)));
		frame.getContentPane().add(label, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);

		for (int i = 1; i < newvideoList.size(); i++) {
			label.setIcon(new ImageIcon(newvideoList.get(i)));
			try {
				Thread.sleep(1000 / frame_rate); // 10000/30 should be default
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.exit(0);
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
