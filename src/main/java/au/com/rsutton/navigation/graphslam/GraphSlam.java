package au.com.rsutton.navigation.graphslam;

import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

public class GraphSlam
{

	RealMatrix omega = MatrixUtils.createRealMatrix(1, 1);

	RealMatrix p = MatrixUtils.createRealMatrix(1, 1);

	GraphSlam(double value)
	{
		omega.setEntry(0, 0, 1.0);
		p.setEntry(0, 0, value);
	}

	public RealMatrix getPositions()
	{
		System.out.println("Omega before inverse: ");
		dumpMatrix(omega);
		RealMatrix omegaInverse = inv(omega);
		return omegaInverse.multiply(p);
	}

	public void dumpAllData()
	{
		System.out.println("Omega: ");
		dumpMatrix(omega);
		System.out.println("P: ");
		dumpMatrix(p);
	}

	public void dumpPositions()
	{
		System.out.println("Positions: (current location followed by landmarks)");
		dumpMatrix(getPositions());
	}

	public void addMove(double distanceMoved, double certainty)
	{

		omega = insertRow(omega, 1);
		omega = insertCol(omega, 1);

		// insert new row at position 1 into
		p = insertRow(p, 1);

		update(1, distanceMoved, certainty);

		collapseRobotPath();

	}

	private void collapseRobotPath()
	{

		RealMatrix a = omega.getSubMatrix(0, 0, 1, omega.getColumnDimension() - 1);
		RealMatrix b = omega.getSubMatrix(0, 0, 0, 0);
		RealMatrix c = p.getSubMatrix(0, 0, 0, 0);

		RealMatrix oo = omega.getSubMatrix(1, omega.getRowDimension() - 1, 1, omega.getColumnDimension() - 1);
		RealMatrix pp = p.getSubMatrix(1, p.getRowDimension() - 1, 0, 0);

		// o = o'-At.B-1.A
		// p = p'-At.B-1.C

		omega = oo.subtract(a.transpose().multiply(inv(b).multiply(a)));
		p = pp.subtract(a.transpose().multiply(inv(b).multiply(c)));

	}

	RealMatrix inv(RealMatrix matrix)
	{

		DecompositionSolver solver = new SingularValueDecomposition(matrix).getSolver();
		return solver.getInverse();

	}

	/**
	 * larger values of certainty indicate that the measurement is more certain
	 * (less error).
	 * 
	 * 1 would be normal certainty
	 * 
	 * 5 would be very certain
	 * 
	 * 0.2 would be very uncertain
	 * 
	 * @param position
	 * @param distanceToLandmark
	 * @param certainty
	 */
	public void update(int position, double distanceToLandmark, double certainty)
	{
		// certainty = 1/sigma
		// row, col
		omega.addToEntry(0, 0, 1 * certainty);
		omega.addToEntry(0, position, -1 * certainty);
		omega.addToEntry(position, 0, -1 * certainty);
		omega.addToEntry(position, position, 1 * certainty);

		p.addToEntry(0, 0, -distanceToLandmark * certainty);
		p.addToEntry(position, 0, distanceToLandmark * certainty);

	}

	public int add(double distanceToLandmark, double certainty)
	{
		omega = addCol(omega);
		omega = addRow(omega);
		p = addRow(p);
		int landmarkNumber = omega.getColumnDimension() - 1;
		update(landmarkNumber, distanceToLandmark, certainty);
		System.out.println(landmarkNumber);
		return landmarkNumber;

	}

	private RealMatrix addRow(RealMatrix matrix)
	{
		RealMatrix result = MatrixUtils.createRealMatrix(matrix.getRowDimension() + 1, matrix.getColumnDimension());
		for (int col = 0; col < matrix.getColumnDimension(); col++)
		{
			for (int row = 0; row < matrix.getRowDimension(); row++)
			{

				result.setEntry(row, col, matrix.getEntry(row, col));
			}
		}

		return result;
	}

	private RealMatrix addCol(RealMatrix matrix)
	{
		RealMatrix result = MatrixUtils.createRealMatrix(matrix.getRowDimension(), matrix.getColumnDimension() + 1);
		for (int col = 0; col < matrix.getColumnDimension(); col++)
		{
			for (int row = 0; row < matrix.getRowDimension(); row++)
			{

				result.setEntry(row, col, matrix.getEntry(row, col));
			}
		}

		return result;
	}

	private RealMatrix insertCol(RealMatrix matrix, int pos)
	{
		RealMatrix result = MatrixUtils.createRealMatrix(matrix.getRowDimension(), matrix.getColumnDimension() + 1);

		int offset = 0;
		for (int col = 0; col < matrix.getColumnDimension(); col++)
		{
			if (col == pos)
			{
				offset = 1;
			}
			for (int row = 0; row < matrix.getRowDimension(); row++)
			{

				result.setEntry(row, col + offset, matrix.getEntry(row, col));
			}
		}
		return result;
	}

	private RealMatrix insertRow(RealMatrix matrix, int pos)
	{
		RealMatrix result = MatrixUtils.createRealMatrix(matrix.getRowDimension() + 1, matrix.getColumnDimension());

		for (int col = 0; col < matrix.getColumnDimension(); col++)
		{
			int offset = 0;
			for (int row = 0; row < matrix.getRowDimension(); row++)
			{
				if (row == pos)
				{
					offset = 1;
				}
				result.setEntry(row + offset, col, matrix.getEntry(row, col));
			}
		}
		return result;
	}

	private void dumpMatrix(RealMatrix matrix)
	{
		for (int row = 0; row < matrix.getRowDimension(); row++)
		{
			for (int col = 0; col < matrix.getColumnDimension(); col++)
			{

				System.out.print(matrix.getEntry(row, col) + ",  ");
			}
			System.out.println("");
		}

	}
}
