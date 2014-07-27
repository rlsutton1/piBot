package au.com.rsutton.cv;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameGrabber;

public class Demo_video
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
			// FrameGrabber grabber = new OpenCVFrameGrabber(
			// "/home/rsutton/VIDEO0015.mp4");

			FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(
					"/home/rsutton/VIDEO0015.mp4");

			
						
			
			// grabber.setFormat("avi");

			ImageProcessor processor = new ImageProcessor();
			// Start grabber to capture video
			grabber.start();

			boolean firstFrame = true;
			// Declare img as IplImage

			IplImage img;
			while (true)
			{
				// inser grabed video fram to IplImage img
				img = grabber.grab();
				if (firstFrame)
				{
					// Set canvas size as per dimentions of video frame.
					canvas.setCanvasSize(grabber.getImageWidth(),
							grabber.getImageHeight());
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