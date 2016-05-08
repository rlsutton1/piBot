package au.com.rsutton.mapping.v2;

import java.util.List;

import au.com.rsutton.entryPoint.trig.Point;
import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.mapping.XY;

public class ScanEvaluatorV3 
{
	/**
	 * takes a radial scan of the environment, and identifies lines and
	 * artifacts like corners
	 */

	double requiredLineQuality = 0.20;

	/*
	 * (non-Javadoc)
	 * 
	 * @see au.com.rsutton.mapping.v2.ScanEvaluatorIfc#findLines(java.util.List)
	 */
	
	public List<HoughLine> findLines(List<XY> scanPoints, int xCenter, int yCenter)
	{
		
        HoughTransform h = new HoughTransform(xCenter, yCenter); 
        
        // add the points from the image (or call the addPoint method separately if your points are not in an image 
 
        

		System.out.println("Dumping line data for unit testing...");
		for (XY xy : scanPoints)
		{
			h.addPoint(xy.getX(), xy.getY());
			System.out.println("scanPoints.add(new XY(" + xy + "));");
		}
		System.out.println();

		// get the lines out 
        List<HoughLine> lines = h.getLines(6); 

		for (HoughLine line : lines)
		{
			System.out.println(line);
		}
		return lines;
	}


	private Point getPointFromXY(XY xy)
	{
		return new Point(new Distance(xy.getX(), DistanceUnit.MM),
				new Distance(xy.getY(), DistanceUnit.MM));
	}
}
