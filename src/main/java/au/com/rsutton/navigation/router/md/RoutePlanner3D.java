package au.com.rsutton.navigation.router.md;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class RoutePlanner3D
{

	Step[][][] plan;
	private int maxY;
	private int maxX;

	private int rotationFraction;
	private InternalPose target;

	RoutePlanner3D(int x, int y, int rotations)
	{
		this.maxX = x;
		this.maxY = y;

		this.rotationFraction = 360 / rotations;
		if (360 / rotationFraction != rotations)
		{
			throw new RuntimeException("rotations must be a factor of 360");
		}

		plan = new Step[x][y][rotations];
		for (int i = 0; i < x; i++)
			for (int j = 0; j < y; j++)
				for (int k = 0; k < rotations; k++)
				{
					plan[i][j][k] = new Step();
				}
		System.out.println("expect " + (x * y * rotations));
	}

	class Step
	{
		double cost = Integer.MAX_VALUE;
		MoveTemplate move = null;
	}

	void plan(int x, int y, Angle angle, MoveTemplate[] moveTemplates)
	{

		target = new InternalPose(x, y, angle);
		ProposedPose start = new ProposedPose(x, y, angle, 0);
		List<ProposedPose> work = new LinkedList<>();
		work.add(start);
		int ctr = 0;
		while (!work.isEmpty())
		{
			ctr++;
			ProposedPose job = work.remove(0);
			for (MoveTemplate move : moveTemplates)
			{
				ProposedPose proposedPose = move.getProposedPose(job);
				if (proposedPose.isWithinBounds() && proposedPose.isBetter(plan))
				{
					proposedPose.addToPlan(move, plan);
					work.add(proposedPose);
				}
			}
		}
		System.out.println("steps " + (ctr * moveTemplates.length));

		dumpUnroutable(x, y);

	}

	private void dumpUnroutable(int x, int y)
	{
		int unroutable = 0;
		for (int i = 0; i < x; i++)
			for (int j = 0; j < y; j++)
				for (int k = 0; k < plan[i][j].length; k++)
				{
					if (plan[i][j][k].cost == Integer.MAX_VALUE)
					{
						unroutable++;
						System.out.println("UR " + i + " " + j + " " + rotationToAngle(k));
					}
				}
		System.out.println("Unroutable " + unroutable);
	}

	void dumpMap()
	{
		for (int y = 0; y < maxY; y++)
		{
			for (int x = 0; x < maxX; x++)
			{
				int min = Integer.MAX_VALUE;
				for (int r = 0; r < plan[x][y].length; r++)
				{
					min = (int) Math.min(min, plan[x][y][r].cost);
				}
				if (min == Integer.MAX_VALUE)
				{
					System.out.print(" ");
				} else
				{
					System.out.print(" " + min);
				}
			}
			System.out.println("");
		}
	}

	void dumpFrom(int x, int y, Angle initialAngle)
	{

		int[][] result = new int[maxX][maxY];

		Vector3D position = new Vector3D(x, y, 0);

		InternalPose currentPose = new InternalPose(position.getX(), position.getY(), initialAngle.invert());
		int ctr = 0;
		Step step = null;
		do
		{
			step = currentPose.getStep(plan);

			if (step != null && step.move != null)
			{

				Angle angle = new Angle(currentPose.angle.angle - step.move.angleDelta.angle);
				Vector3D uv = getUnitVector(angle);
				position = position.subtract(uv);

				InternalPose nextPose = new InternalPose(position.getX(), position.getY(), angle);

				if (!nextPose.isWithinBounds() || nextPose.isAtGoal())
				{
					System.out.println("at goal or out of bounds");
					break;
				}

				result[(int) position.getX()][(int) position.getY()] = ctr;// (int)
																			// (nextPose.getCostOfPose(plan));

				currentPose = nextPose;

			}
			ctr++;
			if (ctr > 2000)
			{
				break;
			}

		} while (step != null);

		dumpPath(x, y, result);
	}

	private void dumpPath(int x, int y, int[][] result)
	{
		for (int y1 = 0; y1 < maxY; y1++)

		{
			System.out.print("" + (y1 % 10));
			for (int x1 = 0; x1 < maxX; x1++)
			{
				if (x1 == x && y1 == y)
				{
					System.out.print("S");
				} else if (x1 == (int) target.x && y1 == (int) target.y)
				{
					System.out.print("T");
				} else if (result[x1][y1] > 0)
				{
					System.out.print(".");
				} else
				{
					System.out.print(" ");
				}
			}
			System.out.println("");
		}
	}

	int rotationToAngle(int rotation)
	{
		return rotation * rotationFraction;
	}

	Angle angleFactory(int degrees)
	{
		return new Angle(degrees);
	}

	class Angle
	{
		private final int angle;

		Angle(int angle)
		{
			int tmp = angle % 360;
			if (tmp < 0)
			{
				tmp += 360;
			}

			this.angle = tmp;

		}

		Angle invert()
		{
			return new Angle(angle - 180);
		}

		int getRotation()
		{
			return angle / rotationFraction;
		}

		@Override
		public String toString()
		{
			return "Angle [angle=" + angle + "]";
		}
	}

	static Map<Integer, Vector3D> unitVectorCache = new HashMap<>();

	static Vector3D getUnitVector(Angle angle)
	{

		Vector3D vector = unitVectorCache.get(angle.angle);
		if (vector == null)
		{
			// unit vector
			vector = new Vector3D(1, 0, 0);

			// rotate
			Rotation rotation = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(angle.angle));
			vector = rotation.applyTo(vector);
			unitVectorCache.put(angle.angle, vector);

		}
		return vector;
	}

	MoveTemplate moveTemplateFactory(int cost, Angle angle)
	{
		return new MoveTemplate(cost, angle);
	}

	public class MoveTemplate
	{

		final double cost;
		final Angle angleDelta;

		public MoveTemplate(double cost, Angle angleDelta)
		{
			this.cost = cost;
			this.angleDelta = angleDelta;
		}

		ProposedPose getProposedPose(ProposedPose current)
		{

			Angle angle = new Angle(current.angle.angle + angleDelta.angle);

			Vector3D vector = getUnitVector(angle);
			// calc new xy
			double x = vector.getX() + current.x;
			double y = vector.getY() + current.y;

			return new ProposedPose(x, y, angle, current.proposedCost + cost);

		}

		@Override
		public String toString()
		{
			return "MoveTemplate [cost=" + cost + ", angleDelta=" + angleDelta + "]";
		}

	}

	public class ProposedPose extends InternalPose
	{

		protected final double proposedCost;

		ProposedPose(double x, double y, Angle angle, double proposedCost)
		{
			super(x, y, angle);
			this.proposedCost = proposedCost;
		}

		public boolean isBetter(Step[][][] plan)
		{
			return plan[(int) x][(int) y][angle.getRotation()].cost > proposedCost;
		}

		void addToPlan(MoveTemplate move, Step[][][] plan)
		{
			plan[(int) x][(int) y][angle.getRotation()].cost = proposedCost;
			plan[(int) x][(int) y][angle.getRotation()].move = move;
		}

	}

	class InternalPose
	{

		double x;
		double y;
		Angle angle;

		InternalPose(double x, double y, Angle rotation)
		{
			this.x = x;
			this.y = y;
			this.angle = rotation;
		}

		Step getStep(Step[][][] plan)
		{
			return plan[(int) x][(int) y][angle.getRotation()];
		}

		boolean isWithinBounds()
		{
			return x >= 0 && x < maxX && y >= 0 && y < maxY;
		}

		boolean isAtGoal()
		{
			double tmp = Math.abs(target.x - x) + Math.abs(target.y - y);

			// System.out.println("Distance to target " + tmp);

			return tmp < 2;

		}

		@Override
		public String toString()
		{
			return "InternalPose [x=" + x + ", y=" + y + ", rotation=" + angle + "]";
		}

	}

}
