package edu.cmu.ini.impli_auth.context_collector.face;


import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
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

import edu.cmu.ini.impli_auth.context_collector.*;
import edu.cmu.ini.impli_auth.context_collector.util.*;

/**
 * This activity is for users to take pictures for registering themselves in this system
 */
public class FaceActivity extends Activity implements CvCameraViewListener2 {

	private static final String TAG = "ContextCollector::FaceActivity";

	private static final int TRAINING = 0;
	private static final int IDLE = 1;
	private int faceState = IDLE;

	private static final int frontCam = 0;
	private static final int backCam = 1;
	private int mChooseCamera = backCam;
	private CameraView mOpenCvCameraView;

	private static final int MAX_PIC = 5;
	private int countImages = 0;

	private Handler mHandler;
	private static final int FINISH_TRAINING = 0;
	private static final int TRAINING_STATE = 1;


	LBPHFaceExtractor fe;
	private Mat mRgba;
	private Mat mGray;
	private CascadeClassifier mJavaDetector;
	private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
	private static final float RELATIVE_FACE_SIZE = 0.2f;
	private int mAbsoluteFaceSize = 0;

	private String mPath = "";

	Bitmap mBitmap;
	TextView textState;
	Button pictakeButton;
	Button submitButton;
	ImageButton imCamera;

	private GlobalVariable gv;
	private SendRegistrationFormTask sendRegistrationFormTask;

	private String mUsername;
	private String mPassword;
	private String mFirstName;
	private String mLastName;
	private String mEmail;


	/**
	 * After OpenCV is loaded successful, load the LBP based xml description file for face detection
	 */
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
				case LoaderCallbackInterface.SUCCESS: {
					Log.i(TAG, "OpenCV loaded successfully");
					fe = new LBPHFaceExtractor(mPath);
					try {
						// load cascade file from application resources
						InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
						File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
						File mCascadeFile = new File(cascadeDir, "lbpcascade.xml");
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
						} else {
							Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());
						}

						if (!cascadeDir.delete()) {
							Log.e("Error", "Error deleting face model cascade directory");
						}
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
		gv = GlobalVariable.getInstance();
		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	/**
	 * Initialize the camera view, and every widget in this activity. Also, create directory for
	 * training image temporary files.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.face_detect_surface_view);

		mOpenCvCameraView = (CameraView) findViewById(R.id.cameraView);
		mOpenCvCameraView.setCvCameraViewListener(this);

		if (mOpenCvCameraView.numberCameras() < 2) {
			Log.e(TAG, "You are supposed to have front and back camera");
		}


		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == FINISH_TRAINING) {
					pictakeButton.setVisibility(View.INVISIBLE);
					submitButton.setVisibility(View.VISIBLE);
				} else if (msg.what == TRAINING_STATE) {
					textState.setText(msg.obj.toString());
				}
			}
		};

		countImages = 0;
		pictakeButton = (Button) findViewById(R.id.pictakeButton);
		submitButton = (Button) findViewById(R.id.submitButton);
		submitButton.setVisibility(View.INVISIBLE);
		textState = (TextView) findViewById(R.id.stateTextView);
		imCamera = (ImageButton) findViewById(R.id.cameraImageButton);

		pictakeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				faceState = TRAINING;
				textState.setText(R.string.SPictaking);
			}
		});

		submitButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				register(mUsername, mPassword, mFirstName, mLastName, mEmail);
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

		mPath = getFilesDir() + "/camtest/";
		boolean success = (new File(mPath)).mkdirs();
		if (!success) {
			Log.e("Error", "Error creating directory");
		}

		Intent intent = getIntent();
		mUsername = intent.getStringExtra("UserName");
		mPassword = intent.getStringExtra("Password");
		mFirstName = intent.getStringExtra("FirstName");
		mLastName = intent.getStringExtra("LastName");
		mEmail = intent.getStringExtra("Email");

		textState.setText(R.string.SWholePicture);
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
		// resume the OpenCV library
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mOpenCvCameraView.disableView();
	}

	/**
	 * initialize the Mat object of training image.
	 * @param width -  the width of the frames that will be delivered
	 * @param height - the height of the frames that will be delivered
	 */
	@Override
	public void onCameraViewStarted(int width, int height) {
		mGray = new Mat();
		mRgba = new Mat();
	}

	/**
	 * release the Mat object of training image
	 */
	@Override
	public void onCameraViewStopped() {
		mGray.release();
		mRgba.release();
	}

	/**
	 * It is used to control taking pictures process, save input frame of user's face as
	 * temporary files and show focusing box for user's face
	 * @param inputFrame inputFrame can from camera sensor
	 * @return Mat object will be shown on camera view
	 */
	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

		mRgba = inputFrame.rgba();
		mGray = inputFrame.gray();

		if (mAbsoluteFaceSize == 0) {
			int height = mGray.rows();
			if (Math.round(height * RELATIVE_FACE_SIZE) > 0) {
				mAbsoluteFaceSize = Math.round(height * RELATIVE_FACE_SIZE);
			}
		}

		MatOfRect faces = new MatOfRect();

