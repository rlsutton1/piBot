package au.com.rsutton.cv;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Test;

public class FastRGBTest
{

	@Test
	public void test() throws IOException, InterruptedException
	{
		Thread.sleep(5000);
		File img = new File("src/test/resources/IMAG0411.jpg");
		BufferedImage in = ImageIO.read(img);

		FastRGB fast = new FastRGB(in);

		for (int x = 0; x < in.getWidth(); x++)
		{
			for (int y = 0; y < in.getHeight(); y++)
			{
				assertTrue("at " + x + "," + y + " Expected " + in.getRGB(x, y)
						+ " got " + fast.getRGB(x, y),
						in.getRGB(x, y) == fast.getRGB(x, y));
			}
		}
	}

	static BufferedImage deepCopy(BufferedImage bi)
	{
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}

}
