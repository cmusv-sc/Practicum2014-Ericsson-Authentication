package edu.cmu.ini.impli_auth.context_collector.face;

import android.graphics.*;
import android.util.*;

import org.opencv.android.*;
import org.opencv.core.*;

import java.io.*;

/**
 * Helper class to deal with face image preprocessing before sent to Authentication Server
 */

public class LBPHFaceExtractor {

	private static final String TAG = "ContextCollector::LBPHFaceExtractor";
	private String mPath;

	private static final int WIDTH = 128;
	private static final int HEIGHT = 128;

	public LBPHFaceExtractor(String path) {
		mPath = path;
		Log.d(TAG, "Start LBPHFaceExtractor");
	}

	/**
	 * Create bitmap format from OpenCV Mat object of testing image.
	 * Convert bitmap of training face images to certain scale and save them into internal
	 * storage as temporary file
	 *
	 * @param m OpenCV Mat object of testing image.
	 */
	public void saveMat(Mat m) {
		Bitmap bmp = Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);

		Utils.matToBitmap(m, bmp);
		bmp = Bitmap.createScaledBitmap(bmp, WIDTH, HEIGHT, false);

		FileOutputStream f;
		try {
			String fileName = String.format(mPath + "%d.jpg", System.currentTimeMillis());
			f = new FileOutputStream(fileName, true);
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, f);
			f.close();
		} catch (Exception e) {
			Log.e(TAG, e.getCause() + " " + e.getMessage());
			e.printStackTrace();
		}
	}
}
