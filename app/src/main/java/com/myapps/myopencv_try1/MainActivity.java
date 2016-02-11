package com.myapps.myopencv_try1;

import android.app.Activity;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import java.util.ArrayList;


public class MainActivity extends Activity implements CvCameraViewListener2 {
    private String TAG = "MyOpenCV";

    private javaViewCameraControl mOpenCvCameraView;

    private TextView title_tv;
    private TextView frameNum;
    private TextView frameSize;
    private TextView frameAvg;
    private TextView imgProcessed;

    private UiDataBundle appData;
    private Mat myInputFrame;
    private DoubleTwoDimQueue dataQ;
    private int startPointer;
    private int endPointer;
    private int fftPoints;
    private int image_processed;
    private boolean first_fft_run;
    private boolean start_fft;

    private int FPS;
    private long BPM;

    private android.os.Handler mHandler;

    private int bad_frame_count;

    // Loader callback to connect with OpenCV manager
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    //Cosmetic: Constructor we need this for implementing Handler for UI thread
    public MainActivity() {

        Log.i(TAG, "Instantiated new " + this.getClass());

        mHandler = new android.os.Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message inputMessage){
                UiDataBundle incoming = (UiDataBundle) inputMessage.obj;

                frameNum.setText ("Num  :   " + incoming.image_got);
                frameSize.setText("Size :   " + incoming.frameSz);
                frameAvg.setText ("Avg  :   " + incoming.frameAv);
                //imgProcessed.setText("Frames : " + image_processed + " Bad Frames : " + bad_frame_count);
                imgProcessed.setText("BPM : " + BPM);
            }

        };

    }

    //Activity related member functions
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Log.i(TAG, "Trying to load OpenCV library");
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);

        // Init the local vars
        title_tv  = (TextView) findViewById(R.id.title);
        frameNum  = (TextView) findViewById(R.id.data1);
        frameSize = (TextView) findViewById(R.id.data2);
        frameAvg  = (TextView) findViewById(R.id.data3);
        imgProcessed = (TextView) findViewById(R.id.data4);
        appData =new UiDataBundle();
        appData.image_got=0;

        bad_frame_count = 0;
        dataQ = new DoubleTwoDimQueue();
        startPointer = 0;
        endPointer = 0;
        fftPoints = 1024;
        image_processed = 0;
        first_fft_run = true;
        FPS = 30;
        BPM = 0;

        //Connect to the camera view and prepare it
        mOpenCvCameraView = (javaViewCameraControl)findViewById(R.id.HelloOpenCvView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setMaxFrameSize(200, 200);
        //Can't have it here since the camera is not yet accessible to OpenCV
        //mOpenCvCameraView.setFrameRate(25000, 25000);           //We are trying to get 25FPS constant rate

        myThread.start();
        myFFTThread.start();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mOpenCvCameraView.turnFlashOff();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "OnDestory");
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    //Implement the abstract function for the abstract class
    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        appData.image_got++;
        myInputFrame = inputFrame.rgba();
        return myInputFrame;
    }

    //My functions for processing Mat frames
    public byte[] Mat2Byte(Mat img){
        int total_bytes = img.cols() * img.rows() * img.channels();
        byte[] return_byte = new byte[total_bytes];
        img.get(0, 0, return_byte);
        return return_byte;
    }

    public double getMatAvg(Mat img){
        double avg = 0.0;
        byte[] b_arr = Mat2Byte(img);

        for(int i =0 ; i < b_arr.length; i++){
            int val = (int)b_arr[i];

            if(val < 0)
                val = 256 + val;

            avg += val;
        }
        avg = avg/b_arr.length;
        return avg;
    }

    public void handleInputData(double data){
        int state = 0;
        double queueData[][] = new double[1][2];

        if(data < 180){
            state = 1;
        }

        queueData[0][0] = data;
        queueData[0][1] = 0.0;

        switch (state){
            case 0:
                bad_frame_count = 0;
                endPointer++;
                image_processed++;
                dataQ.Qpush(queueData);
                break;
            case 1:
                ++bad_frame_count;
                endPointer++;
                image_processed++;
                dataQ.Qpush(queueData);

                if(bad_frame_count > 5){
                    Log.e(TAG,"Expect errors. "+ bad_frame_count +" consecutive bad frames");
                }
                break;
            default:
                Log.e(TAG,"ERROR : UNKNOWN STATE");
        }

        //Triggering for FFT
        if(first_fft_run){
            if(image_processed >= 1024) {
                start_fft = true;
                Log.d(TAG + " My Thread","Start FFT set");
                first_fft_run = false;
                image_processed = 0;
            }
        } else {
            if(image_processed >= 128){
                start_fft = true;
                Log.d(TAG +" My Thread","Start FFT set");
                startPointer = startPointer  + 128;
                image_processed = 0;
            }
        }
    }

    // Computations needs to be done on a separate thread. Declaring the threads below

    //Thread 1 : Capture frames
    //This thread waits till the program is connected to the manager and first frame is obtained
    //After a frame is received, it calculates the avg value for further processing.
    Thread myThread = new Thread(){
        @Override
        public void run(){
            while (appData.image_got <= 0) {
                Log.d(TAG, "Waiting for image");
                try {
                    Thread.sleep(1000);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            int image_got_local = -1;
            mOpenCvCameraView.turnFlashOn();
            mOpenCvCameraView.setFrameRate(30000, 30000);           //We are trying to get 30FPS constant rate
            while(true){

                //We will wait till a new frame is received
                while(image_got_local == appData.image_got){
                    //Sleeping part may lead to timing problems
                    try {
                        Thread.sleep(11);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                appData.frameSz = myInputFrame.size();

                ArrayList<Mat> img_comp = new ArrayList<Mat>(3);
                Core.split(myInputFrame, img_comp);

                Mat myMat = img_comp.get(0);

                appData.frameAv = getMatAvg(myMat);

                //We cannot access UI objects from background threads, hence need to pass this data to UI thread
                Message uiMessage = mHandler.obtainMessage(1,appData);
                uiMessage.sendToTarget();

                handleInputData(appData.frameAv);
                image_got_local = appData.image_got;
            }
        }
    };

    Thread myFFTThread = new Thread(){
        @Override
        public void run(){
            while(true){
                if (start_fft == false){

                    //Sleeping part may lead to timing problems
                    Log.d(TAG + "FFT Thread","Start FFT is not set");
                    try {
                        Thread.sleep(100);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                else {

                    Log.d(TAG + "FFT Started", "Clearing the variable");
                    start_fft = false;

                    double[][] sample_arr = new double[fftPoints][2];
                    double[] freq_arr = new double[fftPoints];
                    fftLib f = new fftLib();
                    sample_arr = dataQ.toArray(startPointer, endPointer);

                    freq_arr = f.fft_energy_squared(sample_arr, fftPoints);

                    //Apply a filter
                    int deno = (int) (2 * FPS);
                    //int n_min = fftPoints / deno;       //start of the filter window
                    //int n_max = 6 * n_min;              //end of the filter window

                    //double max = freq_arr[n_min];
                    double max = freq_arr[1];
                    //int pos = n_min;
                    int pos = 1;
//                    for (int i = n_min + 1; i <= n_max; i++) {
//                        if (freq_arr[i] > max) {
//                            max = freq_arr[i];
//                            pos = i;
//                        }
//                    }
                    for(int i =1; i < fftPoints; i++){
                        if (freq_arr[i] > max) {
                            max = freq_arr[i];
                            pos = i;
                        }
                    }

                    double bps = (pos * FPS) / fftPoints;    //Calculate the freq
                    double bpm = (double) 60.0 * bps;        //Calculate bpm
                    BPM = Math.round(bpm);
                    Log.d(TAG+" FFT Thread", "MAX = " + max + " pos = " + pos);
                }
            }

        }
    };

}
