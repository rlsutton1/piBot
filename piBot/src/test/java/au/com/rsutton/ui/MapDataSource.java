package au.com.rsutton.ui;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.List;

public interface MapDataSource
{

	List<Point> getPoints();

	void drawPoint(BufferedImage image, double pointOriginX, double pointOriginY, double scale);


}
