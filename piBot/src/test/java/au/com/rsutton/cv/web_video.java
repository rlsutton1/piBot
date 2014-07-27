package au.com.rsutton.cv;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_highgui;
import org.bytedeco.javacpp.opencv_highgui.CvCapture;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.IPCameraFrameGrabber;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.javacpp.opencv_highgui.VideoCapture;

public class web_video
{
	public static void main(String[] args)
	{
		try
		{
			// Create canvas frame for displaying video.
			CanvasFrame canvas = new CanvasFrame("VideoCanvas");
	//		canvas.setSize(800, 600);
			// Set Canvas frame to close on exit
			canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
			// Declare FrameGrabber to import video from "video.mp4"
//			 FrameGrabber grabber = new OpenCVFrameGrabber(
//					 "http://192.168.0.107:8080/video");

//			FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(
	//				"/home/rsutton/VIDEO0015.mp4");

			
			IPCameraFrameGrabber grabber = new IPCameraFrameGrabber("http://192.168.0.107:8080/video");
			grabber.start();
			
//			VideoCapture grbber = new VideoCapture("http://192.168.0.107:8080/video");
//			Mat mat = new Mat(640,480,opencv_core.CV_8UC1);
			
//			CvCapture grabber =  opencv_highgui.cvCaptureFromFile("http://192.168.0.107:8080/video");
						
			
			// grabber.setFormat("avi");

			ImageProcessorV3 processor = new ImageProcessorV3();
			// Start grabber to capture video
//			grabber.start();

			boolean firstFrame = true;
			// Declare img as IplImage

			IplImage img;
			while (true)
			{
				// inser grabed video fram to IplImage img
				img = grabber.grab();//			opencv_highgui.cvQueryFrame(grabber); 
				//grbber.retrieve(mat);
				//img = mat.asIplImage();
				if (firstFrame)
				{
					// Set canvas size as per dimentions of video frame.
					canvas.setCanvasSize(640,480);
					firstFrame = false;
				}
				if (img != null)
				{
					processor.processImage(img);
					// Show video frame in canvas
					canvas.showImage(img);
					// Thread.sleep(100);
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}