package au.com.rsutton.navigation.router.md;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class RoutePlanner3D implements PlannerIfc
{

	Step[][][] plan;
	private int maxY;
	private int maxX;

	private int angleArraySize;
	private RpPose target;

	private int[][] map;

	public RoutePlanner3D(int[][] map, int rotations)
	{
		this.maxX = map.length;
		this.maxY = map[0].length;
		this.map = map;

		this.angleArraySize = 360 / rotations;
		if (360 / angleArraySize != rotations)
		{
			throw new RuntimeException("rotations must be a factor of 360");
		}

		plan = new Step[maxX][maxY][rotations];
		for (int x = 0; x < maxX; x++)
			for (int y = 0; y < maxY; y++)
				for (int angleIndex = 0; angleIndex < rotations; angleIndex++)
				{
					plan[x][y][angleIndex] = new Step();
				}
		System.out.println("expect " + (maxX * maxY * rotations));
	}

	private class Step
	{
		double cost = Integer.MAX_VALUE;
		MoveTemplate move = null;
	}

	@Override
	public void plan(RpPose target, MoveTemplate[] moveTemplates)
	{

		this.target = target;
		ProposedPose start = new ProposedPose(target, 0);
		List<ProposedPose> work = new LinkedList<>();
		work.add(start);
		int ctr = 0;
		while (!work.isEmpty())
		{
			ctr++;
			ProposedPose priorPose = work.remove(0);
			for (MoveTemplate move : moveTemplates)
			{
				ProposedPose proposedPose = getProposedPose(move, priorPose, map);
				if (proposedPose.isWithinBounds() && proposedPose.isBetterThanExisting())
				{
					proposedPose.addToPlan(move);
					work.add(proposedPose);
				}
			}
		}
		System.out.println("steps " + (ctr * moveTemplates.length));

		dumpUnroutable();

	}

	private void dumpUnroutable()
	{
		int unroutable = 0;
		for (int i = 0; i < plan.length; i++)
			for (int j = 0; j < plan[0].length; j++)
				for (int k = 0; k < plan[0][0].length; k++)
				{
					if (plan[i][j][k].cost == Integer.MAX_VALUE)
					{
						unroutable++;
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

	void dumpFrom(RpPose startingPose)
	{
		String[][] result = new String[maxX][maxY];
		RobotMoveSimulator robot = new RobotMoveSimulator(startingPose);
		int ctr = 0;
		MoveTemplate move = null;
		do
		{
			move = getNextMove(robot.getPose());
			if (move != null)
			{
				robot.performMove(move);
				if (new InternalPose(robot.getPose()).isAtGoal())
				{
					System.out.println("at goal");
					break;
				}
				if (!new InternalPose(robot.getPose()).isWithinBounds())
				{
					System.out.println("out of bounds");
					break;
				}
				result[(int) robot.getPose().getX()][(int) robot.getPose().getY()] = move.name;
			}
			ctr++;
			if (ctr > 2000)
			{
				break;
			}

		} while (move != null);

		dumpPath((int) startingPose.getX(), (int) startingPose.getY(), result);
	}

	@Override
	public MoveTemplate getNextMove(RpPose target)
	{

		// we use the inverted angle because we are following the map
		// back(wards) towards the origin
		RPAngle invertedAngle = target.getAngle().invert();

		InternalPose currentPose = new InternalPose(target.getX(), target.getY(), invertedAngle);
		Step step = currentPose.getStep();

		if (step != null && step.move != null)
		{
			return step.move;
		}
		return null;

	}

	private void dumpPath(int x, int y, String[][] result)
	{
		for (int y1 = 0; y1 < maxY; y1++)

		{
			System.out.print("" + (y1 % 10));
			for (int x1 = 0; x1 < maxX; x1++)
			{
				if (x1 == x && y1 == y)
				{
					System.out.print("S");
				} else if (x1 == (int) target.getX() && y1 == (int) target.getY())
				{
					System.out.print("T");
				} else if (result[x1][y1] != null)
				{
					System.out.print(result[x1][y1]);
				} else if (map[x1][y1] == Integer.MAX_VALUE)
				{
					System.out.print("*");
				} else
				{
					System.out.print(" ");
				}
			}
			System.out.println("");
		}
	}

	public class Angle extends RPAngle
	{

		Angle(int degrees)
		{
			super(degrees);
		}

		@Override
		public Angle invert()
		{
			return new Angle(getDegrees() - 180);
		}

		int asArrayIndex()
		{
			return getDegrees() / angleArraySize;
		}

		@Override
		public String toString()
		{
			return "Angle [angle=" + getDegrees() + "]";
		}
	}

	static Map<Integer, Vector3D> unitVectorCache = new HashMap<>();

	static Vector3D getUnitVector(RPAngle angle)
	{

		Vector3D vector = unitVectorCache.get(angle.getDegrees());
		if (vector == null)
		{
			// unit vector
			vector = new Vector3D(1, 0, 0);

			// rotate
			Rotation rotation = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(angle.getDegrees()));
			vector = rotation.applyTo(vector);
			unitVectorCache.put(angle.getDegrees(), vector);

		}
		return vector;
	}

	ProposedPose getProposedPose(MoveTemplate template, ProposedPose current, int map[][])
	{

		RPAngle angle = new RPAngle(current.getAngle().getDegrees() + template.angleDelta.getDegrees());

		Vector3D unitVector = RoutePlanner3D.getUnitVector(angle);

		int directionMultiplier = 1;
		if (!template.forward)
		{
			directionMultiplier = -1;
		}

		// calc new xy
		double x = (unitVector.getX() * directionMultiplier) + current.getX();
		double y = (unitVector.getY() * directionMultiplier) + current.getY();

		ProposedPose proposed = new ProposedPose(x, y, angle, current.proposedCost + template.moveCost);
		if (proposed.isWithinBounds())
		{
			int positionCost = map[(int) x][(int) y];
			proposed.proposedCost += positionCost;
		} else
		{
			proposed.proposedCost = Integer.MAX_VALUE;
		}
		return proposed;

	}

	class ProposedPose extends InternalPose
	{

		protected double proposedCost;

		ProposedPose(double x, double y, RPAngle angle, double proposedCost)
		{
			super(x, y, angle);
			this.proposedCost = proposedCost;
		}

		public ProposedPose(RpPose pose, double proposedCost)
		{
			super(pose);
			this.proposedCost = proposedCost;
		}

		public boolean isBetterThanExisting()
		{
			return plan[(int) getX()][(int) getY()][angleArrayIndex].cost > proposedCost;
		}

		void addToPlan(MoveTemplate move)
		{
			Step step = plan[(int) getX()][(int) getY()][angleArrayIndex];
			step.cost = proposedCost;
			step.move = move;
		}

	}

	class InternalPose extends RpPose
	{

		protected int angleArrayIndex;

		InternalPose(RpPose pose)
		{
			super(pose);
			angleArrayIndex = pose.getAngle().getDegrees() / angleArraySize;
		}

		InternalPose(double x, double y, RPAngle angle)
		{
			super(x, y, angle);
			angleArrayIndex = angle.getDegrees() / angleArraySize;
		}

		Step getStep()
		{
			return plan[(int) getX()][(int) getY()][angleArrayIndex];
		}

		boolean isWithinBounds()
		{
			return getX() >= 0 && getX() < maxX && getY() >= 0 && getY() < maxY
					&& map[(int) getX()][(int) getY()] < Integer.MAX_VALUE;
		}

		boolean isAtGoal()
		{
			double tmp = Math.abs(target.getX() - getX()) + Math.abs(target.getY() - getY());

			// we use the inverted angle because we are following the map
			// back(wards) towards the origin
			RPAngle invertedAngle = getAngle().invert();
			double tmp2 = Math.abs(new Angle(target.getAngle().getDegrees() - invertedAngle.getDegrees()).getDegrees());

			return tmp < 2 && tmp2 < 20;

		}

		@Override
		public String toString()
		{
			return "InternalPose [x=" + getX() + ", y=" + getY() + ", rotation=" + getAngle() + "]";
		}

	}

}
