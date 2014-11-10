package com.example.cam;import java.io.*;import java.util.*;import android.annotation.*;import android.app.Activity;import android.content.Context;import android.content.Intent;import android.content.pm.*;import android.hardware.Camera;import android.hardware.Camera.PictureCallback;import android.hardware.Camera.ShutterCallback;import android.net.Uri;import android.os.*;import android.util.*;import android.view.SurfaceView;import android.view.View;import android.view.View.OnClickListener;import android.view.View.OnLongClickListener;import android.view.ViewGroup.LayoutParams;import android.view.Window;import android.view.WindowManager;import android.widget.*;import org.apache.http.*;import org.apache.http.client.*;import org.apache.http.client.entity.*;import org.apache.http.client.methods.*;import org.apache.http.entity.*;import org.apache.http.impl.client.*;import org.apache.http.message.*;import org.apache.http.protocol.*;import org.apache.http.util.*;import org.json.*;public class CamTestActivity extends Activity {	private static final String TAG = "CamTestActivity";	Preview preview;	static EditText edittext;	Button buttonClick;	Camera camera;	Activity act;	Context ctx;	private int cameraId = 0;	@Override	public void onCreate(Bundle savedInstanceState) {		super.onCreate(savedInstanceState);		ctx = this;		act = this;		requestWindowFeature(Window.FEATURE_NO_TITLE);		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);		setContentView(R.layout.main);				preview = new Preview(this, (SurfaceView)findViewById(R.id.surfaceView));		preview.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));		((FrameLayout) findViewById(R.id.layout)).addView(preview);		preview.setKeepScreenOn(true);		buttonClick = (Button) findViewById(R.id.takebutton);		buttonClick.setOnClickListener(new OnClickListener() {			@Override			public void onClick(View arg0) {				camera.takePicture(shutterCallback, rawCallback, jpegCallback);				/*if(edittext.getText().toString().trim().length() != 0) {					camera.takePicture(shutterCallback, rawCallback, jpegCallback);				}				else {					Toast.makeText(ctx, getString(R.string.take_photo_help1),							Toast.LENGTH_LONG).show();				}				edittext.setText("");*/			}		});		Toast.makeText(ctx, getString(R.string.take_photo_help), Toast.LENGTH_LONG).show();		//		buttonClick = (Button) findViewById(R.id.btnCapture);//		//		buttonClick.setOnClickListener(new OnClickListener() {//			public void onClick(View v) {////				preview.camera.takePicture(shutterCallback, rawCallback, jpegCallback);//				camera.takePicture(shutterCallback, rawCallback, jpegCallback);//			}//		});//		//		buttonClick.setOnLongClickListener(new OnLongClickListener(){//			@Override//			public boolean onLongClick(View arg0) {//				camera.autoFocus(new AutoFocusCallback(){//					@Override//					public void onAutoFocus(boolean arg0, Camera arg1) {//						//camera.takePicture(shutterCallback, rawCallback, jpegCallback);//					}//				});//				return true;//			}//		});	}	@Override	protected void onResume() {		super.onResume();//		preview.camera = Camera.open();		// do we have a camera?		if (!getPackageManager()				.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {			Toast.makeText(this, "No camera on this device", Toast.LENGTH_LONG)					.show();		} else {			cameraId = findFrontFacingCamera();			if (cameraId < 0) {				Toast.makeText(this, "No front facing camera found.",						Toast.LENGTH_LONG).show();			} else {				camera = Camera.open(cameraId);				camera.setDisplayOrientation(90);				camera.startPreview();				preview.setCamera(camera);			}		}		//camera = Camera.open();		//camera.startPreview();		//preview.setCamera(camera);	}	@Override	protected void onPause() {		if(camera != null) {			camera.stopPreview();			preview.setCamera(null);			camera.release();			camera = null;		}		super.onPause();	}	@TargetApi(Build.VERSION_CODES.GINGERBREAD)	private int findFrontFacingCamera() {		int cameraId = -1;		// Search for the front facing camera		int numberOfCameras = Camera.getNumberOfCameras();		for (int i = 0; i < numberOfCameras; i++) {			Camera.CameraInfo info = new Camera.CameraInfo();			Camera.getCameraInfo(i, info);			if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {				Log.d(TAG, "Camera found");				cameraId = i;				break;			}		}		return cameraId;	}	private void resetCam() {		camera.startPreview();		preview.setCamera(camera);	}		private void refreshGallery(File file) {		Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);	    mediaScanIntent.setData(Uri.fromFile(file));	    sendBroadcast(mediaScanIntent);	}	ShutterCallback shutterCallback = new ShutterCallback() {		public void onShutter() {//			 Log.d(TAG, "onShutter'd");		}	};	PictureCallback rawCallback = new PictureCallback() {		public void onPictureTaken(byte[] data, Camera camera) {//			 Log.d(TAG, "onPictureTaken - raw");		}	};	PictureCallback jpegCallback = new PictureCallback() { 		public void onPictureTaken(byte[] data, Camera camera) {			new PushImageTask().execute(data);			//new JSONObjectTask().execute(data);			resetCam();			Log.d(TAG, "onPictureTaken - jpeg");		}	};	private class JSONObjectTask extends AsyncTask<byte[], Void, Void> {		@Override		protected Void doInBackground(byte[]... data) {			JSONObject obj=new JSONObject();			try {				obj.put("title", "title1");				obj.put("comment", "comment1");			} catch (JSONException e) {				Log.d(TAG,"creating JSON object error!");				e.printStackTrace();			}			StringEntity se=null;			try {				se = new StringEntity(obj.toString());			} catch (UnsupportedEncodingException e) {				e.printStackTrace();			}			try{				HttpClient httpclient = new DefaultHttpClient();				HttpPost httppost = new HttpPost("http://10.0.2.2:8080/CentralServer/rest/hello/");				se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));				httppost.setEntity(se);				httppost.setHeader("Accept", "application/json");				httppost.setHeader("Content-type", "application/json");				Log.d(TAG, "works till here. 2");				try {					HttpResponse response = httpclient.execute(httppost);					Log.d(TAG, "works till here. 3");				} catch (ClientProtocolException e) {					e.printStackTrace();				} catch (IOException e) {					e.printStackTrace();				}			} catch (Exception e) {				e.printStackTrace();			}			return null;		}	}	private class PushImageTask extends AsyncTask<byte[], Void, Void> {		@Override		protected Void doInBackground(byte[]... data) {			String image_str = Base64.encodeToString(data[0], Base64.DEFAULT);			ArrayList<NameValuePair> nameValuePairs = new  ArrayList<NameValuePair>();			nameValuePairs.add(new BasicNameValuePair("id", "5"));			nameValuePairs.add(new BasicNameValuePair("image",image_str));			try{				HttpClient httpclient = new DefaultHttpClient();				HttpPost httppost = new HttpPost("http://10.0.17" +						".239:8080/CentralServer/json/pic/");				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));				HttpResponse response = httpclient.execute(httppost);				HttpEntity entity = response.getEntity();				//				// Read the contents of an entity and return it as a String.				//				final String content = EntityUtils.toString(entity);				runOnUiThread(new Runnable() {					@Override					public void run() {						Toast.makeText(CamTestActivity.this, content,								Toast.LENGTH_LONG).show();					}				});				/*final String the_string_response = convertResponseToString(response);				runOnUiThread(new Runnable() {					@Override					public void run() {						Toast.makeText(CamTestActivity.this, "Response " + the_string_response,								Toast.LENGTH_LONG).show();					}				});*/			}catch(final Exception e){				runOnUiThread(new Runnable() {					@Override					public void run() {						Toast.makeText(CamTestActivity.this, "ERROR " + e.getMessage(),								Toast.LENGTH_LONG).show();					}				});				System.out.println("Error in http connection "+e.toString());			}			return null;		}		public String convertResponseToString(HttpResponse response) throws IllegalStateException, IOException{			StringBuffer buffer = new StringBuffer();			InputStream inputStream = response.getEntity().getContent();			final int contentLength = (int) response.getEntity().getContentLength(); //getting			// content length…..			runOnUiThread(new Runnable() {				@Override				public void run() {					Toast.makeText(CamTestActivity.this, "contentLength : " + contentLength,							Toast.LENGTH_LONG).show();				}			});			if (contentLength < 0){			}			else{				byte[] data = new byte[512];				int len = 0;				try				{					while (-1 != (len = inputStream.read(data)) )					{						buffer.append(new String(data, 0, len)); //converting to string and appending  to stringbuffer…..					}				}				catch (IOException e)				{					e.printStackTrace();				}				try				{					inputStream.close(); // closing the stream…..				}				catch (IOException e)				{					e.printStackTrace();				}				final String res = buffer.toString();     // converting stringbuffer to string…..				runOnUiThread(new Runnable() {					@Override					public void run() {						Toast.makeText(CamTestActivity.this, "Result : " + res, Toast.LENGTH_LONG).show();					}				});				//System.out.println("Response => " +  EntityUtils.toString(response.getEntity()));			}			return buffer.toString();		}	}	private class SaveImageTask extends AsyncTask<byte[], Void, Void> {		String photoID;		public SaveImageTask(String photoID) {			super();			this.photoID = photoID;		}		@Override		protected Void doInBackground(byte[]... data) {			FileOutputStream outStream = null;			// Write to SD Card			try {	            File sdCard = Environment.getExternalStorageDirectory();	            File dir = new File (sdCard.getAbsolutePath() + "/camtest");	            dir.mkdirs();								Log.d(TAG, photoID);				String fileName = String.format("%s-%d.jpg", photoID,						System.currentTimeMillis());				File outFile = new File(dir, fileName);								outStream = new FileOutputStream(outFile);				outStream.write(data[0]);				outStream.flush();				outStream.close();								Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length + " to " + outFile.getAbsolutePath());				refreshGallery(outFile);			} catch (FileNotFoundException e) {				e.printStackTrace();			} catch (IOException e) {				e.printStackTrace();			} finally {			}			return null;		}			}}