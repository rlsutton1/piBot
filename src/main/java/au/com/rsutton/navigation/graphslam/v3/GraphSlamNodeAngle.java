package au.com.rsutton.navigation.graphslam.v3;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;

public class GraphSlamNodeAngle extends GraphSlamNodeLinear
{

	GraphSlamNodeAngle(String name, double initialPosition)
	{
		super(name, initialPosition);
	}

	@Override
	public void addConstraint(GraphSlamNode node, double offset, double certainty,
			ConstraintOrigin constraintOrigin)
	{

		double normalised = HeadingHelper.normalizeHeading(offset);
		GraphSlamConstraint target = constraints.get(node);
		if (target != null)
		{
			if (target.constraintOrigin != constraintOrigin)
			{
				throw new RuntimeException("Constraint Directions don't match");
			}
			target.addValue(normalised, certainty);
		} else
		{
			constraints.put(node, new GraphSlamConstraint(this, node, normalised, certainty, constraintOrigin));
		}
	}

	@Override
	public double calculateError(GraphSlamConstraint constraint)
	{

		double error = HeadingHelper.getChangeInHeading(position, constraint.node.getPosition())
				+ HeadingHelper.normalizeHeading(constraint.getOffset());

		if (error > 180)
		{
			error = -360 + error;
		}
		if (error < -180)
		{
			error = 360 + error;
		}

		return error;
	}

	@Override
	public double getPosition()
	{
		return HeadingHelper.normalizeHeading(position);
	}

	@Override
	public void adjustPosition(double error)
	{
		position = getNormalisedOffset(position - error);
	}

	@Override
	public double getNormalisedOffset(double offset)
	{
		return HeadingHelper.normalizeHeading(offset);

	}
}