		if (mJavaDetector != null)
			mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, new Size(mAbsoluteFaceSize,
					mAbsoluteFaceSize), new Size());


		Rect[] facesArray = faces.toArray();
		Log.d(TAG, "Face number: " + facesArray.length);

		// only consider one user in the single input frame
		if ((facesArray.length == 1) && (faceState == TRAINING)) {

			faceState = IDLE;
			Rect r = facesArray[0];
			Mat m = mRgba.submat(r);
			mBitmap = Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);

			Utils.matToBitmap(m, mBitmap);

			if (countImages < MAX_PIC) {
				countImages++;
				fe.saveMat(m);
				if (MAX_PIC - countImages == 0) {
					Message msg = new Message();
					msg.what = TRAINING_STATE;
					msg.obj = getString(R.string.SPictureDone);
					mHandler.sendMessage(msg);

					msg = new Message();
					msg.what = FINISH_TRAINING;
					mHandler.sendMessage(msg);
				} else {
					Message msg = new Message();
					msg.what = TRAINING_STATE;
					String formatStr = getString(R.string.SPictureLeft);
					msg.obj = String.format(formatStr, MAX_PIC - countImages);
					mHandler.sendMessage(msg);
				}
			}
		}
		for (Rect r : facesArray) {
			Core.rectangle(mRgba, r.tl(), r.br(), FACE_RECT_COLOR, 3);
		}
		return mRgba;
	}

	/**
	 * Send username, password, firstname, lastname, email and 5 training images (temporary file)
	 * to Authentication Server to do registration
	 * @param mUsername username
	 * @param mPassword password
	 * @param mFirstName firstname
	 * @param mLastName lastname
	 * @param mEmail email
	 * @return whether registration is successful or not
	 */
	public boolean register(String mUsername, String mPassword, String mFirstName,
	                        String mLastName, String mEmail) {

		File root = new File(mPath);

		FilenameFilter pngFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".jpg");
			}
		};

		File[] imgFiles = root.listFiles(pngFilter);
		List<byte[]> imageBytesList = new ArrayList<byte[]>();

		for (File imgFile : imgFiles) {
			if (imgFile.exists()) {
				Log.d("SendRegistrationFormTask", "get image!");
				Bitmap bmp = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
				imageBytesList.add(baos.toByteArray());
				if (!imgFile.delete()) {
					Log.e("Error", "Error deleting image of user's face");
				}
			}
		}
		sendRegistrationFormTask = new SendRegistrationFormTask(mUsername, mPassword,
				mFirstName, mLastName,
				mEmail,
				imageBytesList);
		sendRegistrationFormTask.execute();
		return true;
	}

	/**
	 * AsyncTask to do HTTP post for registration
	 */
	public class SendRegistrationFormTask extends AsyncTask<Void, Void, Boolean> {

		private final String mUsername;
		private final String mPassword;
		private final String mFirstName;
		private final String mLastName;
		private final String mEmail;
		private final List<byte[]> imageBytesList;


		SendRegistrationFormTask(String mUsername, String mPassword, String mFirstName,
		                         String mLastName, String mEmail, List<byte[]> imageBytesList) {
			this.mUsername = mUsername;
			this.mPassword = mPassword;
			this.mFirstName = mFirstName;
			this.mLastName = mLastName;
			this.mEmail = mEmail;
			this.imageBytesList = imageBytesList;
		}

		/**
		 * bundle HTTP POST parameter, and send to Authentication Server
		 * @param params nothing, we pass parameter as instance variable
		 * @return succeed or not
		 */
		@Override
		protected Boolean doInBackground(Void... params) {
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

			nameValuePairs.add(new BasicNameValuePair("Username", mUsername));
			nameValuePairs.add(new BasicNameValuePair("Password", mPassword));
			nameValuePairs.add(new BasicNameValuePair("FirstName", mFirstName));
			nameValuePairs.add(new BasicNameValuePair("LastName", mLastName));
			nameValuePairs.add(new BasicNameValuePair("Email", mEmail));

			Log.d("SendRegistrationFormTask", "ArrayList size is " + imageBytesList.size());
			for (int i = 0; i < imageBytesList.size(); i++) {
				String image_str = Base64.encodeToString(imageBytesList.get(i), Base64.DEFAULT);
				nameValuePairs.add(new BasicNameValuePair(String.format("image%d", i + 1),
						image_str));
			}

			String url = gv.getAuthURL() + gv.getTestPath();

			Log.d("SendRegistrationFormTask", "start to send regis!");

			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(url);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				Log.d("SendRegistrationFormTask", "finish send regis!");
				// Read the contents of an entity and return it as a String.
				final String content = EntityUtils.toString(entity);
				return content.equalsIgnoreCase("Succeed");

			} catch (final Exception e) {
				System.out.println("Error in http connection " + e.toString());
			}
			return false;

		}

		/**
		 * Get the result of registration from Authentication Server,
		 * and return back to registration form activity.
		 *
		 * @param success HTTP POST succeeds or not
		 */
		@Override
		protected void onPostExecute(final Boolean success) {
			sendRegistrationFormTask = null;
			Intent returnIntent = new Intent();
			returnIntent.putExtra("Result", success);
			setResult(RESULT_OK, returnIntent);
			finish();
		}

		@Override
		protected void onCancelled() {
			sendRegistrationFormTask = null;
		}
	}
}
