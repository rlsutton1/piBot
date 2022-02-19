package au.com.rsutton.gradientdescent;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.junit.Test;

import com.google.common.base.Stopwatch;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.kalman.RobotPoseSourceTimeTraveling.RobotPoseInstant;
import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;
import au.com.rsutton.navigation.feature.DistanceXY;
import au.com.rsutton.navigation.router.ExpansionPoint;
import au.com.rsutton.navigation.router.RoutePlanner;
import au.com.rsutton.navigation.router.RoutePlannerFinalStage;
import au.com.rsutton.ui.DataSourceMap;
import au.com.rsutton.units.Distance;
import au.com.rsutton.units.DistanceUnit;

class PlannerNext implements RoutePlannerFinalStage, DataSourceMap
{

	static final int REQUIRED_ACCURACY = 5;

	public static int INTERMEDIATES = 3;

	public static int STEP_SIZE = 5;
	public static int STEP_LIMIT = 8 * INTERMEDIATES;

	private List<Point> pointList = new CopyOnWriteArrayList<>();

	static List<Transition> transitions;

	@Test
	public void test() throws InterruptedException
	{

		Target[] targets = {
				new Target(30, 00), new Target(0, 30) };

		// current location
		double x = 0;
		double y = 0;
		double angle = 0;

		// previous turn angle
		double thetaDelta = 0;

		ProbabilityMap map = new ProbabilityMap(5, 0.5);

		State initialState = new State(targets, x, y, angle, new Transition((int) thetaDelta, STEP_SIZE));

		State state = solve(initialState, targets, STEP_LIMIT, map);

		System.out.println("Solved " + state);
		// state = solve(targets, steps, map);
		// state = solve(targets, steps, map);
		// state = solve(targets, steps, map);
		// state = solve(targets, steps, map);
		// state = solve(targets, steps, map);
		// state = solve(targets, steps, map);
		// state = solve(targets, steps, map);
		// state = solve(targets, steps, map);
		// state = solve(targets, steps, map);

	}

	static private void createPossibleTransitions()
	{
		int m1 = 1;
		int m2 = 2;
		int m3 = 5;
		int m4 = 10;

		transitions = new ArrayList<>();
		transitions.add(new Transition(0, PlannerNext.STEP_SIZE));
		transitions.add(new Transition(m1, PlannerNext.STEP_SIZE));
		transitions.add(new Transition(-m1, PlannerNext.STEP_SIZE));
		transitions.add(new Transition(m2, PlannerNext.STEP_SIZE));
		transitions.add(new Transition(-m2, PlannerNext.STEP_SIZE));
		transitions.add(new Transition(m3, PlannerNext.STEP_SIZE));
		transitions.add(new Transition(-m3, PlannerNext.STEP_SIZE));
		// possible.add(new Transition(20, -PlannerNext.STEP_SIZE));
		transitions.add(new Transition(m4, -PlannerNext.STEP_SIZE));
		transitions.add(new Transition(-m4, -PlannerNext.STEP_SIZE));

	}

	State solution;

	final Semaphore semaphore = new Semaphore(1);

	private Distance absoluteTotalDistance;

