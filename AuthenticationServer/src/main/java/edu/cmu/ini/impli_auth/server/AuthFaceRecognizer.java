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


/*
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_core.*;

import static com.googlecode.javacv.cpp.opencv_imgproc.*;

import com.googlecode.javacv.cpp.opencv_imgproc;
import com.googlecode.javacv.cpp.opencv_contrib.FaceRecognizer;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_core.MatVector;
import org.opencv.core.Mat;
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
		/*
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
		*/
		
		  File root = new File("./");

	        FilenameFilter imgFilter = new FilenameFilter() {
	            public boolean accept(File dir, String name) {
	                name = name.toLowerCase();
	                return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
	            }
	        };

	        File[] imageFiles = root.listFiles(imgFilter);
	        System.out.println(imageFiles.length);
	        MatVector images = new MatVector(imageFiles.length);

	        Mat labels = new Mat(imageFiles.length, 1, CV_8UC1);
	        IntBuffer labelsBuf = labels.getIntBuffer();
	        if(labelsBuf == null) {
	        	System.out.println("labelsBuf is null!");
	        }
	        
	        int counter = 0;
	        
	        Labels labelsFile = new Labels("");
	        labelsFile.Read();

	        for (File image : imageFiles) {
	        	String p = image.getName();
	        	System.out.println(p);
	        	
	            Mat img = imread(image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
	            
	            int i1 = p.lastIndexOf("-");
	            
	            String description = p.substring(0, i1);
	            
	            if (labelsFile.get(description) < 0)
					labelsFile.add(description, labelsFile.max() + 1);

				int label = labelsFile.get(description);

	            images.put(counter, img);

	            labelsBuf.put(counter, label);

	            counter++;
	        }

		
		
        // faceRecognizer = createFisherFaceRecognizer();
        // FaceRecognizer faceRecognizer = createEigenFaceRecognizer();
        faceRecognizer = createLBPHFaceRecognizer();

        faceRecognizer.train(images, labels);
	}
	
	public void test(File testImage) {
	/*	Mat image = imread(testImage.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
		if(faceRecognizer == null) {
			System.out.println("faceRecognizer hasn't been generated!");
			return;
		}
		int predictedLabel = faceRecognizer.predict(image);

        System.out.println("Predicted label: " + predictedLabel); */
	}
}