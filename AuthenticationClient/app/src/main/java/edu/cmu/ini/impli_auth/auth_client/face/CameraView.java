package edu.cmu.ini.impli_auth.auth_client.face;

import android.content.*;
import android.hardware.Camera;
import android.util.*;

import org.opencv.android.*;


public class CameraView extends JavaCameraView {

	private static final String TAG = "AuthenticationClient::CameraView";

	public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
		Log.d(TAG, "Start the camera view");
	}

	public void setCamFront() {
		disconnectCamera();
		setCameraIndex(org.opencv.android.CameraBridgeViewBase.CAMERA_ID_FRONT);
		connectCamera(getWidth(), getHeight());
	}

	public void setCamBack() {
		disconnectCamera();
		setCameraIndex(org.opencv.android.CameraBridgeViewBase.CAMERA_ID_BACK);
		connectCamera(getWidth(), getHeight());
	}

	public int numberCameras() {
		return Camera.getNumberOfCameras();
	}

}
