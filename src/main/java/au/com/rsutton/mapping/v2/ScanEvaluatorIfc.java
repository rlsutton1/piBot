package au.com.rsutton.mapping.v2;

import java.util.List;

import au.com.rsutton.mapping.XY;

public interface ScanEvaluatorIfc
{

	/**
	 * look for at least 6 consecutive points forming one or more lines
	 * 
	 * @param scanPoints
	 * @return
	 */
	public abstract List<Line> findLines(List<XY> scanPoints);

}