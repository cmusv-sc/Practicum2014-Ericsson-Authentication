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

/**
 * This activity is for users to do facial recognition login.
 */
public class FaceActivity extends Activity implements CvCameraViewListener2 {

	private static final String TAG = "AuthenticationClient::FaceActivity";

	public static final int SEARCHING = 0;
	public static final int IDLE = 1;
	private int faceState = IDLE;

	private static final int frontCam = 0;
	private static final int backCam = 1;
	private int mChooseCamera = backCam;
	private CameraView mOpenCvCameraView;

	LBPHFaceExtractor fe;
	private Mat mRgba;
	private Mat mGray;
	private CascadeClassifier mJavaDetector;
	private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
	private static final float RELATIVE_FACE_SIZE = 0.2f;
	private int mAbsoluteFaceSize = 0;

	private static final int MAX_SEARCH_PIC = 1;
	private int countSearch = 0;

	TextView textResult;
	ToggleButton searchButton;
	ImageButton imCamera;

	private GlobalVariable gv;
	public SendAuthTask sendAuthTask;

	/**
	 * After OpenCV is loaded successful, load the LBP based xml description file for face detection
	 */
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
				case LoaderCallbackInterface.SUCCESS: {
					Log.i(TAG, "OpenCV loaded successfully");
					fe = new LBPHFaceExtractor();
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
	 * Initialize the camera view, and every widget in this activity.
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

		textResult = (TextView) findViewById(R.id.resultTextView);
		searchButton = (ToggleButton) findViewById(R.id.searchButton);
		imCamera = (ImageButton) findViewById(R.id.cameraImageButton);

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
					faceState = SEARCHING;
				} else {
					faceState = IDLE;
					textResult.setText(getResources().getString(R.string.SIdle));
				}
			}
		});

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
	 * It is used to control taking pictures process, put image of users in the current input
	 * frame in to a list and send it for prediction. Also, show focusing box for user's face
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

		if (mJavaDetector != null) {
			mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2,
					new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
		}

		Rect[] facesArray = faces.toArray();
		Log.d(TAG, "Face number: " + facesArray.length);

		if ((facesArray.length > 0) && (faceState == SEARCHING)) {
			if (countSearch < MAX_SEARCH_PIC) {
				List<Bitmap> bitmaps = new ArrayList<Bitmap>();
				for (Rect r : facesArray) {
					Mat m = mGray.submat(r);
					Bitmap mBitmap = Bitmap.createBitmap(m.width(), m.height(),
							Bitmap.Config.ARGB_8888);
					Utils.matToBitmap(m, mBitmap);
					bitmaps.add(mBitmap);
				}
				predict(bitmaps);
				countSearch++;
			}
		}

		for (Rect r : facesArray) {
			Core.rectangle(mRgba, r.tl(), r.br(), FACE_RECT_COLOR, 3);
		}
		return mRgba;
	}

	/**
	 * get credential of this SharedResource for Authenticaiton Server to check whether certain
	 * users are near SharedResource(display) or not.
	 * @return Credential of this SharedResource
	 */
	private String getSharedResourceCredential() {
		SharedPreferences sharedPref;
		sharedPref = PreferenceManager.getDefaultSharedPreferences(FaceActivity.this);
		return sharedPref.getString(getString(R.string.share_perf_key), null);
	}

	/**
	 * AsyncTask to do HTTP post for authentication
	 */
	public class SendAuthTask extends AsyncTask<Void, Void, String> {

		private final List<byte[]> imageBytesList;

		SendAuthTask(List<byte[]> imageBytesList) {
			this.imageBytesList = imageBytesList;
		}

		/**
		 * bundle HTTP POST parameter, and send to Authentication Server
		 * @param params nothing, we pass parameter as instance variable
		 * @return username, public or private resource, probability of this authentication
		 */
		@Override
		protected String doInBackground(Void... params) {
			String content = "";

			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			String image_str1 = Base64.encodeToString(imageBytesList.get(0), Base64.DEFAULT);

			String image_str2 = "";
			if (imageBytesList.size() > 1) {
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
			} catch (Throwable t) {
				t.printStackTrace();
				Log.d("SendAuthTask", "post image exception!");
			}

			return content;
		}

		/**
		 * Get the result of authentication from Authentication Server,
		 * and hand over the result back to third party application.
		 * @param result username, public or private resource, probability of this authentication
		 */
		@Override
		protected void onPostExecute(final String result) {
			sendAuthTask = null;
			if (!result.isEmpty()) {
				Intent returnIntent = new Intent();
				String[] extras = result.split(":");
				returnIntent.putExtra("UserName", extras[0]);
				returnIntent.putExtra("Prob", Double.parseDouble(extras[1]));
				returnIntent.putExtra("Access", extras[2]);
				if (extras[0].isEmpty()) {
					setResult(RESULT_CANCELED, returnIntent);
				} else {
					setResult(RESULT_OK, returnIntent);
				}
				finish();
			} else {
				Log.d("SendAuthTask", "somthing wrong during postExecution!");
			}
		}

		@Override
		protected void onCancelled() {
			sendAuthTask = null;
		}
	}

	/**
	 * Send byte array of users' face images to AsynTask for HTTP POST
	 * @param mBitmaps List of bitmap of users' face image
	 */
	public void predict(List<Bitmap> mBitmaps) {
		List<byte[]> byteArrayList = fe.constructTestImages(mBitmaps);
		sendAuthTask = new SendAuthTask(byteArrayList);
		sendAuthTask.execute();
	}
}