	@Override
	public void plan(RobotPoseInstant poseSource, RoutePlanner planner, ProbabilityMapIIFc world)
			throws InterruptedException
	{
		if (semaphore.tryAcquire())
		{
			Stopwatch timer = Stopwatch.createStarted();
			try
			{

				if (solution != null && solution.distanceTraveledSinceSolutionCreated != null)
				{
					try
					{
						State result = validateOldSolution(solution, absoluteTotalDistance, poseSource, world);
						if (result != null)
						{
							solution = result;
							solution.distanceTraveledSinceSolutionCreated = absoluteTotalDistance;
							return;
						}
					} catch (CollisionException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				DistanceXY xy = poseSource.getXyPosition();

				ExpansionPoint next = new ExpansionPoint((int) xy.getX().convert(DistanceUnit.CM),
						(int) xy.getY().convert(DistanceUnit.CM), 0, null);

				List<Target> vals = new LinkedList<>();

				int i = 1;
				int lookAheadCM = 75;
				int sampleDistance = 35;
				Vector3D wayPoint = null;
				for (; i < lookAheadCM; i++)
				{
					next = planner.getRouteForLocation(next.getX(), next.getY());
					if (i % sampleDistance == 0)
					{
						wayPoint = new Vector3D(next.getX(), next.getY(), 0);

						if (wayPoint.distance(xy.getVector(DistanceUnit.CM)) > 10)
						{
							vals.add(new Target(wayPoint.getX(), wayPoint.getY()));
							if (vals.size() >= 2)
							{
								break;
							}
						}
					}
				}
				if (vals.isEmpty() && wayPoint != null)
				{
					if (wayPoint.distance(xy.getVector(DistanceUnit.CM)) > 10)
					{
						vals.add(new Target(wayPoint.getX(), wayPoint.getY()));

					}
				}

				if (vals.size() > 0)
				{
					State initialState = new State(vals.toArray(new Target[] {}), xy.getX().convert(DistanceUnit.CM),
							xy.getY().convert(DistanceUnit.CM), poseSource.getHeading(),
							new Transition((int) getAngle(), STEP_SIZE));

					solution = solve(initialState, vals.toArray(new Target[] {}), STEP_LIMIT, (ProbabilityMap) world);
					if (solution != null)
					{
						solution.distanceTraveledSinceSolutionCreated = absoluteTotalDistance;
						System.out.println("Solved " + solution);
					}

					buildPointList();
				}
			} finally
			{
				System.out.println("Plan took " + timer.elapsed(TimeUnit.MILLISECONDS));
				semaphore.release();
			}
		}

	}

	void buildPointList()
	{
		pointList.clear();

		State current = solution;
		while (current != null && current.parentState != null)
		{
			pointList.add(new Point((int) current.x, (int) current.y));

			current = current.getParentState();
		}

	}

	@Override
	public int getDirection()
	{
		int direction = 1;
		State current = solution;
		while (current != null && current.parentState != null)
		{
			direction = (int) Math.signum(current.transition.distance);
			current = current.getParentState();
		}
		return direction;
	}

	double getAngle()
	{
		double angle = 0;
		State current = solution;
		while (current != null && current.parentState != null)
		{
			angle = current.transition.thetaDelta;
			current = current.getParentState();
		}
		return angle;
	}

	@Override
	public double getTurnRadius()
	{
		return getRadiusOfArc(getAngle(), STEP_SIZE);
	}

	public double getRadiusOfArc(double angle, double length)
	{
		// H is the hieght of the arc
		// W is the width of the arc

		// formula for the radius of an arc

		// r = (H/2)+ ((W^2)/8H)

		Vector3D v = new Vector3D(0, length, 0);

		Rotation rotation = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(angle));

		Vector3D vector = v.add(rotation.applyTo(v));
		double w = vector.getNorm();
		double h = rotation.applyTo(v).getX();

		return (h / 2.0) + ((w * w) / (8.0 * h));

	}

	State validateOldSolution(State oldSolution, Distance absoluteDistanceTraveled, RobotPoseInstant poseSource,
			ProbabilityMapIIFc map) throws CollisionException
	{

		double distanceTraveledSinceSolutionCreated = absoluteDistanceTraveled.convert(DistanceUnit.CM)
				- oldSolution.distanceTraveledSinceSolutionCreated.convert(DistanceUnit.CM);
		// build forward order state list
		List<State> states = new LinkedList<>();
		State next = solution;
		while (next != null)
		{
			states.add(0, next);
			next = next.parentState;
		}

		State result = null;
		int distance = 0;
		boolean matched = false;
		for (State state : states)
		{
			if (!matched)
			{
				// find our current pose in the list of states
				if (Math.abs(distance - distanceTraveledSinceSolutionCreated) < REQUIRED_ACCURACY)
				{
					// check x, y and theta match acceptably closely
					double xDelta = Math.abs(state.x - poseSource.getXyPosition().getX().convert(DistanceUnit.CM));
					double yDelta = Math.abs(state.y - poseSource.getXyPosition().getY().convert(DistanceUnit.CM));
					double thetaDelta = HeadingHelper.getChangeInHeading(state.theta, poseSource.getHeading());
					if (xDelta < REQUIRED_ACCURACY * 2 && yDelta < REQUIRED_ACCURACY * 2
							&& thetaDelta < REQUIRED_ACCURACY * 2)
					{
						// this is the new starting state
						double x = poseSource.getXyPosition().getX().convert(DistanceUnit.CM);
						double y = poseSource.getXyPosition().getX().convert(DistanceUnit.CM);
						double theta = poseSource.getHeading();

						result = new State(state.targets, x, y, theta, state.transition);
						matched = true;

					}
				}
			} else
			{
				// we've found where we are in the state list, rebuild the rest
				// of the
				// states with the updated start position
				result = State.transition(result, state.transition);
			}
			distance += Math.abs(state.transition.distance);
		}
		if (result != null)
		{
			// validate that the updated solution doesn't result in a collision
			validateSolution(result, map);
			return result;
		}

		return null;

	}

	private void validateSolution(State solution, ProbabilityMapIIFc map) throws CollisionException
	{
		int ctr = 0;
		State next = solution;
		while (next != null)
		{
			State.collisionCheck(map, next);
			next = next.parentState;
			ctr++;
		}
		if (ctr < 5)
		{
			// not really a collision, but the remaining path is to short to be
			// safe to use as it leaves insufficient scope for corrections
			throw new CollisionException("Only " + ctr + " steps remain");
		}
	}

	static State solve(State initialState, Target[] targets, int maxSteps, ProbabilityMap map)
			throws InterruptedException
	{

		createPossibleTransitions();

		State currentState = initialState;

		State bestState = null;

		int ctr = 0;
		while (currentState != null)
		{
			ctr++;
			try
			{
				State possible = currentState.getNextPossibleState(currentState, map, transitions);
				if (possible != null)
				{
					if (possible.step < maxSteps)
					{
						currentState = possible;
						if (currentState.score.bestTargetDistances[0] < REQUIRED_ACCURACY)
						{
							if (bestState == null || currentState.score.isBetterThan(bestState.score))
							{
								bestState = currentState;
							}
							currentState = stepUpTree(currentState);
						}
					} else
					{
						if (bestState == null || currentState.score.isBetterThan(bestState.score))
						{
							if (currentState.score.bestTargetDistances[0] < REQUIRED_ACCURACY)
							{
								bestState = currentState;
							}
						}

						currentState = stepUpTree(currentState);
					}
				} else
				{
					currentState = stepUpTree(currentState);
				}
			} catch (CollisionException e)
			{
				// abandon this path, step back and try another path
				currentState = stepUpTree(currentState);
			}
			if (ctr % 100000 == 1)
			{
				System.out.println("Ctr : " + ctr);
			}
		}

		System.out.println("States evaluated " + ctr);
		for (Target target : targets)
		{
			System.out.println(target);
		}
		if (bestState != null)
		{

			System.out.println(" cost: " + bestState.score.bestTargetDistance + " " + bestState.score.reverseCount);
		} else
		{
			System.out.println("Failed to plan route");
		}
		return bestState;

	}

	static State stepUpTree(State state)
	{
		State newState = state;
		while (newState != null)
		{
			newState = newState.getParentState();
			if (newState != null && !newState.intermediate)
			{
				break;
			}
		}
		return newState;
	}

	@Override
	public List<Point> getPoints()
	{
		return pointList;
	}

	@Override
	public void drawPoint(BufferedImage image, double pointOriginX, double pointOriginY, double scale, double originalX,
			double originalY)
	{

		Graphics graphics = image.getGraphics();

		graphics.setColor(Color.ORANGE);

		graphics.drawLine((int) (pointOriginX), (int) (pointOriginY), (int) ((pointOriginX + 5)),
				(int) ((pointOriginY + 5)));
		// System.out.println(pointOriginX + " " + pointOriginY + " " + value);

	}

	@Override
	public void setAbsoluteTotalDistance(Distance absoluteTotalDistance)
	{
		this.absoluteTotalDistance = absoluteTotalDistance;
	}

}

class Target
{
	final double x;
	final double y;

