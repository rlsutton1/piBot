package au.com.rsutton.hazelcast;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.imageio.ImageIO;

import au.com.rsutton.cv.RangeFinderConfiguration;

public class ImageWrapper implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4932667661952274901L;
	transient BufferedImage image;

	public ImageWrapper(BufferedImage image)
	{
		this.image = image;
	}

	public ImageWrapper()
	{
	
	}


	public BufferedImage getImage()
	{
		return image;
	}


	
	private void writeObject(ObjectOutputStream out) throws IOException
	{
		out.defaultWriteObject();

		ImageIO.write(image, "png", out); // png is lossless

	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException
	{
		in.defaultReadObject();
		image = ImageIO.read(in);
	}
}
