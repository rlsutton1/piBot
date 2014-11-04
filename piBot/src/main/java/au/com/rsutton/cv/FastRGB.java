package au.com.rsutton.cv;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class FastRGB
{

	private int width;
	private boolean hasAlphaChannel;
	private int pixelLength;
	private byte[] pixels;

	FastRGB(BufferedImage image)
	{

		pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		width = image.getWidth();
		hasAlphaChannel = image.getAlphaRaster() != null;
		pixelLength = 3;
		if (hasAlphaChannel)
		{
			pixelLength = 4;
		}

	}

	int getRGB(int x, int y)
	{
		int pos = (y * pixelLength * width) + (x * pixelLength);

		int argb = -16777216; // 255 alpha
		if (hasAlphaChannel)
		{
			argb = (((int) pixels[pos++] & 0xff) << 24); // alpha
		}

		argb += ((int) pixels[pos++] & 0xff); // blue
		argb += (((int) pixels[pos++] & 0xff) << 8); // green
		argb += (((int) pixels[pos++] & 0xff) << 16); // red
		return argb;

	}
}