	Target(double x, double y)
	{
		this.x = x;
		this.y = y;

	}

	@Override
	public String toString()
	{
		return "Target [x=" + x + ", y=" + y + "]";
	}
}

class State
{
	boolean intermediate = false;
	Distance distanceTraveledSinceSolutionCreated;

	@Override
	public String toString()
	{
		String result = "";
		State current = this;
		while (current != null)
		{
			result += "State [x=" + current.x + ", y=" + current.y + ", step=" + current.step + ", angle="
					+ current.theta + ", thetaDelta= " + current.transition.thetaDelta + ", intermediate= "
					+ current.intermediate + "]\n";
			current = current.parentState;
		}
		result += "\nScore=" + score;
		return result;
	}

	public State getNextPossibleState(State current, ProbabilityMap map, List<Transition> transitions)
			throws CollisionException
	{
		if (transitionCounter == transitions.size())
			return null;
		Transition transistion = transitions.get(transitionCounter++);

		double delta = Math.abs(current.transition.thetaDelta - transistion.thetaDelta);
		if (delta > 11)
		{
			return getNextPossibleState(current, map, transitions);
		}

		State next = this;
		for (int i = 0; i < PlannerNext.INTERMEDIATES; i++)
		{
			// step forward through three steps, marking the first two as
			// intermediate
			next = transition(next, transistion);
			next.intermediate = true;
			collisionCheck(map, next);

		}
		next = transition(next, transistion);

		collisionCheck(map, next);
		return next;
	}

