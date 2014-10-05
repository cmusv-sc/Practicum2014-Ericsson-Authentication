package edu.cmu.ini.impli_auth.server;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.IntBuffer;
import java.sql.ResultSet;
import java.util.List;
import java.util.ArrayList;

import static org.bytedeco.javacpp.opencv_contrib.*;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.*;

/**
 * I couldn't find any tutorial on how to perform face recognition using OpenCV and Java,
 * so I decided to share a viable solution here. The solution is very inefficient in its
 * current form as the training model is built at each run, however it shows what's needed
 * to make it work.
 *
 * The class below takes two arguments: The path to the directory containing the training
 * faces and the path to the image you want to classify. Not that all images has to be of
 * the same size and that the faces already has to be cropped out of their original images
 * (Take a look here http://fivedots.coe.psu.ac.th/~ad/jg/nui07/index.html if you haven't
 * done the face detection yet).
 *
 * For the simplicity of this post, the class also requires that the training images have
 * filename format: <label>-rest_of_filename.png. For example:
 *
 * 1-jon_doe_1.png
 * 1-jon_doe_2.png
 * 2-jane_doe_1.png
 * 2-jane_doe_2.png
 * ...and so on.
 *
 * Source: http://pcbje.com/2012/12/doing-face-recognition-with-javacv/
 *
 * @author Petter Christian Bjelland
 * @author Samuel Audet
 */
public class AuthFaceRecognizer {
	
	FaceRecognizer faceRecognizer = null;
	File testImage;
	
	public AuthFaceRecognizer(File testImage) {
		this.testImage = testImage;
	}
	
	public void run() {
		train();
		test(this.testImage);
	}
	
	private void train() {

		class Picture {
			int label;
			File image;
			public Picture (int label, File image){
				this.label = label;
				this.image = image;
			}
		}
		
		sqlConnection dao = new sqlConnection();
		List<Picture> imageFilesList = null;
		try {
			ResultSet resultSet = dao.readDataBase(-1);
			imageFilesList = new ArrayList<Picture>();
			while(resultSet.next()) {
				imageFilesList.add(new Picture(Integer.parseInt(resultSet.getString("ID")), 
						new File(resultSet.getString("PICTURE"))));
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(imageFilesList == null || imageFilesList.isEmpty()) {
			System.out.println("there is no any images in database!");
			return;
		}
		
		
        MatVector images = new MatVector(imageFilesList.size());

        Mat labels = new Mat(imageFilesList.size(), 1, CV_32SC1);
        IntBuffer labelsBuf = labels.getIntBuffer();

        int counter = 0;

        for (Picture picture : imageFilesList) {
            Mat img = imread(picture.image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
            int label = picture.label;
            images.put(counter, img);
            labelsBuf.put(counter, label);
            counter++;
        }

        faceRecognizer = createFisherFaceRecognizer();
        // FaceRecognizer faceRecognizer = createEigenFaceRecognizer();
        // FaceRecognizer faceRecognizer = createLBPHFaceRecognizer()

        faceRecognizer.train(images, labels);
	}
	
	public void test(File testImage) {
		Mat image = imread(testImage.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
		if(faceRecognizer == null) {
			System.out.println("faceRecognizer hasn't been generated!");
			return;
		}
		int predictedLabel = faceRecognizer.predict(image);

        System.out.println("Predicted label: " + predictedLabel);
	}
}