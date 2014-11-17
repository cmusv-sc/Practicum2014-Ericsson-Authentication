package edu.cmu.ini.impli_auth.face;

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

import edu.cmu.ini.impli_auth.server.Labels;
import edu.cmu.ini.impli_auth.server.Util;
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
		
		System.out.println(userImageMap.size());
		MatVector images = new MatVector(userImageMap.size());

		Mat labels = new Mat(userImageMap.size(), 1, CV_32SC1);
		IntBuffer labelsBuf = labels.getIntBuffer();
		if (labelsBuf == null) {
			System.out.println("labelsBuf is null!");
		}

		int counter = 0;

		//Labels labelsFile = new Labels("");
		//labelsFile.Read();

		for (File image : userImageMap.keySet()) {
			String p = image.getName();
			System.out.println(p);

			Mat img = imread(image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);

			//int i1 = p.lastIndexOf("-");

			//String description = p.substring(0, i1);

			/*if (labelsFile.get(description) < 0)
				labelsFile.add(description, labelsFile.max() + 1);*/

			//int label = labelsFile.get(description);

			images.put(counter, img);

			labelsBuf.put(counter, userImageMap.get(image));

			counter++;
		}

		// faceRecognizer = createFisherFaceRecognizer();
		// faceRecognizer = createEigenFaceRecognizer();
		faceRecognizer = createLBPHFaceRecognizer();
		faceRecognizer.train(images, labels);
	}

	public void test(byte[] imageInByte) {
		Mat mat = new Mat(width, height, CV_8UC1);
		System.out.println("mat has been created!");

		InputStream in = new ByteArrayInputStream(imageInByte);
		try {
			BufferedImage bImageFromConvert = ImageIO.read(in);
			
			IplImage image = IplImage.create(width, height, IPL_DEPTH_8U, 4);
			image.copyFrom(bImageFromConvert);
			IplImage grayImg = IplImage.create(image.width(), image.height(),
					IPL_DEPTH_8U, 1);
			cvCvtColor(image, grayImg, CV_BGR2GRAY);

			mat.copyFrom(grayImg.getBufferedImage());
			
			if (faceRecognizer == null) {
				System.out.println("faceRecognizer hasn't been generated!");
				return;
			}

			int n[] = new int[1];
			double p[] = new double[1];
			faceRecognizer.predict(mat, n, p);
			System.out.println("n : " + n[0]);
			if (n[0] != -1) {
				Labels labelsFile = new Labels("");
				labelsFile.Read();
				System.out.println(labelsFile.get(n[0]));
			} else {
				System.out.println("Unkown");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}