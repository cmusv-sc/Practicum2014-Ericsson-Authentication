package com.impl_auth.authenticationclient;


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

	TextView textresult;
	Bitmap mBitmap;
	Handler mHandler;

	LBPHFaceExtractor fe;
	ToggleButton toggleButtonTrain;
	Button buttonSubmit;
	ImageView ivGreen, ivYellow, ivRed;
	ImageButton imCamera;

	TextView textState;
	com.googlecode.javacv.cpp.opencv_contrib.FaceRecognizer faceRecognizer;


	static final long MAXIMG = 1;

	ArrayList<Mat> alimgs = new ArrayList<Mat>();

	int[] labels = new int[(int) MAXIMG];
	int countImages = 0;
	int countSearch = 0;

	Map<String, Integer> map = new HashMap<String, Integer>();


	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
				case LoaderCallbackInterface.SUCCESS: {
					Log.i(TAG, "OpenCV loaded successfully");

					// Load native library after(!) OpenCV initialization
					//   System.loadLibrary("detection_based_tracker");

					fe = new LBPHFaceExtractor(mPath);
					//fr.load();

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

		mOpenCvCameraView = (CameraView) findViewById(R.id.tutorial3_activity_java_surface_view);

		mOpenCvCameraView.setCvCameraViewListener(this);


		File dir = new File(getFilesDir() + "/camtest");
		dir.mkdirs();
		mPath = getFilesDir() + "/camtest/";

		textresult = (TextView) findViewById(R.id.textView1);

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.obj == "IMG") {
					Canvas canvas = new Canvas();
					canvas.setBitmap(mBitmap);
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
		toggleButtonTrain = (ToggleButton) findViewById(R.id.toggleButton1);
		buttonSubmit = (Button) findViewById(R.id.submitButton);
		textState = (TextView) findViewById(R.id.textViewState);
		ivGreen = (ImageView) findViewById(R.id.imageView3);
		ivYellow = (ImageView) findViewById(R.id.imageView4);
		ivRed = (ImageView) findViewById(R.id.imageView2);
		imCamera = (ImageButton) findViewById(R.id.imageButton1);

		ivGreen.setVisibility(View.INVISIBLE);
		ivYellow.setVisibility(View.INVISIBLE);
		ivRed.setVisibility(View.INVISIBLE);
		textresult.setVisibility(View.INVISIBLE);


		toggleButtonTrain.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (toggleButtonTrain.isChecked()) {
					countImages = 0;
					textState.setText("Taking Picture");
					textresult.setVisibility(View.VISIBLE);
					//textresult.setText(getResources().getString(R.string.SFaceName));
					ivGreen.setVisibility(View.INVISIBLE);
					ivYellow.setVisibility(View.INVISIBLE);
					ivRed.setVisibility(View.INVISIBLE);
					faceState = TRAINING;
				} else {
					//textState.setText(R.string.Straininig);
					textresult.setText("");
					textresult.setText("");
					textState.setText(getResources().getString(R.string.SIdle));
					faceState = IDLE;
				}
			}
		});

		buttonSubmit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				sendRegistrationRequest();
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

		boolean success = (new File(mPath)).mkdirs();
		if (!success) {
			Log.e("Error", "Error creating directory");
		}
	}

	private void sendRegistrationRequest() {
		fe.register();
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

		if ((facesArray.length > 0) && (faceState == TRAINING)) {

			Mat m = new Mat();
			Rect r = facesArray[0];
			m = mRgba.submat(r);
			mBitmap = Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);

			Utils.matToBitmap(m, mBitmap);

			Message msg = new Message();
			String textTochange = "IMG";
			msg.obj = textTochange;
			mHandler.sendMessage(msg);
			if (countImages < MAXIMG) {
				fe.saveMat(m);
				countImages++;
			}
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
