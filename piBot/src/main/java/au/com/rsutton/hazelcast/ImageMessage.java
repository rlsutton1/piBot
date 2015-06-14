package au.com.rsutton.hazelcast;

import java.awt.image.BufferedImage;

import au.com.rsutton.cv.RangeFinderConfiguration;

public class ImageMessage extends MessageBase<ImageMessage>
{

	private static final long serialVersionUID = -9199163884148073550L;
	ImageWrapper firstImage;
	private RangeFinderConfiguration rangeFinderConfig;

	public ImageMessage(BufferedImage firstImage,
			RangeFinderConfiguration rangeFinderConfig)
	{
		super(HcTopic.IMAGE);
		this.firstImage = new ImageWrapper(firstImage);
		this.rangeFinderConfig = rangeFinderConfig;
	}

	public ImageMessage()
	{
		super(HcTopic.IMAGE);
	}

	public void setTopic()
	{

		this.topic = HazelCastInstance.getInstance().getTopic(
				HcTopic.IMAGE.toString());
	}

	public RangeFinderConfiguration getRangeFinderConfig()
	{
		return rangeFinderConfig;
	}

	public BufferedImage getImage()
	{
		return firstImage.getImage();
	}

}
