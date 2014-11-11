package edu.cmu.ini.impli_auth.face;


import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import org.opencv.android.*;
import org.opencv.android.CameraBridgeViewBase.*;
import org.opencv.core.*;
import org.opencv.core.Rect;
import org.opencv.objdetect.*;

import java.io.*;
import java.util.*;

public class CamActivity extends Activity implements CvCameraViewListener2 {

	private static final String TAG = "OCVSample::FdActivity";
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

	private Tutorial3View mOpenCvCameraView;
	private int mChooseCamera = backCam;

	EditText text;
	TextView textresult;
	//private  ImageView Iv;
	Bitmap mBitmap;
	Handler mHandler;

	LBPFaceRecognizer fr;
	ToggleButton toggleButtonGrabar, toggleButtonTrain, buttonSearch;
	Button buttonCatalog;
	ImageView ivGreen, ivYellow, ivRed;
	ImageButton imCamera;

	TextView textState;
	com.googlecode.javacv.cpp.opencv_contrib.FaceRecognizer faceRecognizer;


	static final long MAXIMG = 1;
	static final long MAXIMG_SEARCH = 1;

	ArrayList<Mat> alimgs = new ArrayList<Mat>();

	int[] labels = new int[(int) MAXIMG];
	int countImages = 0;
	int countSearch = 0;

	Map<String, Integer> map = new HashMap<String, Integer>();

