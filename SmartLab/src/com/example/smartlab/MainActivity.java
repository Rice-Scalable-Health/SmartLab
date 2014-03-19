package com.example.smartlab;

import java.io.ByteArrayOutputStream;
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
import org.opencv.core.TermCriteria;

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
import android.widget.TextView;

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
		    Log.i("Init", "0");
			Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
			Bitmap bmp32 = image.copy(Bitmap.Config.ARGB_8888, true); 
		    Utils.bitmapToMat(bmp32, mat);
		    Log.i("Init", "1");
		    Mat gray = new Mat(image.getHeight(),image.getWidth(),CvType.CV_8UC1);
		    Log.i("Init", "2");
		    ByteArrayOutputStream stream = new ByteArrayOutputStream();
		    image.compress(Bitmap.CompressFormat.PNG, 100, stream);
		    byte[] byteArray = stream.toByteArray();
		    Log.i("Init", "3");
		    mat.put(0, 0, byteArray);
		    gray.put(0, 0, byteArray);
		    //Mat circles = new Mat();
		    Log.i("Test", "Worked till here");
		    
		    boolean hasAnemia = findIronDeficiencyCells(gray, mat, 3);
		    Log.i("Anemia", "Ran without error");
		    Log.i("Anemia", "Ran without error");
		    Log.i("Anemia", "Ran without error");
		    Log.i("Anemia", "Ran without error");
		    Log.i("Anemia", "Ran without error");
		    
		    
		    /*Imgproc.cvtColor(mat,  gray,  Imgproc.COLOR_RGB2GRAY);
		    Imgproc.GaussianBlur(gray, gray, new Size(9,9), 0.5, 0.5);
		    
		    Imgproc.HoughCircles(gray, circles, Imgproc.CV_HOUGH_GRADIENT, 1, 8, 150, 12, 8, 15);
		    System.out.println("width: " + String.valueOf(circles.size().width) + ", height: " + String.valueOf(circles.size().height));
		    
		    for (int i=0; i < circles.size().width; i++) {
		    	Point3 circ = new Point3(circles.get(0, i));
		    	Point center = new Point(circ.x, circ.y);
		    	
		    	Core.circle(mat, center, 3, new Scalar(0,255,0));
		    	Core.circle(mat, center, (int)Math.round(circ.z), new Scalar(255,0,0));
		    	
		    	System.out.println("x: " + String.valueOf(circ.x) + ", y: " + String.valueOf(circ.y) + 
		    			", r: " + String.valueOf(circ.z));
		    }
		    
		    Imgproc.cvtColor(mat,  mat,  Imgproc.COLOR_RGB2BGR);*/
		    byte[] outData = new byte[mat.rows()*mat.cols()*(int)(mat.elemSize())];
			mat.get(0, 0, outData);
			
			Bitmap bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
			Utils.matToBitmap(mat, bmp);
			
			setContentView(R.layout.image_view);
			ImageView i = (ImageView) findViewById(R.id.image);
			addListenerOnButton();
			TextView count = (TextView) findViewById(R.id.count);
			TextView result = (TextView) findViewById(R.id.result);
			Button runAgainBtn = (Button) findViewById(R.id.runAgain);

			
			try {
				i.setImageBitmap(bmp);
				//count.setText(" Count: " + circles.size().width);
				result.setText(" No sign of Anemia detected.");
				result.setTextColor(Color.GREEN);
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
		Log.i("Photo", "Started");
		Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, 1);
		Log.i("Photo", "Ended");
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
	
	/*
	 * Following are all image processing helper methods
	 * 
	 */
	
	public static void findHoughCirclesOld(Mat newCircles, Mat mat) {
		int[] radRange = {6, 10};
		int[] thresh = {25, 6};
	    Imgproc.GaussianBlur(mat, mat, new Size(9,9), 0.1, 0.1);
	    Imgproc.HoughCircles(mat, newCircles, Imgproc.CV_HOUGH_GRADIENT, 1, radRange[0],
	    		thresh[0], thresh[1], radRange[0], radRange[1]);
	}
	
	public static void findHoughCircles(Mat newCircles, Mat mat) {
		Mat tmpCircles = new Mat();
		int[] radRange = {6, 10};
		int[] thresh = {25, 6};
	    int numCell;

	    Imgproc.GaussianBlur(mat, mat, new Size(9,9), 0.1, 0.1);
	    Imgproc.HoughCircles(mat, tmpCircles, Imgproc.CV_HOUGH_GRADIENT, 1, radRange[0],
	    		thresh[0], thresh[1]+10, radRange[0], radRange[1]);
	    int circMean = getCircMean(tmpCircles, mat);
	    
	    tmpCircles = new Mat();
	    Imgproc.HoughCircles(mat, tmpCircles, Imgproc.CV_HOUGH_GRADIENT, 1, radRange[0],
	    		thresh[0], thresh[1], radRange[0], radRange[1]);
	    
	    Mat circles = new Mat(tmpCircles.rows(), tmpCircles.cols(), CvType.CV_32FC3);
	    numCell = removeFalsePos(tmpCircles, circles, mat, circMean);
	    circles = circles.submat(0, circles.rows(), 0, numCell);
	    newCircles.create(circles.rows(), numCell, CvType.CV_32FC3);
	    circles.copyTo(newCircles);
	}
	
	/**
	 * Draws circles around iron deficiency cells and returns whether or not the person has
	 * iron deficiency anemia.
	 */
	public static boolean findIronDeficiencyCells(Mat mat, Mat drawMat, int k) {
		Mat tmpCircles = new Mat();
		int[] radRange = {6, 10};
		int[] thresh = {60, 10};
		int[] count = new int[k];
	    int sickGroup = 0;
	    double maxCenter = 0;
	    double normalCellCenter = 0;
	    double probThresh = 30;
	    double centerThresh = 5;
	    double sickProb;
	    int numCell;
		
	    Imgproc.GaussianBlur(mat, mat, new Size(9,9), 0.1, 0.1);
	    Imgproc.HoughCircles(mat, tmpCircles, Imgproc.CV_HOUGH_GRADIENT, 1, radRange[0],
	    		thresh[0], thresh[1], radRange[0], radRange[1]);
	    numCell = (int)tmpCircles.size().width;
	    Mat intensities = new Mat(numCell, 1, CvType.CV_32FC1);
	    Mat labels = new Mat();
	    Mat centers = new Mat();
	    getCircIntensities(intensities, tmpCircles, mat);
	    Core.kmeans(intensities, k, labels, new TermCriteria(), 10, Core.KMEANS_PP_CENTERS, centers);
	    for (int i=0; i<labels.size().height; i++) {
	    	count[(int)labels.get(i,0)[0]] += 1;
	    }
	    
	    for (int i=0; i<k; i++) {
	    	double center = centers.get(i,0)[0];
		    System.out.println("Center: " + center + ", num: " + count[i]);
		    if (center > maxCenter) {
		    	normalCellCenter += count[sickGroup]*maxCenter;
		    	sickGroup = i;
		    	maxCenter = center;
		    } else {
		    	normalCellCenter += count[i]*center;
		    }
	    }
	    normalCellCenter /= numCell - count[sickGroup];
	    
	    for (int i=0; i < numCell; i++) {
	    	Point3 circ = new Point3(tmpCircles.get(0, i));
	    	Point center = new Point(circ.x, circ.y);
	    	
	    	double group = labels.get(i,0)[0];
	    	if (group != sickGroup) {
	    		Core.circle(drawMat, center, 1, new Scalar(0,0,0));
	    		Core.circle(drawMat, center, (int)Math.round(circ.z), new Scalar(0,0,0));
	    	} else {
	    		Core.circle(drawMat, center, 1, new Scalar(255,255,255));
	    		Core.circle(drawMat, center, (int)Math.round(circ.z), new Scalar(255,255,255));
	    	}
	    }
	    sickProb = 100*count[sickGroup]/numCell;
	    System.out.println("Percentage of possible iron deficient cells: " + String.valueOf(sickProb) + "%");

	    return (sickProb > probThresh && maxCenter - normalCellCenter > centerThresh);
	}
	
	public static boolean findIronDeficiencyCells2(Mat mat, Mat drawMat) {
		Mat tmpCircles = new Mat();
		int[] radRange = {6, 10};
		int[] thresh = {50, 10};
	    double probThresh = 40;
	    double idiffThresh = 6;
	    double sickProb;
	    int numCell;
	    int numSickCell = 0;
		
	    Imgproc.GaussianBlur(mat, mat, new Size(9,9), 0.1, 0.1);
	    Imgproc.HoughCircles(mat, tmpCircles, Imgproc.CV_HOUGH_GRADIENT, 1, radRange[0],
	    		thresh[0], thresh[1], radRange[0], radRange[1]);
	    numCell = (int)tmpCircles.size().width;
	    Mat idiff = new Mat(numCell, 1, CvType.CV_32FC1);
	    getCircIDiff(idiff, tmpCircles, mat);
	    
	    for (int i=0; i<numCell; i++) {
	    	Point3 circ = new Point3(tmpCircles.get(0, i));
	    	Point center = new Point(circ.x, circ.y);
	    	
	    	if (idiff.get(i,0)[0] > idiffThresh) {
	    		Core.circle(drawMat, center, 2, new Scalar(0,0,0));
	    		Core.circle(drawMat, center, (int)Math.round(circ.z), new Scalar(0,0,0));
	    		numSickCell += 1;
	    	}
	    }
	    sickProb = 100*numSickCell/numCell;
	    System.out.println("Percentage of possible iron deficient cells: " + String.valueOf(sickProb) + "%");

	    return (sickProb > probThresh);
	}
	
	public static void getCircIDiff(Mat idiff, Mat circles, Mat mat) {
		int rows = mat.rows();
		int cols = mat.cols();
		for (int i=0; i < circles.size().width; i++) {
	    	Point3 circ = new Point3(circles.get(0, i));
	    	double bigRad = circ.z/Math.sqrt(2);
	    	double smallRad = circ.z/2;
	    	Mat bigSquare = mat.submat(norm(circ.y-bigRad, rows), norm(circ.y+bigRad, rows),
	    			norm(circ.x-bigRad, cols), norm(circ.x+bigRad, cols));
	    	Mat smallSquare = mat.submat(norm(circ.y-smallRad, rows), norm(circ.y+smallRad, rows),
	    			norm(circ.x-smallRad, cols), norm(circ.x+smallRad, cols));
	    	
	    	double outerMean = (Core.sumElems(bigSquare).val[0] - Core.sumElems(smallSquare).val[0]) / 
	    			(bigSquare.rows()*bigSquare.cols() - smallSquare.rows()*smallSquare.cols());
	    	double innerMean = Core.mean(smallSquare).val[0];
	    	
	    	idiff.put(i, 0, innerMean - outerMean);
	    }
	}
	
	public static void drawCircles(Mat circles, Mat mat) {
		for (int i=0; i < circles.size().width; i++) {
	    	Point3 circ = new Point3(circles.get(0, i));
	    	Point center = new Point(circ.x, circ.y);
	    	
    		Core.circle(mat, center, 3, new Scalar(0,0,0));
    		Core.circle(mat, center, (int)Math.round(circ.z), new Scalar(0,0,0));
    	
    		System.out.println("x: " + String.valueOf(circ.x) + ", y: " + String.valueOf(circ.y) + 
    				", r: " + String.valueOf(circ.z));
	    }
	    System.out.println("Num cells: " + String.valueOf(circles.size().width));
	}
	
	public static int getCircMean(Mat circles, Mat mat) {
		int rows = mat.rows();
		int cols = mat.cols();
		int circMean = 0;
		for (int i=0; i < circles.size().width; i++) {
	    	Point3 circ = new Point3(circles.get(0, i));
	    	Mat square = mat.submat(norm(circ.y-circ.z, rows), norm(circ.y+circ.z, rows),
	    			norm(circ.x-circ.z, cols), norm(circ.x+circ.z, cols));
	    	circMean += Core.mean(square).val[0];
	    }
		circMean /= circles.size().width;
		return circMean;
	}
	
	public static void getCircIntensities(Mat intensities, Mat circles, Mat mat) {
		int rows = mat.rows();
		int cols = mat.cols();
		for (int i=0; i < circles.size().width; i++) {
	    	Point3 circ = new Point3(circles.get(0, i));
	    	double rad = circ.z/3;
	    	Mat square = mat.submat(norm(circ.y-rad, rows), norm(circ.y+rad, rows),
	    			norm(circ.x-rad, cols), norm(circ.x+rad, cols));
	    	intensities.put(i, 0, Core.mean(square).val[0]);
	    }
	}
	
	public static int removeFalsePos(Mat circles, Mat newCircles, Mat mat, int circMean) {
		int falsePosThresh = 30;
		int rows = mat.rows();
		int cols = mat.cols();
		int numCell = 0;
		for (int i=0; i < circles.size().width; i++) {
	    	double[] circle = circles.get(0, i);
	    	Point3 circ = new Point3(circle);
	    	Mat square = mat.submat(norm(circ.y-circ.z, rows), norm(circ.y+circ.z, rows),
	    			norm(circ.x-circ.z, cols), norm(circ.x+circ.z, cols));
	    	double circColor = Core.mean(square).val[0];
	    	if (Math.abs(circMean-circColor) < falsePosThresh) {
	    		newCircles.put(0, numCell, circle);
	    		numCell += 1;
	    	}
	    }
		return numCell;
	}
	
	public static int norm(double val, double boundary) {
		val = Math.max(0, val);
		val = Math.min(boundary - 1, val);
		return (int)val;
	}

}
