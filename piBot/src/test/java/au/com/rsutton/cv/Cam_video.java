package au.com.rsutton.cv;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

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

import com.pi4j.gpio.extension.pixy.PixyCoordinate;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.hazelcast.RobotLocation;
import au.com.rsutton.mapping.CoordResolver;

public class Cam_video
{

//	v4l2-ctl -d /dev/video0 --list-framesizes=YUYV
//	ioctl: VIDIOC_ENUM_FRAMESIZES
//        Size: Discrete 640x480
//        Size: Discrete 160x120
//        Size: Discrete 176x144
//        Size: Discrete 320x176
//        Size: Discrete 320x240
//        Size: Discrete 352x288
//        Size: Discrete 432x240
//        Size: Discrete 544x288
//        Size: Discrete 640x360
//        Size: Discrete 752x416
//        Size: Discrete 800x448
//        Size: Discrete 800x600
//        Size: Discrete 864x480
//        Size: Discrete 960x544
//        Size: Discrete 960x720
//        Size: Discrete 1024x576
//        Size: Discrete 1184x656
//        Size: Discrete 1280x720
//        Size: Discrete 1280x960

	
	
	public static void main(String[] args)
	{
		try
		{
			// Create canvas frame for displaying video.
			CanvasFrame canvas = new CanvasFrame("VideoCanvas");
			// canvas.setSize(800, 600);
			// Set Canvas frame to close on exit
			canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
			// Declare FrameGrabber to import video from "video.mp4"
			// FrameGrabber grabber = new OpenCVFrameGrabber(
			// "http://192.168.0.107:8080/video");

			// FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(
			// "/home/rsutton/VIDEO0015.mp4");

			FrameGrabber grabber = new OpenCVFrameGrabber("");
	
			
			
			grabber.setImageWidth(544);
			grabber.setImageHeight( 288);
			
			
			//grabber.setImageWidth(432);
			//grabber.setImageHeight( 240);
			
			//grabber.setImageWidth(852);
			//grabber.setImageHeight( 480);

			// IPCameraFrameGrabber grabber = new
			// IPCameraFrameGrabber("http://192.168.0.107:8080/video");
			grabber.start();

			// VideoCapture grbber = new
			// VideoCapture("http://192.168.0.107:8080/video");
			// Mat mat = new Mat(640,480,opencv_core.CV_8UC1);

			// CvCapture grabber =
			// opencv_highgui.cvCaptureFromFile("http://192.168.0.107:8080/video");

			// grabber.setFormat("avi");

			ImageProcessorV4 processor = new ImageProcessorV4();
			// Start grabber to capture video
			// grabber.start();

			boolean firstFrame = true;
			// Declare img as IplImage

			grabber.setFrameRate(30);

			grabber.setNumBuffers(1);
			grabber.setTriggerMode(true);

			long lastTimeStamp = 0;

			IplImage img;
			while (true)
			{
				Thread.sleep(100);

				grabber.trigger();
				// inser grabed video fram to IplImage img
				img = grabber.grab();// opencv_highgui.cvQueryFrame(grabber);
				// grbber.retrieve(mat);
				// img = mat.asIplImage();
				if (firstFrame)
				{
					// Set canvas size as per dimentions of video frame.
					canvas.setCanvasSize(img.width(), img.height());
					// canvas.setCanvasSize(640,480);
					firstFrame = false;
				}
				if (img != null)
				{

					RobotLocation messageObject = new RobotLocation();
					messageObject.setHeading(0);
					messageObject.setX(new Distance(0,DistanceUnit.CM));
					messageObject.setY(new Distance(0,DistanceUnit.CM));
					
					Collection<PixyCoordinate> rangeData = new LinkedList<>();
					CoordResolver resolver = new CoordResolver();
					Map<Integer,Integer> data = processor.processImage(img);
					for (Entry<Integer, Integer> value:data.entrySet())
					{
						System.out.println(resolver.convertYtoRange( value.getValue()));
						rangeData.add(new PixyCoordinate(value.getKey(), value.getValue()));
					}
					
					messageObject.setLaserData(rangeData );
					messageObject.publish();

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