package edu.cmu.ini.impli_auth.auth_client.face;

import java.io.*;
import java.util.*;

import android.graphics.*;
import android.util.*;

public class LBPHFaceExtractor {

	private static final String TAG = "AuthenticationClient::LBPHFaceExtractor";

	private static final int WIDTH = 128;
	private static final int HEIGHT = 128;

	LBPHFaceExtractor() {
		Log.d(TAG, "Start LBPHFaceExtractor");
	}

	public List<byte[]> constructTestImages(List<Bitmap> mBitmaps) {
		List<byte[]> byteArrayList = new ArrayList<byte[]>();
		for(Bitmap mBitmap : mBitmaps) {
			Bitmap bmp = Bitmap.createScaledBitmap(mBitmap, WIDTH, HEIGHT, false);
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
				byteArrayList.add(baos.toByteArray());
			} catch (Exception e) {
				Log.e(TAG, e.getCause() + " " + e.getMessage());
				e.printStackTrace();
			}
		}
		return byteArrayList;
	}
}
