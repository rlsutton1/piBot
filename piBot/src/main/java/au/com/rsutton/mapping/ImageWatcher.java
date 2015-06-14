package au.com.rsutton.mapping;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JFrame;
import javax.swing.JPanel;

import au.com.rsutton.cv.ImageProcessorV5;
import au.com.rsutton.hazelcast.ImageMessage;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

public class ImageWatcher extends JPanel implements
		MessageListener<ImageMessage>
{

	volatile private int finder;

	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		if (firstImage.get() != null)
		{
			g2.drawImage(firstImage.get(), 0, 0, this);
		}

	}

	public static void main(String[] args)
	{
		System.out.println("Starting image watcher");
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ImageWatcher graph = new ImageWatcher();
		f.add(graph);
		f.setSize(400, 400);
		f.setLocation(200, 200);
		f.setVisible(true);
		f.setTitle("Image watcher");

		// Thread th = new Thread(graph);
		// th.start();
	}

	public ImageWatcher()
	{
		ImageMessage locationMessage = new ImageMessage();
		locationMessage.addMessageListener(this);

	}

	ImageProcessorV5 processor;
	private CoordResolver coordResolver;
	volatile private AtomicReference<BufferedImage> firstImage = new AtomicReference<BufferedImage>();

	@Override
	public void onMessage(Message<ImageMessage> message)
	{
		ImageMessage messageObject = message.getMessageObject();

		if (finder == 0)
		{
			finder = messageObject.getRangeFinderConfig()
					.getOrientationToRobot();
		} else if (finder != messageObject.getRangeFinderConfig()
				.getOrientationToRobot())
		{
			return;
		}
		coordResolver = new CoordResolver(messageObject.getRangeFinderConfig());

		processor = new ImageProcessorV5(coordResolver);

		System.out.println("Frame received");
		firstImage.set(messageObject.getImage());
		processor.processImage(firstImage.get());

		repaint();

	}

}