	static void collisionCheck(ProbabilityMapIIFc map, State state) throws CollisionException
	{
		if (map.get(state.x, state.y) > 0.5)
			throw new CollisionException(state.toString());
	}

	public State getParentState()
	{
		return parentState;
	}

	int transitionCounter = 0;

	final State parentState;

	final double x;
	final double y;
	final double theta;
	final Transition transition;

	final int step;
	final Score score;

	final Target[] targets;

	State(State parentState, double x, double y, double theta, Transition transition)
	{
		this.targets = parentState.targets;
		this.step = parentState.step + 1;
		this.x = x;
		this.y = y;
		this.theta = theta;
		this.transition = transition;
		this.parentState = parentState;
		score = parentState.score.add(this);

	}

	State(Target[] targets, double x, double y, double theta, Transition transition)
	{
		this.targets = targets;
		this.x = x;
		this.y = y;
		this.theta = theta;
		this.transition = transition;
		this.step = 0;
		parentState = null;
		score = new Score().add(this);

	}

	static State transition(State parentState, Transition transition)
	{

		final double theta = parentState.theta + transition.thetaDelta;
		Vector3D position = new Vector3D(parentState.x, parentState.y, 0);
		Vector3D move = new Vector3D(0, transition.distance, 0);

		// TODO optimise (cache) the rotation here
		Rotation rotation = getCachedRotation((int) theta);
		position = rotation.applyTo(move).add(position);

		return new State(parentState, position.getX(), position.getY(), theta, transition);
	}

	static Rotation[] rotationCache = new Rotation[361];

	static Rotation getCachedRotation(int degrees)
	{
		degrees = (int) HeadingHelper.normalizeHeading(degrees);
		Rotation rotation = rotationCache[degrees];
		if (rotation == null)
		{
			rotation = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(degrees));
			rotationCache[degrees] = rotation;
		}
		return rotation;
	}

}

