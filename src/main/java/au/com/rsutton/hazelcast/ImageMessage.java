package au.com.rsutton.hazelcast;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageMessage extends MessageBase<ImageMessage>
{

	private static final long serialVersionUID = 938950572432708619L;

	private long time = System.currentTimeMillis();

	private byte[] bytes;

	public ImageMessage()
	{
		super(HcTopic.IMAGE);

	}

	public void setTopic()
	{

		this.topicInstance = HazelCastInstance.getInstance().getTopic(HcTopic.IMAGE.toString());
	}

	public long getTime()
	{
		return time;
	}

	public void setTime(long time)
	{
		this.time = time;
	}

	public void setImage(BufferedImage image) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, "png", baos);
		bytes = baos.toByteArray();
	}

	public BufferedImage getImage() throws IOException
	{
		return ImageIO.read(new ByteArrayInputStream(bytes));
	}

	public byte[] getBytes()
	{
		return bytes;
	}

	public void setBytes(byte[] bytes)
	{
		this.bytes = bytes;
	}

}
