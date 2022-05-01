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
		Expansion start = new Expansion(x, y, angle);
		List<Expansion> work = new LinkedList<>();
		work.add(start);
		int ctr = 0;
		while (!work.isEmpty())
		{
			ctr++;
			Expansion job = work.remove(0);
			MoveTemplate move = job.getNextMove(moveTemplates);
			if (move != null)
			{
				ProposedMove next = move.getProposedPose(job);
				if (next != null && isWithinBounds(next))
				{

					if (next.isBetter(plan))
					{
						next.setCostOfPose(next.getCostOfProposedMove(), plan, move);
						work.add(next);
					}

				}
				if (next != null)
				{
					// requeue the job, it may have more moves
					work.add(job);
				}
			}

		}
		System.out.println("steps " + ctr);

	}

	int angleToRotation(int angle)
	{
		return angle / rotationFraction;
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
		final int angle;

		@Override
		public String toString()
		{
			return "Angle [angle=" + angle + "]";
		}

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

		Angle add(Angle angle)
		{
			return new Angle(this.angle + angle.angle);
		}

		int delta(Angle angle)
		{
			return new Angle(this.angle - angle.angle).angle;
		}

		public int getRotation()
		{
			return angleToRotation(angle);
		}
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
				double currentCost = step.cost;
				System.out.println(ctr + " " + currentPose + " = " + currentCost + " " + step.move);

				Angle angle = new Angle(currentPose.angle.angle - step.move.angleDelta.angle);
				Vector3D uv = getUnitVector(angle);
				position = position.subtract(uv);

				InternalPose nextPose = new InternalPose(position.getX(), position.getY(), angle);

				// TODO: magic number 5
				if (!isWithinBounds(nextPose) || isAtGoal(nextPose))
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

		for (int y1 = 0; y1 < maxY; y1++)

		{
			System.out.print("" + (y1 % 10));
			for (int x1 = 0; x1 < maxX; x1++)
			{
				if (result[x1][y1] > 0)
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

	private boolean isAtGoal(InternalPose nextPose)
	{
		double tmp = Math.abs(target.x - nextPose.x) + Math.abs(target.y - nextPose.y);

		System.out.println("Distance to target " + tmp);

		return tmp < 5;

	}

	InternalPose getBestPoseValue(double x, double y, Angle angle)
	{
		InternalPose result = null;
		double best = Integer.MAX_VALUE;
		if (x >= 0 && x < maxX && y >= 0 && y < maxY)
		{
			for (int r = 0; r < plan[(int) x][(int) y].length; r++)

			{

				Angle toTry = new Angle(rotationToAngle(r));
				double cost = plan[(int) x][(int) y][r].cost;
				if (cost < best)
				{
					result = new InternalPose(x, y, toTry);
					best = cost;
				}
			}
		}
		return result;
	}

	private boolean isWithinBounds(InternalPose next)
	{
		return next.x >= 0 && next.x < maxX && next.y >= 0 && next.y < maxY;
	}

	static class ProposedMove extends Expansion
	{

		ProposedMove(double x, double y, Angle angle)
		{
			super(x, y, angle);
		}

		public boolean isBetter(Step[][][] plan)
		{
			return getCostOfPose(plan) > getCostOfProposedMove();
		}

		ProposedMove addCost(double toAdd)
		{
			value += toAdd;
			return this;
		}

		int getCostOfProposedMove()
		{
			return value;
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
		@Override
		public String toString()
		{
			return "MoveTemplate [cost=" + cost + ", angleDelta=" + angleDelta + "]";
		}

		final int cost;
		final Angle angleDelta;

		public MoveTemplate(int cost, Angle angleDelta)
		{
			this.cost = cost;
			this.angleDelta = angleDelta;
		}

		ProposedMove getProposedPose(Expansion current)
		{

			Angle angle = new Angle(current.angle.angle + angleDelta.angle);

			Vector3D vector = getUnitVector(angle);

			ProposedMove proposed = getProposedMove(current, angle, vector);

			return proposed.addCost(current.value + cost);

		}

		private ProposedMove getProposedMove(Expansion current, Angle angle, Vector3D vector)
		{
			// calc new xy
			double x = vector.getX() + current.x;
			double y = vector.getY() + current.y;

			ProposedMove proposed = new ProposedMove(x, y, angle);
			return proposed;
		}

		public MoveTemplate invert()
		{
			return new MoveTemplate(cost, new Angle(angleDelta.angle * -1));
		}
	}

	static public class Expansion extends InternalPose
	{

		int value;

		int usedMoveIndex = 0;

		Expansion(InternalPose pose)
		{
			super(pose.x, pose.y, pose.angle);
		}

		public MoveTemplate getNextMove(MoveTemplate[] moveTemplates)
		{
			if (usedMoveIndex < moveTemplates.length)
			{
				MoveTemplate move = moveTemplates[usedMoveIndex++];

				return move;
			}

			return null;
		}

		Expansion(double x, double y, Angle angle)
		{
			super(x, y, angle);
		}

	}

	static class InternalPose
	{
		@Override
		public String toString()
		{
			return "InternalPose [x=" + x + ", y=" + y + ", rotation=" + angle + "]";
		}

		double x;
		double y;
		Angle angle;

		InternalPose(double x, double y, Angle rotation)
		{
			this.x = x;
			this.y = y;
			this.angle = rotation;
		}

		double getCostOfPose(Step[][][] plan)
		{
			return plan[(int) x][(int) y][angle.getRotation()].cost;
		}

		Step getStep(Step[][][] plan)
		{
			return plan[(int) x][(int) y][angle.getRotation()];
		}

		void setCostOfPose(double value, Step[][][] plan, MoveTemplate move)
		{
			plan[(int) x][(int) y][angle.getRotation()].cost = value;
			plan[(int) x][(int) y][angle.getRotation()].move = move;
		}
	}

}