class Score
{
	int reverseCount = 0;
	int directionChangeCount = 0;
	double[] bestTargetDistances;

	double bestTargetDistance = Double.MAX_VALUE;

	int depth;

	double totalAngle = 0;

	public boolean isBetterThan(Score score)
	{
		if (reverseCount < score.reverseCount)
			return true;
		if (reverseCount == score.reverseCount)
		{
			if (directionChangeCount < score.directionChangeCount)
				return true;
			if (directionChangeCount == score.directionChangeCount)
			{

				if (isTargetDistanceBetter(score))
					return true;
				if (isTargetDistanceEqual(score))
				{
					if (depth < score.depth)
						return true;
					if (depth == score.depth)

						if (totalAngle < score.totalAngle)
							return true;
				}
			}
		}
		return false;

	}

	boolean isTargetDistanceBetter(Score score)
	{
		for (int i = 0; i < score.bestTargetDistances.length; i++)
		{
			double local = Math.max(PlannerNext.REQUIRED_ACCURACY, bestTargetDistances[i]);
			double other = Math.max(PlannerNext.REQUIRED_ACCURACY, score.bestTargetDistances[i]);

			if (local < other)
			{
				return true;

			}
			if (local > other)
			{
				return false;
			}
		}
		return false;
	}

	boolean isTargetDistanceEqual(Score score)
	{
		for (int i = 0; i < score.bestTargetDistances.length; i++)
		{
			double local = Math.max(PlannerNext.REQUIRED_ACCURACY, bestTargetDistances[i]);
			double other = Math.max(PlannerNext.REQUIRED_ACCURACY, score.bestTargetDistances[i]);
			if (Math.abs(local - other) > 0.05)
			{
				return false;

			}

		}
		return true;
	}

	@Override
	public String toString()
	{
		return "Score [reverseCount=" + reverseCount + ", bestTargetDistance=" + bestTargetDistance + "]";
	}

	Score add(State state)
	{
		Score score = new Score();
		score.depth = state.step;

		score.bestTargetDistances = new double[state.targets.length];
		if (state.parentState == null)
		{
			score.totalAngle = state.transition.thetaDelta;

			for (int i = 0; i < score.bestTargetDistances.length; i++)
			{
				score.bestTargetDistances[i] = Double.MAX_VALUE;
			}
		} else
		{
			score.directionChangeCount = state.parentState.score.directionChangeCount;

			score.totalAngle = state.transition.thetaDelta + state.parentState.score.totalAngle;
			score.reverseCount = state.parentState.score.reverseCount;
			for (int i = 0; i < score.bestTargetDistances.length; i++)
			{
				score.bestTargetDistances[i] = state.parentState.score.bestTargetDistances[i];
			}
		}

		double worst = 0;
		for (int i = 0; i < score.bestTargetDistances.length; i++)
		{
			double dist = getDistanceToTarget(state, state.targets[i]);
			if (dist < score.bestTargetDistances[i])
			{
				score.bestTargetDistances[i] = dist;
			}
			worst = Math.max(worst, score.bestTargetDistances[i]);

		}

		score.bestTargetDistance = Math.min(score.bestTargetDistance, worst);

		if (state.parentState != null && Math
				.signum((int) state.parentState.transition.distance) != (int) Math.signum(state.transition.distance))
		{
			score.directionChangeCount += 1;
		}

		if (state.transition.distance < 0)
			score.reverseCount++;
		return score;
	}

	static double getDistanceToTarget(State state, Target target)
	{
		return new Vector3D(state.x, state.y, 0).distance(new Vector3D(target.x, target.y, 0));
	}

}

class Transition
{
	final double thetaDelta;
	double distance;

	public Transition(int thetaDelta, int distance)
	{
		super();
		this.thetaDelta = thetaDelta;
		this.distance = distance;
	}
}
