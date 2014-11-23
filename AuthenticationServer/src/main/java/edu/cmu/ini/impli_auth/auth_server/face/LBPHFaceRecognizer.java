package edu.cmu.ini.impli_auth.auth_server.face;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.sql.ResultSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import javax.imageio.ImageIO;

import org.bytedeco.javacpp.BytePointer;

import edu.cmu.ini.impli_auth.auth_server.util.Util;
import static org.bytedeco.javacpp.opencv_contrib.*;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

public class LBPHFaceRecognizer {

	FaceRecognizer faceRecognizer = null;
	int width, height;

	public LBPHFaceRecognizer(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public void train() {
		
		List<Integer> userList = Util.getAllUserList();
		Map<File, Integer> userImageMap = Util.getUserImageMap(userList);
		
		MatVector images = new MatVector(userImageMap.size());

		Mat labels = new Mat(userImageMap.size(), 1, CV_32SC1);
		IntBuffer labelsBuf = labels.getIntBuffer();
		if (labelsBuf == null) {
			System.out.println("labelsBuf is null!");
		}

		int counter = 0;

		for (File image : userImageMap.keySet()) {
			Mat img = imread(image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);

			images.put(counter, img);

			labelsBuf.put(counter, userImageMap.get(image));

			counter++;
		}

		// faceRecognizer = createFisherFaceRecognizer();
		// faceRecognizer = createEigenFaceRecognizer();
		faceRecognizer = createLBPHFaceRecognizer(2, 8, 8, 8, 90);
		// faceRecognizer = createFisherFaceRecognizer(0, 1500);
		faceRecognizer.train(images, labels);
	}

	public FaceTestResult test(byte[] imageInByte) {
		Mat mat = new Mat(width, height, CV_8UC1);
		System.out.println("mat has been created!");

		InputStream in = new ByteArrayInputStream(imageInByte);
		try {
			FileOutputStream outStream = null;
			File outFile = new File("test.jpg");
			outStream = new FileOutputStream(outFile);
			outStream.write(imageInByte); 
			outStream.flush(); 
			outStream.close();
			Mat testImage = imread("test.jpg", CV_LOAD_IMAGE_GRAYSCALE);
			/*
			BufferedImage bImageFromConvert = ImageIO.read(in);
			
			IplImage image = IplImage.create(width, height, IPL_DEPTH_8U, 4);
			image.copyFrom(bImageFromConvert);
			cvSaveImage("test1.jpg",image);
			IplImage grayImg = IplImage.create(image.width(), image.height(),
					IPL_DEPTH_8U, 1);
			cvCvtColor(image, grayImg, CV_BGR2GRAY);

			mat.copyFrom(grayImg.getBufferedImage());
			cvSaveImage("test2.jpg",grayImg);
			if (faceRecognizer == null) {
				System.out.println("faceRecognizer hasn't been generated!");
				return new FaceTestResult(-1, 0);
			}
			*/
			
			int n[] = new int[1];
			double p[] = new double[1];
			faceRecognizer.predict(testImage, n, p);
			System.out.println("n : " + n[0]);
			System.out.println("c : " + p[0]);
			if (n[0] != -1) {
				System.out.println(n[0]);
				
			} else {
				System.out.println("Unkown");
				return new FaceTestResult(-1, 0);
			}
			return new FaceTestResult(n[0], Util.genProb(p[0]));

		} catch (IOException e) {
			e.printStackTrace();
		}
		return new FaceTestResult(-1, 0);
	}
}