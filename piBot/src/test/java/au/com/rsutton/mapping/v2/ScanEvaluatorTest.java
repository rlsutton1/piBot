package au.com.rsutton.mapping.v2;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.omg.PortableInterceptor.SUCCESSFUL;

import au.com.rsutton.mapping.XY;

public class ScanEvaluatorTest
{

	@Test
	public void testPerfectLineAlongX()
	{
		ScanEvaluatorIfc evaluator = new ScanEvaluator();

		List<XY> scanPoints = new LinkedList<>();
		scanPoints.add(new XY(10, 1));
		scanPoints.add(new XY(11, 1));
		scanPoints.add(new XY(12, 1));
		scanPoints.add(new XY(13, 1));
		scanPoints.add(new XY(14, 1));
		scanPoints.add(new XY(15, 1));
		List<Line> lines = evaluator.findLines(scanPoints);

		assertTrue("got " + lines.size() + ", expected 1", lines.size() == 1);
		Line line = lines.get(0);
		assertTrue(line.getStart().getX() == 10 && line.getStart().getY() == 1);
		assertTrue("got " + line.getEnd() + " expected 15,1", line.getEnd()
				.getX() == 15 && line.getEnd().getY() == 1);

	}

	@Test
	public void testPerfectLineAlongY()
	{
		ScanEvaluatorIfc evaluator = new ScanEvaluator();

		List<XY> scanPoints = new LinkedList<>();
		scanPoints.add(new XY(1, 10));
		scanPoints.add(new XY(1, 11));
		scanPoints.add(new XY(1, 12));
		scanPoints.add(new XY(1, 13));
		scanPoints.add(new XY(1, 14));
		scanPoints.add(new XY(1, 15));
		List<Line> lines = evaluator.findLines(scanPoints);

		assertTrue("got " + lines.size() + ", expected 1", lines.size() == 1);
		Line line = lines.get(0);
		assertTrue(line.getStart().getX() == 1 && line.getStart().getY() == 10);
		assertTrue("got " + line.getEnd() + " expected 1,15", line.getEnd()
				.getX() == 1 && line.getEnd().getY() == 15);

	}

	@Test
	public void testPerfectDiagonalLine()
	{
		ScanEvaluatorIfc evaluator = new ScanEvaluator();

		List<XY> scanPoints = new LinkedList<>();
		scanPoints.add(new XY(0, 0));
		scanPoints.add(new XY(1, 1));
		scanPoints.add(new XY(2, 2));
		scanPoints.add(new XY(3, 3));
		scanPoints.add(new XY(4, 4));
		scanPoints.add(new XY(5, 5));
		List<Line> lines = evaluator.findLines(scanPoints);

		assertTrue("got " + lines.size() + ", expected 1", lines.size() == 1);
		Line line = lines.get(0);
		assertTrue(line.getStart().getX() == 0 && line.getStart().getY() == 0);
		assertTrue("got " + line.getEnd() + " expected 5,5", line.getEnd()
				.getX() == 5 && line.getEnd().getY() == 5);

	}

	@Test
	public void testTwoDiagonalLines()
	{
		ScanEvaluatorIfc evaluator = new ScanEvaluator();

		List<XY> scanPoints = new LinkedList<>();
		scanPoints.add(new XY(0, 0));
		scanPoints.add(new XY(1, 1));
		scanPoints.add(new XY(2, 2));
		scanPoints.add(new XY(3, 3));
		scanPoints.add(new XY(4, 4));
		scanPoints.add(new XY(5, 5));

		scanPoints.add(new XY(0, 0));
		scanPoints.add(new XY(1, 1));
		scanPoints.add(new XY(2, 2));
		scanPoints.add(new XY(3, 3));
		scanPoints.add(new XY(4, 4));
		scanPoints.add(new XY(5, 5));

		List<Line> lines = evaluator.findLines(scanPoints);

		assertTrue("got " + lines.size() + ", expected 2", lines.size() == 2);
		Line line = lines.get(0);
		assertTrue(line.getStart().getX() == 0 && line.getStart().getY() == 0);
		assertTrue("got " + line.getEnd() + " expected 5,5", line.getEnd()
				.getX() == 5 && line.getEnd().getY() == 5);

		line = lines.get(1);
		assertTrue(line.getStart().getX() == 0 && line.getStart().getY() == 0);
		assertTrue("got " + line.getEnd() + " expected 5,5", line.getEnd()
				.getX() == 5 && line.getEnd().getY() == 5);

	}

	@Test
	public void testPerfectLineAlongXWithWobble()
	{
		ScanEvaluatorIfc evaluator = new ScanEvaluator();

		List<XY> scanPoints = new LinkedList<>();
		scanPoints.add(new XY(110, 10));
		scanPoints.add(new XY(120, 15));
		scanPoints.add(new XY(130, 10));
		scanPoints.add(new XY(140, 5));
		scanPoints.add(new XY(150, 10));
		scanPoints.add(new XY(160, 10));
		List<Line> lines = evaluator.findLines(scanPoints);

		assertTrue("got " + lines.size() + ", expected 1", lines.size() == 1);
		Line line = lines.get(0);
		assertTrue("got " + line.getStart() + " expected 120,15", line
				.getStart().getX() == 120 && line.getStart().getY() == 15);
		assertTrue("got " + line.getEnd() + " expected 160,10", line.getEnd()
				.getX() == 160 && line.getEnd().getY() == 10);

	}

	@Test
	public void testRealData()
	{
		ScanEvaluatorIfc evaluator = new ScanEvaluator();

		List<XY> scanPoints = new LinkedList<>();
		

		loadSampleData(scanPoints);
		
		List<Line> lines = evaluator.findLines(scanPoints);

	}
	