	Labels labelsFile;


	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
				case LoaderCallbackInterface.SUCCESS: {
					Log.i(TAG, "OpenCV loaded successfully");

					// Load native library after(!) OpenCV initialization
					//   System.loadLibrary("detection_based_tracker");

					fr = new LBPFaceRecognizer(mPath);
					String s = getResources().getString(R.string.Straininig);
					Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
					fr.load();

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
				}
				break;
				default: {
					super.onManagerConnected(status);
				}
				break;
			}
		}
	};

	public CamActivity() {
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

		mOpenCvCameraView = (Tutorial3View) findViewById(R.id.tutorial3_activity_java_surface_view);

		mOpenCvCameraView.setCvCameraViewListener(this);


		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File(sdCard.getAbsolutePath() + "/camtest");
		dir.mkdirs();
		mPath = sdCard.getAbsolutePath() + "/camtest/";
		//mPath = getFilesDir() + "/facerecogOCV/";


		labelsFile = new Labels(mPath);

		//Iv=(ImageView)findViewById(R.id.imageView1);
		textresult = (TextView) findViewById(R.id.textView1);

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.obj == "IMG") {
					Canvas canvas = new Canvas();
					canvas.setBitmap(mBitmap);
					//Iv.setImageBitmap(mBitmap);
					if (countImages >= MAXIMG - 1) {
						toggleButtonGrabar.setChecked(false);
						grabarOnclick();
					}
				} else {
					textresult.setText(msg.obj.toString());
					ivGreen.setVisibility(View.INVISIBLE);
					ivYellow.setVisibility(View.INVISIBLE);
					ivRed.setVisibility(View.INVISIBLE);

					/*
					if (mLikely < 0) ;
					else if (mLikely < 50)
						ivGreen.setVisibility(View.VISIBLE);
					else if (mLikely < 80)
						ivYellow.setVisibility(View.VISIBLE);
					else
						ivRed.setVisibility(View.VISIBLE);
						*/
				}
			}
		};
		text = (EditText) findViewById(R.id.editText1);
		buttonCatalog = (Button) findViewById(R.id.buttonCat);
		toggleButtonGrabar = (ToggleButton) findViewById(R.id.toggleButtonGrabar);
		buttonSearch = (ToggleButton) findViewById(R.id.buttonBuscar);
		toggleButtonTrain = (ToggleButton) findViewById(R.id.toggleButton1);
		textState = (TextView) findViewById(R.id.textViewState);
		ivGreen = (ImageView) findViewById(R.id.imageView3);
		ivYellow = (ImageView) findViewById(R.id.imageView4);
		ivRed = (ImageView) findViewById(R.id.imageView2);
		imCamera = (ImageButton) findViewById(R.id.imageButton1);

		ivGreen.setVisibility(View.INVISIBLE);
		ivYellow.setVisibility(View.INVISIBLE);
		ivRed.setVisibility(View.INVISIBLE);
		text.setVisibility(View.INVISIBLE);
		textresult.setVisibility(View.INVISIBLE);

		toggleButtonGrabar.setVisibility(View.INVISIBLE);
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

		text.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((text.getText().toString().length() > 0) && (toggleButtonTrain.isChecked()))
					toggleButtonGrabar.setVisibility(View.VISIBLE);
				else
					toggleButtonGrabar.setVisibility(View.INVISIBLE);
				return false;
			}
		});


		toggleButtonTrain.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (toggleButtonTrain.isChecked()) {
					textState.setText(getResources().getString(R.string.SEnter));
					buttonSearch.setVisibility(View.INVISIBLE);
					textresult.setVisibility(View.VISIBLE);
					text.setVisibility(View.VISIBLE);
					textresult.setText(getResources().getString(R.string.SFaceName));
					if (text.getText().toString().length() > 0)
						toggleButtonGrabar.setVisibility(View.VISIBLE);

					ivGreen.setVisibility(View.INVISIBLE);
					ivYellow.setVisibility(View.INVISIBLE);
					ivRed.setVisibility(View.INVISIBLE);
				} else {
					textState.setText(R.string.Straininig);
					textresult.setText("");
					text.setVisibility(View.INVISIBLE);
					buttonSearch.setVisibility(View.VISIBLE);
					textresult.setText("");
					{
						toggleButtonGrabar.setVisibility(View.INVISIBLE);
						text.setVisibility(View.INVISIBLE);
					}
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.Straininig), Toast.LENGTH_LONG).show();
					fr.train();
					textState.setText(getResources().getString(R.string.SIdle));
				}
			}
		});


		toggleButtonGrabar.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				grabarOnclick();
			}
		});

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

		buttonSearch.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				if (buttonSearch.isChecked()) {
					if (!fr.canPredict()) {
						buttonSearch.setChecked(false);
						Toast.makeText(getApplicationContext(), getResources().getString(R.string.SCanntoPredic), Toast.LENGTH_LONG).show();
						return;
					}
					textState.setText(getResources().getString(R.string.SSearching));
					toggleButtonGrabar.setVisibility(View.INVISIBLE);
					toggleButtonTrain.setVisibility(View.INVISIBLE);
					text.setVisibility(View.INVISIBLE);
					countSearch = 0;
					map = new HashMap<String, Integer>();
					faceState = SEARCHING;
					textresult.setVisibility(View.VISIBLE);
				} else {
					faceState = IDLE;
					textState.setText(getResources().getString(R.string.SIdle));
					toggleButtonGrabar.setVisibility(View.INVISIBLE);
					toggleButtonTrain.setVisibility(View.VISIBLE);
					text.setVisibility(View.INVISIBLE);
					textresult.setVisibility(View.INVISIBLE);
				}
			}
		});

		boolean success = (new File(mPath)).mkdirs();
		if (!success) {
			Log.e("Error", "Error creating directory");
		}
	}

	void grabarOnclick() {
		if (toggleButtonGrabar.isChecked())
			faceState = TRAINING;
		else {
			if (faceState == TRAINING) ;
			// train();
			//fr.train();
			countImages = 0;
			faceState = IDLE;
		}
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

		if ((facesArray.length == 1) && (faceState == TRAINING) && (countImages < MAXIMG) && (!text.getText().toString().isEmpty())) {

			Mat m = new Mat();
			Rect r = facesArray[0];

			m = mRgba.submat(r);
			mBitmap = Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);

			Utils.matToBitmap(m, mBitmap);
			// SaveBmp(mBitmap,"/sdcard/db/I("+countTrain+")"+countImages+".jpg");

			Message msg = new Message();
			String textTochange = "IMG";
			msg.obj = textTochange;
			mHandler.sendMessage(msg);
			if (countImages < MAXIMG) {
				fr.add(m, text.getText().toString());
				countImages++;
			}

		} else if ((facesArray.length > 0) && (faceState == SEARCHING)) {
			Mat m = new Mat();
			m = mGray.submat(facesArray[0]);
			mBitmap = Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);


			Utils.matToBitmap(m, mBitmap);
			Message msg = new Message();
			String textTochange = "IMG";
			msg.obj = textTochange;
			mHandler.sendMessage(msg);

			if(countSearch < MAXIMG_SEARCH) {
				textTochange = fr.predict(mBitmap);
				countSearch++;
				msg = new Message();
				msg.obj = "processing...";
				mHandler.sendMessage(msg);
				msg = new Message();
				msg.obj = textTochange;
				mHandler.sendMessage(msg);
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
}
