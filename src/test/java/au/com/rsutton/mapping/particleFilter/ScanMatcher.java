package au.com.rsutton.mapping.particleFilter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JFrame;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;
import au.com.rsutton.ui.DataSourcePoint;
import au.com.rsutton.ui.DataSourceStatistic;
import au.com.rsutton.ui.MapUI;
import au.com.rsutton.ui.WrapperForMapInMapUI;

public class ScanMatcher extends JFrame
{

	private MapUI graph = new MapUI();

	ProbabilityMapIIFc world;
	private ParticleFilterObservationSet observations;
	private double compassAdjustment;

	volatile double currentMatch = 0;

	private AtomicReference<Pose> currentPose = new AtomicReference<>();

	ScanMatcher(ProbabilityMap world, ParticleFilterObservationSet observations, double compassAdjustment)
	{
		this.world = world;
		this.observations = observations;
		this.compassAdjustment = 0;// compassAdjustment;

		setup();

		WrapperForMapInMapUI mapSource = new WrapperForMapInMapUI(world, new Color(255, 255, 255));

		graph.addDataSource(mapSource);

		matchLoop(new Pose(0, 0, 0));

	}

	void setup()
	{
		this.setBounds(0, 0, 850, 900);

		FlowLayout experimentLayout = new FlowLayout();

		this.setLayout(experimentLayout);

		graph = new MapUI();
		graph.setPreferredSize(new Dimension(750, 750));
		this.add(graph);

		setSize(700, 700);
		setLocation(200, 200);
		setVisible(true);

		DataSourcePoint pointSource = new DataSourcePoint()
		{

			@Override
			public List<Point> getOccupiedPoints()
			{
				Rotation rotation = new Rotation(RotationOrder.XYZ, 0, 0, currentPose.get().heading);
				List<Point> points = new LinkedList<>();
				for (ScanObservation obs : observations.getObservations())
				{
					Vector3D pos = rotation.applyTo(obs.getVector());
					points.add(new Point((int) pos.getX(), (int) pos.getY()));
				}
				return points;
			}

		};

		WrapperForMapInMapUI mapSource = new WrapperForMapInMapUI(pointSource, new Color(255, 0, 0));
		graph.addDataSource(mapSource);

		graph.addStatisticSource(new DataSourceStatistic()
		{

			@Override
			public String getValue()
			{
				return "" + currentMatch;
			}

			@Override
			public String getLabel()
			{
				return "best";
			}
		});

	}

	Pose matchLoop(Pose initialPose)
	{
		for (int i = 0; i < 200; i++)
		{

			initialPose = match(initialPose);
			currentPose.set(initialPose);

			graph.render(0, 0, 0.5);
			this.repaint();
			try
			{
				Thread.sleep(100);
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(i);

		}
		return initialPose;
	}

	private Pose match(Pose initialPose)
	{
		double rate = 0.2;

		Pose posePlusX = initialPose.addX(5);
		Pose poseMinusX = initialPose.addX(-5);

		Pose posePlusY = initialPose.addY(5);
		Pose poseMinusY = initialPose.addY(-5);

		Pose posePlusHeading = initialPose.addHeading(15);
		Pose poseMinusHeading = initialPose.addHeading(-15);

		double current = evaluatePose(initialPose);

		double plusX = current - evaluatePose(posePlusX);
		double minusX = current - evaluatePose(poseMinusX);

		double plusY = current - evaluatePose(posePlusY);
		double minusY = current - evaluatePose(poseMinusY);

		double plusHeading = evaluatePose(posePlusHeading);
		double minusHeading = evaluatePose(poseMinusHeading);

		currentMatch = current;

		Pose finalPose = initialPose.addX(Math.signum(plusX - minusX) * 5.0 * rate);
		finalPose = finalPose.addY(Math.signum(plusY - minusY) * 5.0 * rate);
		finalPose = finalPose.addHeading(Math.signum(plusHeading - minusHeading) * 0.1 * rate);

		return finalPose;
	}

	private double evaluatePose(Pose testPose)
	{

		Particle particle = new Particle(testPose.getX(), testPose.getY(), testPose.getHeading(), 0, 0);

		particle.addObservation(world, observations, compassAdjustment, false);

		return particle.getRating();

	}
}
