package com.example.smartlab;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.view.View.OnTouchListener;

import org.opencv.imgproc.Imgproc;

public class MainActivity extends Activity {
	
	private static double aneamiaProb;
	
	/* Holds the count of number of times user has touched the screen - used for user input of RBC size */
	private static int touchCount = 0;
	
	/* Screen Size */
	private static int width;
	private static int height;
	
	/* Image width */
	private static int imageWidth;
	
	/* Seekbar for RBC size range selection */
	private SeekBar rbcSizeSeeker = null;
	
	/* Variables for (x,y) coordinates when drawing a line to specify RBC relative size */
	private static float x1;
	private static float x2;
	private static float y1;
	private static float y2;
	
	/* Bitmap image */
	private static Bitmap bmp;
	
	private static Canvas canvas;
	private static ImageView imView;
	
	
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
 	   
 	   /* Determine size of the screen - Using Depreciated Methods*/
 	   Display display = getWindowManager().getDefaultDisplay();
 	   width = display.getWidth();
 	   height = display.getHeight();
 	   
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
		    
		    byte[] byteArray = null;
		    //for non-greyscale images
		    if(true){
			    int bytes = bmp32.getWidth()*bmp32.getHeight()*4;
			    ByteBuffer buffer = ByteBuffer.allocate(bytes);
			    bmp32.copyPixelsToBuffer(buffer);
			    byteArray = buffer.array();
		    }
		    
		    
		    mat.put(0, 0, byteArray);
	    	//Imgproc.cvtColor(mat,  gray,  Imgproc.COLOR_RGB2GRAY);
		    gray.put(0, 0, byteArray);
		    //Mat circles = new Mat();  
		    
		    Imgproc.cvtColor(mat,  gray,  Imgproc.COLOR_RGB2GRAY);
		    
		    boolean isSick = findIronDeficiencyCells(gray,mat,3);
		    
		    Imgproc.cvtColor(mat,  mat,  Imgproc.COLOR_RGB2BGR);
		    byte[] outData = new byte[mat.rows()*mat.cols()*(int)(mat.elemSize())];
			mat.get(0, 0, outData);
			
			bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
			Utils.matToBitmap(mat, bmp);
			
			setContentView(R.layout.image_view);
			imView = (ImageView) findViewById(R.id.image);
			rbcSizeSeeker = (SeekBar) findViewById(R.id.seek1);
			rbcSizeSeeker.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
	            int progressChanged = 0;
	 
	            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
	                progressChanged = progress;
	                //canvas = new Canvas(bmp); //No idea if this will work
	                drawCircleAtPoint(40,40, (progressChanged/2.0), canvas);
	            }
	 
	            public void onStartTrackingTouch(SeekBar seekBar) {
	                // TODO Auto-generated method stub
	            }
	 
	            public void onStopTrackingTouch(SeekBar seekBar) {
	                //drawCircleAtPoint(40,40, (progressChanged/2.0), canvas);
	            }
	        });
			
			addImageTouchListener();
			addListenerOnButton();
			TextView count = (TextView) findViewById(R.id.count);
			TextView result = (TextView) findViewById(R.id.result);
			Button runAgainBtn = (Button) findViewById(R.id.runAgain);

			
			try {
				imView.setImageBitmap(bmp);
				imageWidth = bmp.getWidth();
				canvas = new Canvas(bmp);
				
				
				Paint paint = new Paint();
			    paint.setColor(Color.RED);
			    paint.setStyle(Paint.Style.STROKE);
			    canvas.drawCircle(100, 40, 10, paint);
			    
			    
				count.setText(" Percentage of possible iron deficient cells:  " + aneamiaProb + "%");
				if(isSick){
					result.setText(" Iron Deficiency Anemia detected.");
					result.setTextColor(Color.RED);
				}else{
					result.setText(" No sign of Anemia detected.");
					result.setTextColor(Color.GREEN);
				}
				runAgainBtn.setText("Analyze Another Sample");
			} catch (Exception e) {
			       Log.e("Error", "Error occured");
			       e.printStackTrace();
			}
			
			
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
				touchCount = 0;
			}
 
		});
 
	}
	
	public void addButtonListener() {
		 
		Button button = (Button) findViewById(R.id.countRBCs);
		button.setOnClickListener(new OnClickListener() {
 
			@Override
			public void onClick(View arg0) {
				photoPicker();
				touchCount = 0;
			}
 
		});
 
	}
	
	public void addImageTouchListener(){
		imView.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View arg0, MotionEvent me) {
				/*
				 * Handling for User Inputed RBC size.
				 * User touches two sides of the RBC to identify the diameter of the RBC on the image displayed by the phone
				 * We then use this diameter to set our parameters for the image processing algorithms.
				 */
				if(touchCount == 0){
					x1 = me.getX();
					y1 = me.getY();
					touchCount++;
				}else if(touchCount == 1){
					if(me.getX() == x1 && me.getY() == y1){
						//No-op: Removes double click bug
					}else{
						x2 = me.getX();
						y2 = me.getY();
						touchCount++; //don't want it to loop
						validateUserRBCSizeInput();
					}
				}
				return true;
			}
			
		});
	}
	
	public void validateUserRBCSizeInput(){
		Paint paint = new Paint();
	    paint.setColor(Color.RED);
	    
	    //Distance formula to compute actual cell size
	    double x = Math.pow(x2 - x1, 2);
	    double y = Math.pow(y2 - y1, 2);
	    double distance = Math.sqrt(x + y);
	    Log.i("Distance", distance+ "");
	    
	    //Scale distance to account for fact that we are displaying image at not its true width
	    double scale = (double)imageWidth / width;
	    distance = distance * scale;
	    Log.i("Distance Scaled", distance+ "");
	    
	    //Line drawing not working
	    canvas.drawLine(x1, y1, x2, y2, paint); 
	    imView.invalidate(); //force the view to draw
	    //TODO: Prompt user to confirm the line is accurate. If yes, do analysis, if no repeat
	}
	
	public void drawCircleAtPoint(int x, int y, double r, Canvas c){
		Log.i("Paint", "This was called");
		//canvas.drawBitmap(bmp);
		Paint paint = new Paint();
		paint.setColor(Color.RED);
	    paint.setStyle(Paint.Style.STROKE);
		canvas.drawCircle(x, y, (float) r, paint);
		imView.invalidate();
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
		int[] radRange = {16, 20};
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
		
//	    Imgproc.GaussianBlur(mat, mat, new Size(9,9), 0.1, 0.1);
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
	    		Log.i("Circle Radius", (int)Math.round(circ.z) + "");
	    		Core.circle(drawMat, center, (int)Math.round(circ.z), new Scalar(255,255,255));
	    	}
	    }
	    sickProb = 100*count[sickGroup]/numCell;
	    System.out.println("Percentage of possible iron deficient cells: " + String.valueOf(sickProb) + "%");
	    aneamiaProb = sickProb; //changed to numCell to just output count for time being
	    //System.out.println(numCell);

	    return  (sickProb > probThresh && maxCenter - normalCellCenter > centerThresh);
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
