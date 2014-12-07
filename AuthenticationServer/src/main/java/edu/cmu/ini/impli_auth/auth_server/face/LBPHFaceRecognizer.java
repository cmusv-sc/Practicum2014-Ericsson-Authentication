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

/**
 * For doing LBPH facial recognition training and testing
 */
public class LBPHFaceRecognizer {

	FaceRecognizer faceRecognizer = null;
	int width, height;

	/**
	 * initialize the image pixels for following algorithm's input
	 * 
	 * @param width
	 *            image width
	 * @param height
	 *            image height
	 */
	public LBPHFaceRecognizer(int width, int height) {
		this.width = width;
		this.height = height;
	}

	/**
	 * supervised training for all target images with their labels
	 */
	public void train() {

		List<Integer> userList = Util.getAllUserList();
		Map<File, Integer> userImageMap = Util.getUserImageMap(userList);

		MatVector images = new MatVector(userImageMap.size());

		// we can only use single channel image to do LBPH
		Mat labels = new Mat(userImageMap.size(), 1, CV_32SC1);
		IntBuffer labelsBuf = labels.getIntBuffer();
		if (labelsBuf == null) {
			System.out.println("labelsBuf is null!");
		}

		int counter = 0;

		for (File image : userImageMap.keySet()) {
			// convert color images into grayscale
			Mat img = imread(image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);

			images.put(counter, img);

			labelsBuf.put(counter, userImageMap.get(image));

			counter++;
		}

		faceRecognizer = createLBPHFaceRecognizer(2, 8, 8, 8, 90);
		faceRecognizer.train(images, labels);
	}

	/**
	 * LBPH recognition
	 * 
	 * @param imageInByte
	 *            byte array of test image
	 * @param fileName
	 *            to specify for testing which temporary image file (for
	 *            multiple user scenario)
	 * @return test result
	 */
	public FaceTestResult test(byte[] imageInByte, String fileName) {
		try {
			// save test image as temporary file for following Mat object
			// create.
			FileOutputStream outStream = null;
			File outFile = new File(fileName);
			outStream = new FileOutputStream(outFile);
			outStream.write(imageInByte);
			outStream.flush();
			outStream.close();

			// convert color images into grayscale
			Mat testImage = imread(fileName, CV_LOAD_IMAGE_GRAYSCALE);

			int n[] = new int[1]; // predicted label
			double p[] = new double[1]; // distance from predicted label
			faceRecognizer.predict(testImage, n, p);
			System.out.println("n : " + n[0]);
			System.out.println("c : " + p[0]);
			if (n[0] != -1) { // predicted label is unknown
				System.out.println(n[0]);

			} else {
				System.out.println("Unkown");
				return new FaceTestResult(-1, 0);
			}
			double prob = Util.genProb(p[0]);
			if (prob < 20) { // probability is too low, set user as unknown one
				return new FaceTestResult(-1, 0);
			} else {
				return new FaceTestResult(n[0], prob);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return new FaceTestResult(-1, 0);
	}
}