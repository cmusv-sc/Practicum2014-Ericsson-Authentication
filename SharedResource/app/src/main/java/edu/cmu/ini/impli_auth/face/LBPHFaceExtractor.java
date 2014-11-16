package edu.cmu.ini.impli_auth.face;

import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_core.*;

import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_contrib.*;

import java.io.*;
import java.util.ArrayList;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.entity.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.message.*;
import org.apache.http.util.*;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import com.googlecode.javacv.cpp.opencv_imgproc;
import com.googlecode.javacv.cpp.opencv_contrib.FaceRecognizer;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_core.MatVector;

import android.graphics.*;
import android.os.*;
import android.util.*;

public class LBPHFaceExtractor {

	private static final String TAG = "SharedResource::PersonRecognizer";
	public final static int MAXIMG = 100;
	FaceRecognizer faceRecognizer;
	String mPath;
	int count = 0;

	static final int WIDTH = 128;
	static final int HEIGHT = 128;
	private int mProb = 999;

	static final double confidenceThx = 80;

	LBPHFaceExtractor(String path) {
		faceRecognizer = com.googlecode.javacv.cpp.opencv_contrib.createLBPHFaceRecognizer(2, 8,
				8, 8, confidenceThx);
		mPath = path;
	}

	void changeRecognizer(int nRec) {
		switch (nRec) {
			case 0:
				faceRecognizer = com.googlecode.javacv.cpp.opencv_contrib.createLBPHFaceRecognizer(1, 8, 8, 8, 100);
				break;
			case 1:
				faceRecognizer = com.googlecode.javacv.cpp.opencv_contrib.createFisherFaceRecognizer();
				break;
			case 2:
				faceRecognizer = com.googlecode.javacv.cpp.opencv_contrib.createEigenFaceRecognizer();
				break;
		}
		train();
	}

	void add(Mat m, String description) {
		Bitmap bmp = Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);

		Utils.matToBitmap(m, bmp);
		bmp = Bitmap.createScaledBitmap(bmp, WIDTH, HEIGHT, false);

		FileOutputStream f;
		try {
			f = new FileOutputStream(mPath + description + "-" + count + ".jpg", true);
			count++;
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, f);
			f.close();
		} catch (Exception e) {
			Log.e("error", e.getCause() + " " + e.getMessage());
			e.printStackTrace();
		}
	}

	public boolean train() {
		File root = new File(mPath);

		FilenameFilter pngFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".jpg");

			}
		};

		File[] imgFiles = root.listFiles(pngFilter);

		for (File imgFile : imgFiles) {
			if(imgFile.exists()) {
				Bitmap bmp = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
				new SendHttpRequestTask().execute(baos.toByteArray());
			}
		}

		return true;
	}


	public String predict(Bitmap mBitmap) {

		Bitmap bmp = Bitmap.createScaledBitmap(mBitmap, WIDTH, HEIGHT, false);

		FileOutputStream f;
		try {
			/*
			String fileName = String.format(mPath + "%d.jpg", System.currentTimeMillis());
			f = new FileOutputStream(fileName, true);
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, f);
			f.close();
			*/

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);

			String image_str = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

			nameValuePairs.add(new BasicNameValuePair("width", String.valueOf(WIDTH)));
			nameValuePairs.add(new BasicNameValuePair("height", String.valueOf(HEIGHT)));
			nameValuePairs.add(new BasicNameValuePair("image",image_str));

			String url = "http://10.0.23.8:8080/CentralServer/json/testImage/";

			Log.d(TAG, "start to post image!");
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(url);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				Log.d(TAG, "start to post image1!");
				HttpResponse response = httpclient.execute(httppost);
				Log.d(TAG, "start to post image2!");
				HttpEntity entity = response.getEntity();

				Log.d(TAG, "start to post image3!");
				//
				// Read the contents of an entity and return it as a String.
				//
				final String content = EntityUtils.toString(entity);

			}
			catch(Throwable t) {
				t.printStackTrace();
				Log.d(TAG, "post image exception!");
			}

		} catch (Exception e) {
			Log.e(TAG, e.getCause() + " " + e.getMessage());
			e.printStackTrace();
		}

		return "not yet!";
	}

	protected void SaveBmp(Bitmap bmp, String path) {
		FileOutputStream file;
		try {
			file = new FileOutputStream(path, true);
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, file);
			file.close();
		} catch (Exception e) {
			Log.e("", e.getMessage() + e.getCause());
			e.printStackTrace();
		}

	}

	public void load() {
		train();
	}

	public int getProb() {
		return mProb;
	}

	private class SendHttpRequestTask extends AsyncTask<byte[], Void, Void> {

		@Override
		protected Void doInBackground(byte[]... data) {
			String image_str = Base64.encodeToString(data[0], Base64.DEFAULT);
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

			nameValuePairs.add(new BasicNameValuePair("image",image_str));

			String url = "http://10.0.0.4:8080/CentralServer/json/testImage/";

			Log.d(TAG, "start to post image!");
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(url);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				Log.d(TAG, "start to post image1!");
				HttpResponse response = httpclient.execute(httppost);
				Log.d(TAG, "start to post image2!");
				HttpEntity entity = response.getEntity();

				Log.d(TAG, "start to post image3!");
				//
				// Read the contents of an entity and return it as a String.
				//
				final String content = EntityUtils.toString(entity);

			}
			catch(Throwable t) {
				t.printStackTrace();
				Log.d(TAG, "post image exception!");
			}

			return null;
		}
	}

}
