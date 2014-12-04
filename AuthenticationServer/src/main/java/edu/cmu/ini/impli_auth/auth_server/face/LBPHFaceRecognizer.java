package edu.cmu.ini.impli_auth.auth_server.face;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Map;


import edu.cmu.ini.impli_auth.auth_server.util.Util;
import static org.bytedeco.javacpp.opencv_contrib.*;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.*;

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

		faceRecognizer = createLBPHFaceRecognizer(2, 8, 8, 8, 90);
		faceRecognizer.train(images, labels);
	}

	public FaceTestResult test(byte[] imageInByte, String fileName) {
		try {
			FileOutputStream outStream = null;
			File outFile = new File(fileName);
			outStream = new FileOutputStream(outFile);
			outStream.write(imageInByte); 
			outStream.flush(); 
			outStream.close();
			Mat testImage = imread(fileName, CV_LOAD_IMAGE_GRAYSCALE);
			
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
			double prob = Util.genProb(p[0]);
			if(prob < 20) {
				return new FaceTestResult(-1, 0);
			}
			else {
				return new FaceTestResult(n[0], prob);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return new FaceTestResult(-1, 0);
	}
}