	@Test
	public void testRealData2()
	{
		ScanEvaluatorIfc evaluator = new ScanEvaluator();

		List<XY> scanPoints = new LinkedList<>();
		

		loadSampleData2(scanPoints);
		
		List<Line> lines = evaluator.findLines(scanPoints);

	}
	
	private void loadSampleData2(List<XY> scanPoints)
	{
		scanPoints.add(new XY(2150,437));
		scanPoints.add(new XY(2070,387));
		scanPoints.add(new XY(445,0));
		scanPoints.add(new XY(445,-8));
		scanPoints.add(new XY(445,-16));
		scanPoints.add(new XY(441,-23));
		scanPoints.add(new XY(444,-23));
		scanPoints.add(new XY(448,-30));
		scanPoints.add(new XY(448,-38));
		scanPoints.add(new XY(450,-63));
		scanPoints.add(new XY(450,-63));
		scanPoints.add(new XY(454,-71));
		scanPoints.add(new XY(453,-86));
		scanPoints.add(new XY(448,-101));
		scanPoints.add(new XY(452,-110));
		scanPoints.add(new XY(451,-134));
		scanPoints.add(new XY(454,-143));
		scanPoints.add(new XY(457,-160));
		scanPoints.add(new XY(453,-159));
		scanPoints.add(new XY(453,-166));
		scanPoints.add(new XY(452,-174));
		scanPoints.add(new XY(456,-183));
		scanPoints.add(new XY(459,-192));
		scanPoints.add(new XY(455,-190));
		scanPoints.add(new XY(456,-198));
		scanPoints.add(new XY(459,-200));
		scanPoints.add(new XY(458,-206));
		scanPoints.add(new XY(458,-215));
		scanPoints.add(new XY(457,-222));
		scanPoints.add(new XY(457,-222));
		scanPoints.add(new XY(461,-233));
		scanPoints.add(new XY(457,-230));
		scanPoints.add(new XY(457,-237));
		scanPoints.add(new XY(461,-239));
		scanPoints.add(new XY(457,-244));
	}

	private void loadSampleData(List<XY> scanPoints)
	{
//		Dumping line data for unit testing...
		scanPoints.add(new XY(768,155));
		scanPoints.add(new XY(768,155));
		scanPoints.add(new XY(775,146));
		scanPoints.add(new XY(782,136));
		scanPoints.add(new XY(782,136));
		scanPoints.add(new XY(787,115));
		scanPoints.add(new XY(787,115));
		scanPoints.add(new XY(797,104));
		scanPoints.add(new XY(785,103));
		scanPoints.add(new XY(794,93));
		scanPoints.add(new XY(804,95));
		scanPoints.add(new XY(803,82));
		scanPoints.add(new XY(810,71));
		scanPoints.add(new XY(808,57));
		scanPoints.add(new XY(819,60));
		scanPoints.add(new XY(817,47));
		scanPoints.add(new XY(829,47));
		scanPoints.add(new XY(826,34));
		scanPoints.add(new XY(835,20));
		scanPoints.add(new XY(844,8));
		scanPoints.add(new XY(844,8));
		scanPoints.add(new XY(841,-5));
		scanPoints.add(new XY(851,-19));
		scanPoints.add(new XY(851,-19));
		scanPoints.add(new XY(860,-34));
		scanPoints.add(new XY(860,-34));
		scanPoints.add(new XY(857,-47));
		scanPoints.add(new XY(857,-47));
		scanPoints.add(new XY(868,-63));
		scanPoints.add(new XY(881,-63));
		scanPoints.add(new XY(878,-78));
		scanPoints.add(new XY(889,-95));
		scanPoints.add(new XY(889,-95));
		scanPoints.add(new XY(885,-110));
		scanPoints.add(new XY(900,-112));
		scanPoints.add(new XY(896,-127));
		scanPoints.add(new XY(912,-128));
		scanPoints.add(new XY(908,-144));
		scanPoints.add(new XY(920,-163));
		scanPoints.add(new XY(928,-198));
		scanPoints.add(new XY(940,-217));
		scanPoints.add(new XY(924,-213));
		scanPoints.add(new XY(958,-221));
		scanPoints.add(new XY(968,-260));
		scanPoints.add(new XY(964,-277));
		scanPoints.add(new XY(982,-281));
		scanPoints.add(new XY(977,-298));
		scanPoints.add(new XY(1023,-370));
		scanPoints.add(new XY(1019,-389));
		scanPoints.add(new XY(1015,-408));
		scanPoints.add(new XY(1052,-443));
		scanPoints.add(new XY(1084,-521));
		scanPoints.add(new XY(1079,-540));
		scanPoints.add(new XY(1074,-560));
		scanPoints.add(new XY(1048,-568));
		scanPoints.add(new XY(1022,-573));
		scanPoints.add(new XY(1001,-562));
		scanPoints.add(new XY(998,-580));
		scanPoints.add(new XY(974,-586));
		scanPoints.add(new XY(951,-592));
		scanPoints.add(new XY(929,-597));
		scanPoints.add(new XY(929,-597));
		scanPoints.add(new XY(908,-601));
		scanPoints.add(new XY(908,-601));
		scanPoints.add(new XY(904,-617));

		// should have only got 3 at most
//		768,155 797,104 V:0.023741015116328743 A:29.623748751173814
//		785,103 808,57 V:0.24190453882697574 A:26.56505117707799
//		819,60 924,-213 V:0.2838629710918178 A:21.037511025421818
//		958,-221 1022,-573 V:0.25035353227943713 A:10.304846468766033
//		1001,-562 904,-617 V:0.14061621935154853 A:-60.44631636769255
	}
}
