package au.com.rsutton.cv;

import static org.junit.Assert.fail;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.junit.Test;

import com.hazelcast.core.MessageListener;

import au.com.rsutton.hazelcast.ImageMessage;
import au.com.rsutton.mapping.CoordResolver;
import au.com.rsutton.mapping.ImageWatcher;

public class ImageProcessorV4Test extends JPanel
{

	volatile private int finder;
	volatile BufferedImage imageToDraw;

	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.drawImage(imageToDraw, 0, 0,800,600, this);

	}

	

	@Test
	public void test() throws IOException, InterruptedException
	{
		
		System.out.println("Starting image watcher");
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		f.add(this);
		f.setSize(400, 400);
		f.setLocation(200, 200);
		f.setVisible(true);
		f.setTitle("Image watcher");
		
//		Thread.sleep(5000);
		File img = new File("src/test/resources/IMAG0411.jpg");
		BufferedImage in = ImageIO.read(img);

		// TODO: this is broken
		RangeFinderConfiguration config = new RangeFinderConfiguration();
		CoordResolver coordResolver = new CoordResolver(config);
		ImageProcessorV5 processor = new ImageProcessorV5(coordResolver);

		for (int i = 0; i < 1; i++)
		{
			BufferedImage tmp = deepCopy(in);
			Map<Integer, Integer> data = processor.processImage(tmp);
			for (Entry<Integer, Integer> entry : data.entrySet())
			{
				System.out.println(entry.getKey() + " " + entry.getValue());
			}
			System.out.println(i);
			imageToDraw = tmp;
			repaint();
		}
		
		Thread.sleep(60000);
		
	}

	static BufferedImage deepCopy(BufferedImage bi)
	{
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}

}
