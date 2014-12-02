package edu.cmu.ini.impli_auth.auth_client.face;


import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.preference.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.entity.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.message.*;
import org.apache.http.util.*;
import org.opencv.android.*;
import org.opencv.android.CameraBridgeViewBase.*;
import org.opencv.core.*;
import org.opencv.core.Rect;
import org.opencv.objdetect.*;

import java.io.*;
import java.util.*;

import edu.cmu.ini.impli_auth.auth_client.*;
import edu.cmu.ini.impli_auth.auth_client.util.*;

public class FaceActivity extends Activity implements CvCameraViewListener2 {

	private static final String TAG = "SharedResource::FaceActivity";
	private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
	public static final int JAVA_DETECTOR = 0;
	public static final int NATIVE_DETECTOR = 1;

	public static final int TRAINING = 0;
	public static final int SEARCHING = 1;
	public static final int IDLE = 2;

	private static final int frontCam = 1;
	private static final int backCam = 2;


	private int faceState = IDLE;
	private MenuItem nBackCam;
	private MenuItem mFrontCam;
	private MenuItem mEigen;


	private Mat mRgba;
	private Mat mGray;
	private File mCascadeFile;
	private CascadeClassifier mJavaDetector;
	//   private DetectionBasedTracker  mNativeDetector;

	private int mDetectorType = JAVA_DETECTOR;
	private String[] mDetectorName;

	private float mRelativeFaceSize = 0.2f;
	private int mAbsoluteFaceSize = 0;
	private int mLikely = 999;

	String mPath = "";

	private CameraView mOpenCvCameraView;
	private int mChooseCamera = backCam;

	Handler mHandler;

	LBPHFaceExtractor fe;
	TextView textResult;
	ToggleButton searchButton;
	Button buttonCatalog;
	ImageButton imCamera;

	com.googlecode.javacv.cpp.opencv_contrib.FaceRecognizer faceRecognizer;


	static final long MAXIMG = 1;
	static final long MAXIMG_SEARCH = 1;

	ArrayList<Mat> alimgs = new ArrayList<Mat>();

	int[] labels = new int[(int) MAXIMG];
	int countImages = 0;
	int countSearch = 0;

	Map<String, Integer> map = new HashMap<String, Integer>();
	private GlobalVariable gv;
	public SendAuthTask sendAuthTask;
	static final int WIDTH = 128;
	static final int HEIGHT = 128;

	private SharedPreferences sharedPref;

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
				case LoaderCallbackInterface.SUCCESS: {
					Log.i(TAG, "OpenCV loaded successfully");

					// Load native library after(!) OpenCV initialization
					//   System.loadLibrary("detection_based_tracker");

					fe = new LBPHFaceExtractor(mPath);
					// String s = getResources().getString(R.string.Straininig);
					// Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();

					try {
						// load cascade file from application resources
						InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
						File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
						mCascadeFile = new File(cascadeDir, "lbpcascade.xml");
						FileOutputStream os = new FileOutputStream(mCascadeFile);

						byte[] buffer = new byte[4096];
						int bytesRead;
						while ((bytesRead = is.read(buffer)) != -1) {
							os.write(buffer, 0, bytesRead);
						}
						is.close();
						os.close();

						mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
						if (mJavaDetector.empty()) {
							Log.e(TAG, "Failed to load cascade classifier");
							mJavaDetector = null;
						} else
							Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

						//                 mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);

						cascadeDir.delete();
					} catch (IOException e) {
						e.printStackTrace();
						Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
					}
					mOpenCvCameraView.enableView();
					break;
				}
				default: {
					super.onManagerConnected(status);
					break;
				}
			}
		}
	};

	public FaceActivity() {
		mDetectorName = new String[2];
		mDetectorName[JAVA_DETECTOR] = "Java";
		mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";
		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.face_detect_surface_view);

		mOpenCvCameraView = (CameraView) findViewById(R.id.cameraView);

		mOpenCvCameraView.setCvCameraViewListener(this);


		File dir = new File(getFilesDir() + "/camtest");
		dir.mkdirs();
		mPath = getFilesDir() + "/camtest/";

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				/*
				if (msg.obj == "IMG") {
					Canvas canvas = new Canvas();
					canvas.setBitmap(mBitmap);
				} else {
					textResult.setText(msg.obj.toString());
				}
				*/
			}
		};
		textResult = (TextView) findViewById(R.id.resultTextView);
		searchButton = (ToggleButton) findViewById(R.id.searchButton);
		imCamera = (ImageButton) findViewById(R.id.cameraImageButton);

