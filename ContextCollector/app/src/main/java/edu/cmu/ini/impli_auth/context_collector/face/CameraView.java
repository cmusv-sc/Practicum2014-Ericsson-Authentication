package edu.cmu.ini.impli_auth.context_collector.face;

import android.content.*;
import android.hardware.Camera;
import android.util.*;

import org.opencv.android.*;

/**
 * Customized camera view extended from org.opencv.android.JavaCameraView.
 * It is used to receive and display the camera live stream
 */
public class CameraView extends JavaCameraView {

	private static final String TAG = "ContextCollector::CameraView";

	public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
		Log.d(TAG, "Start the camera view");
	}

	/**
	 * set front camera for camera view
	 */
	public void setCamFront() {
		disconnectCamera();
		setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
		connectCamera(getWidth(), getHeight());
	}

	/**
	 * set back camera for camera view
	 */
	public void setCamBack() {
		disconnectCamera();
		setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK);
		connectCamera(getWidth(), getHeight());
	}

	/**
	 * used to check whether device has front and back cameras or not
	 * @return number of camera in current device
	 */
	public int numberCameras() {
		return Camera.getNumberOfCameras();
	}

}
