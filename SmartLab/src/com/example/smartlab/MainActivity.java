package com.example.smartlab;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.imgproc.Imgproc;

public class MainActivity extends Activity {
	
	private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
		   switch (status) {
		       case LoaderCallbackInterface.SUCCESS:
		       {
		    	   setupMenu();
		       } break;
		       default:
		       {
		    	   super.onManagerConnected(status);
		       } break;
		   }
		}
	};
	
	private void setupMenu(){
 	   setContentView(R.layout.activity_main);
 	   //View btn = findViewById(R.id.countRBCs);
 	   //View root = btn.getRootView();
 	   //root.setBackgroundColor(Color.BLACK);
 	   addButtonListener();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	    Log.i("OpenCV Setup", "Trying to load OpenCV library");
	    if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_8, this, mOpenCVCallBack))
	    {
	      Log.e("OpenCV Setup", "Cannot connect to OpenCV Manager");
	    }

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
	    super.onActivityResult(requestCode, resultCode, data);
	    if (resultCode == RESULT_OK)
	    {
	        Uri chosenImageUri = data.getData();

	        Bitmap mBitmap = null;
	        try {
				mBitmap = Media.getBitmap(this.getContentResolver(), chosenImageUri);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	        if(mBitmap != null){
	        	countRBCs(mBitmap);
	        }
	     }
	    
	}
	
	private void countRBCs(Bitmap image){
		try {
		    
			Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
			Bitmap bmp32 = image.copy(Bitmap.Config.ARGB_8888, true); 
		    Utils.bitmapToMat(bmp32, mat);
		
		    Mat gray = new Mat(image.getHeight(),image.getWidth(),CvType.CV_8UC1);
		    Mat circles = new Mat();
		    
		    
		    Imgproc.cvtColor(mat,  gray,  Imgproc.COLOR_RGB2GRAY);
		    Imgproc.GaussianBlur(gray, gray, new Size(9,9), 0.5, 0.5);
		    
		    Imgproc.HoughCircles(gray, circles, Imgproc.CV_HOUGH_GRADIENT, 1, 8, 150, 12, 8, 15);
		    System.out.println("width: " + String.valueOf(circles.size().width) + ", height: " + String.valueOf(circles.size().height));
		    Log.i("Function", "Setup");
		    
		    for (int i=0; i < circles.size().width; i++) {
		    	Point3 circ = new Point3(circles.get(0, i));
		    	Point center = new Point(circ.x, circ.y);
		    	
		    	Core.circle(mat, center, 3, new Scalar(0,255,0));
		    	Core.circle(mat, center, (int)Math.round(circ.z), new Scalar(255,0,0));
		    	
		    	System.out.println("x: " + String.valueOf(circ.x) + ", y: " + String.valueOf(circ.y) + 
		    			", r: " + String.valueOf(circ.z));
		    }
		    
		    Imgproc.cvtColor(mat,  mat,  Imgproc.COLOR_RGB2BGR);
			byte[] outData = new byte[mat.rows()*mat.cols()*(int)(mat.elemSize())];
			mat.get(0, 0, outData);
			
			Bitmap bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
			Utils.matToBitmap(mat, bmp);
			
			setContentView(R.layout.image_view);
			ImageView i = (ImageView) findViewById(R.id.image);
			addListenerOnButton();
			Button countBtn = (Button) findViewById(R.id.button);
			Button runAgainBtn = (Button) findViewById(R.id.runAgain);

			
			try {
				i.setImageBitmap(bmp);
				countBtn.setText("Count: " + circles.size().width);
				runAgainBtn.setText("Analyze Another Sample");
			} catch (Exception e) {
			       Log.e("Error", "Error occured");
			       e.printStackTrace();
			}
			Log.i("Function", "4");
			
			
		} catch (Exception e) {
			if(e.getMessage() != null){
			 Log.e("Error",e.getMessage());
			}else{
				Log.e("Error", "Some weird error occured");
			}
	   	}
		
	}
	
	private void photoPicker(){
		Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, 1);
	}
	
	public void addListenerOnButton() {
 
		Button button = (Button) findViewById(R.id.runAgain);
		button.setOnClickListener(new OnClickListener() {
 
			@Override
			public void onClick(View arg0) {
				photoPicker();
			}
 
		});
 
	}
	
	public void addButtonListener() {
		 
		Button button = (Button) findViewById(R.id.countRBCs);
		button.setOnClickListener(new OnClickListener() {
 
			@Override
			public void onClick(View arg0) {
				photoPicker();
			}
 
		});
 
	}

}
