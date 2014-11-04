package au.com.rsutton.cv;

import static org.junit.Assert.fail;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Test;

public class ImageProcessorV4Test
{

	@Test
	public void test() throws IOException, InterruptedException
	{
		Thread.sleep(5000);
		File img=new File("src/test/resources/IMAG0411.jpg");
		BufferedImage in = ImageIO.read(img);

		ImageProcessorV4 processor = new ImageProcessorV4();
		
		for (int i = 0;i < 1000;i++)
		{
			BufferedImage tmp = deepCopy(in);
			processor.processImage(tmp);
		}
	}
	
	static BufferedImage deepCopy(BufferedImage bi) {
		 ColorModel cm = bi.getColorModel();
		 boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		 WritableRaster raster = bi.copyData(null);
		 return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
		}

}