/*
		buttonCatalog.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent i = new Intent(CamActivity.this,
						ImageGallery.class);
				i.putExtra("path", mPath);
				startActivity(i);
			}
		});
*/

		imCamera.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				if (mChooseCamera == frontCam) {
					mChooseCamera = backCam;
					mOpenCvCameraView.setCamBack();
				} else {
					mChooseCamera = frontCam;
					mOpenCvCameraView.setCamFront();

				}
			}
		});

		searchButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				if (searchButton.isChecked()) {
					textResult.setText(getResources().getString(R.string.SSearching));
					countSearch = 0;
					map = new HashMap<String, Integer>();
					faceState = SEARCHING;
				} else {
					faceState = IDLE;
					textResult.setText(getResources().getString(R.string.SIdle));
				}
			}
		});

		boolean success = (new File(mPath)).mkdirs();
		if (!success) {
			Log.e("Error", "Error creating directory");
		}
		gv = GlobalVariable.getInstance();
	}


	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
	}

	public void onDestroy() {
		super.onDestroy();
		mOpenCvCameraView.disableView();
	}

	public void onCameraViewStarted(int width, int height) {
		mGray = new Mat();
		mRgba = new Mat();
	}

	public void onCameraViewStopped() {
		mGray.release();
		mRgba.release();
	}

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

		mRgba = inputFrame.rgba();
		mGray = inputFrame.gray();

		if (mAbsoluteFaceSize == 0) {
			int height = mGray.rows();
			if (Math.round(height * mRelativeFaceSize) > 0) {
				mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
			}
			//  mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
		}

		MatOfRect faces = new MatOfRect();

		if (mDetectorType == JAVA_DETECTOR) {
			if (mJavaDetector != null)
				mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
						new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
		} else if (mDetectorType == NATIVE_DETECTOR) {
//            if (mNativeDetector != null)
//                mNativeDetector.detect(mGray, faces);
		} else {
			Log.e(TAG, "Detection method is not selected!");
		}

		Rect[] facesArray = faces.toArray();
		Log.d(TAG, "Face number: " + facesArray.length);

		if ((facesArray.length > 0) && (faceState == SEARCHING)) {
			/*
			Message msg = new Message();
			String textTochange = "IMG";
			msg.obj = textTochange;
			mHandler.sendMessage(msg);
			*/
			if(countSearch < MAXIMG_SEARCH) {
				List<Bitmap> bitmaps = new ArrayList<Bitmap>();
				for(Rect r : facesArray) {
					Mat m = new Mat();
					m = mGray.submat(r);
					Bitmap mBitmap = Bitmap.createBitmap(m.width(), m.height(),
							Bitmap.Config.ARGB_8888);
					Utils.matToBitmap(m, mBitmap);
					bitmaps.add(mBitmap);
				}
				predict(bitmaps);
				countSearch++;
			}

			/*
			if(countSearch < MAXIMG_SEARCH) {
				textTochange = fr.predict(m);
				mLikely = fr.getProb();
				if(map.containsKey(textTochange)) {
					map.put(textTochange, map.get(textTochange) + 1);
				}
				else {
					map.put(textTochange, 1);
				}
				countSearch++;
				msg = new Message();
				msg.obj = "processing...";
				mHandler.sendMessage(msg);
			}
			else {
				int max_val = 0;
				String result_label = null;
				Iterator<String> iterator = map.keySet().iterator();
				while(iterator.hasNext()) {
					String key = iterator.next();
					int val = map.get(key);
					if(val > max_val) {
						result_label = key;
					}
				}
				msg = new Message();
				msg.obj = result_label;
				mHandler.sendMessage(msg);
			}
			*/

			/*
			textTochange = fr.predict(m);
			mLikely = fr.getProb();
			msg = new Message();
			msg.obj = textTochange;
			mHandler.sendMessage(msg);
			*/
		}
		for (int i = 0; i < facesArray.length; i++)
			Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);

		return mRgba;
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i(TAG, "called onCreateOptionsMenu");
		if (mOpenCvCameraView.numberCameras() > 1) {
			nBackCam = menu.add(getResources().getString(R.string.SFrontCamera));
			mFrontCam = menu.add(getResources().getString(R.string.SBackCamera));
//        mEigen = menu.add("EigenFaces");
//        mLBPH.setChecked(true);
		} else {
			imCamera.setVisibility(View.INVISIBLE);

		}
		//mOpenCvCameraView.setAutofocus();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
		nBackCam.setChecked(false);
		mFrontCam.setChecked(false);
		//  mEigen.setChecked(false);
		if (item == nBackCam) {
			mOpenCvCameraView.setCamFront();
			mChooseCamera = frontCam;
		}
		else if (item == mFrontCam) {
			mChooseCamera = backCam;
			mOpenCvCameraView.setCamBack();

		}

		item.setChecked(true);

		return true;
	}

	private void setMinFaceSize(float faceSize) {
		mRelativeFaceSize = faceSize;
		mAbsoluteFaceSize = 0;
	}

	private String getSharedResourceCredential() {
		sharedPref = PreferenceManager.getDefaultSharedPreferences(FaceActivity.this);
		return sharedPref.getString(getString(R.string.share_perf_key),null);
	}

	public class SendAuthTask extends AsyncTask<Void, Void, String> {

		private final List<byte[]> imageBytesList;

		SendAuthTask(List<byte[]> imageBytesList) {
			this.imageBytesList = imageBytesList;
		}

		@Override
		protected String doInBackground(Void... params) {
			String content = null;

			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			String image_str1 = "", image_str2 = "";

			image_str1 = Base64.encodeToString(imageBytesList.get(0), Base64.DEFAULT);
			if(imageBytesList.size() > 1) {
				image_str2 = Base64.encodeToString(imageBytesList.get(1), Base64.DEFAULT);
			}

			nameValuePairs.add(new BasicNameValuePair("credential", getSharedResourceCredential()));
			nameValuePairs.add(new BasicNameValuePair("image1", image_str1));
			nameValuePairs.add(new BasicNameValuePair("image2", image_str2));

			String url = gv.getAuthURL() + gv.getTestPath();
			Log.d("SendAuthTask", url);
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(url);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				Log.d("SendAuthTask", "start to post image!");
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				Log.d("SendAuthTask", "finish to post image!");
				// Read the contents of an entity and return it as a String.
				content = EntityUtils.toString(entity);
				return content;
			}
			catch(Throwable t) {
				t.printStackTrace();
				Log.d("SendAuthTask", "post image exception!");
			}

			return content;
		}

		@Override
		protected void onPostExecute(final String result) {
			sendAuthTask = null;
			if(result != null) {
				Intent returnIntent = new Intent();
				String[] extras = result.split(":");
				returnIntent.putExtra("UserName", extras[0]);
				returnIntent.putExtra("Prob", Double.parseDouble(extras[1]));
				returnIntent.putExtra("Access", extras[2]);
				if(extras[0].isEmpty()) {
					setResult(RESULT_CANCELED, returnIntent);
				}
				else {
					setResult(RESULT_OK, returnIntent);
				}
				finish();
			}
			else {
				Log.d("SendAuthTask", "somthing wrong during postExecution!");
			}
		}

		@Override
		protected void onCancelled() {
			sendAuthTask = null;
		}
	}

	public String predict(List<Bitmap> mBitmaps) {

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
		sendAuthTask = new SendAuthTask(byteArrayList);
		sendAuthTask.execute();
		return "start to auth";
	}
}
