package au.com.rsutton.deeplearning.feature;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.collection.CollectionRecordReader;
import org.datavec.api.writable.Writable;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

import au.com.rsutton.deeplearning.feature.FeatureSimulatorBase.Scan;
import au.com.rsutton.mapping.particleFilter.RobotPoseSource;
import au.com.rsutton.mapping.particleFilter.ScanObservation;
import au.com.rsutton.navigation.feature.DistanceXY;
import au.com.rsutton.navigation.feature.RobotLocationDeltaListener;
import au.com.rsutton.robot.RobotInterface;
import au.com.rsutton.ui.DataSourceMap;
import au.com.rsutton.units.Angle;
import au.com.rsutton.units.Distance;
import au.com.rsutton.units.DistanceUnit;

public class FeatureExtractionTest3
{

	private static final int DATA_SET_SIZE = 20000;
	protected static final int RANDOM = 0;
	Random rand = new Random();
	MultiLayerNetwork model;

	@Test
	public void test1()
	{
		train();
	}

	public void train()
	{
		RecordReader trainer = getReader(DATA_SET_SIZE);
		RecordReader test = getReader(DATA_SET_SIZE);

		dl4jText(trainer, test);
	}

	private void setupListener(RobotInterface robot)
	{
		robot.addMessageListener(new RobotLocationDeltaListener()
		{

			@Override
			public void onMessage(Angle deltaHeading, Distance deltaDistance, List<ScanObservation> robotLocation)
			{
				evaluateScan(robotLocation);

			}
		});
	}

	List<Vector3D> pointsToDraw = new CopyOnWriteArrayList<>();

	public DataSourceMap getHeadingMapDataSource(final RobotPoseSource pf, RobotInterface robot)
	{
		setupListener(robot);
		return new DataSourceMap()
		{

			@Override
			public List<Point> getPoints()
			{
				DistanceXY pos = pf.getXyPosition();
				List<Point> points = new LinkedList<>();
				points.add(new Point((int) pos.getX().convert(DistanceUnit.CM),
						(int) pos.getY().convert(DistanceUnit.CM)));
				return points;
			}

			@Override
			public void drawPoint(BufferedImage image, double pointOriginX, double pointOriginY, double scale)
			{
				Graphics graphics = image.getGraphics();

				graphics.setColor(new Color(0, 255, 255));
				// draw lidar observation lines
				for (Vector3D obs : pointsToDraw)
				{
					Vector3D vector = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(pf.getHeading()))
							.applyTo(obs);

					graphics.drawRect((int) (pointOriginX + (vector.getX() * scale)),
							(int) (pointOriginY + (vector.getY() * scale)), 5, 5);

				}

			}

		};
	}

	private void evaluateScan(List<ScanObservation> obs)
	{

		double requiredCertainty = 0.99;

		List<Vector3D> finalPoints = new LinkedList<>();

		List<Vector3D> points = new LinkedList<>();
		Vector3D last = null;
		List<Writable> data;
		for (ScanObservation o : obs)
		{
			if (last == null)
			{
				last = o.getVector();
			}
			if (last.distance(o.getVector()) >= 10)
			{
				points.add(last);
				last = o.getVector();
				if (points.size() == 7)
				{
					data = convertFromPointsToAngles(points);
					if (dl4jEval(data) > requiredCertainty)
					{
						finalPoints.add(points.get(3));
					}
					points.remove(0);
				}
			}
		}
		points.add(last);
		if (points.size() == 7)
		{
			data = convertFromPointsToAngles(points);
			if (dl4jEval(data) > requiredCertainty)
			{
				// we found a feature, draw it on the map
				finalPoints.add(points.get(3));
			}
		}

		pointsToDraw.clear();
		pointsToDraw.addAll(finalPoints);

	}

	private double dl4jEval(List<Writable> data)
	{

		List<Writable> tmp = new LinkedList<>();
		tmp.add(new DoubleWriteable(0.0));
		tmp.addAll(data);

		List<List<Writable>> holder = new LinkedList<>();
		holder.add(tmp);

		RecordReader reader = new CollectionRecordReader(holder);

		DataSetIterator testIter = new RecordReaderDataSetIterator(reader, 1, 0, 4);
		DataSet t = testIter.next();

		INDArray features = t.getFeatureMatrix();
		INDArray predicted = model.output(features, false);

		System.out.println(predicted);
		return Math.max(predicted.getDouble(cornerObtuseSimulater.getLabel()),
				predicted.getDouble(cornerAcuteSimulator.getLabel()));

	}

	void dl4jText(RecordReader trainer, RecordReader test)
	{

		int seed = 123;
		double learningRate = 0.001;
		int batchSize = 1000;
		int nEpochs = 50;

		int numInputs = 10;
		int numOutputs = 4;
		int numHiddenNodes = 100;

		// Load the training data:
		DataSetIterator trainIter = new RecordReaderDataSetIterator(trainer, batchSize, 0, 4);

		// Load the test/evaluation data:
		DataSetIterator testIter = new RecordReaderDataSetIterator(test, batchSize, 0, 4);

		MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder().seed(seed).iterations(1)
				.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).learningRate(learningRate)
				.updater(Updater.NESTEROVS).momentum(0.9).list()
				.layer(0,
						new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes).weightInit(WeightInit.XAVIER)
								.activation("relu").build())
				.layer(1,
						new DenseLayer.Builder().nIn(numHiddenNodes).nOut(numHiddenNodes).weightInit(WeightInit.XAVIER)
								.activation("relu").build())
				.layer(2,
						new DenseLayer.Builder().nIn(numHiddenNodes).nOut(numHiddenNodes).weightInit(WeightInit.XAVIER)
								.activation("relu").build())
				.layer(3,
						new OutputLayer.Builder(LossFunction.NEGATIVELOGLIKELIHOOD).weightInit(WeightInit.XAVIER)
								.activation("softmax").weightInit(WeightInit.XAVIER).nIn(numHiddenNodes)
								.nOut(numOutputs).build())
				.pretrain(false).backprop(true).build();

		model = new MultiLayerNetwork(conf);
		model.init();
		model.setListeners(new ScoreIterationListener(10)); // Print score every
															// 10 parameter
															// updates

		for (int n = 0; n < nEpochs; n++)
		{
			model.fit(trainIter);
		}

		System.out.println("Evaluate model....");
		Evaluation eval = new Evaluation(numOutputs);
		while (testIter.hasNext())
		{
			DataSet t = testIter.next();
			INDArray features = t.getFeatureMatrix();
			INDArray lables = t.getLabels();
			INDArray predicted = model.output(features, false);

			eval.eval(lables, predicted);

		}

		// Print the evaluation statistics
		System.out.println(eval.stats());

		System.out.println("****************Example finished********************");

	}

	private RecordReader getReader(final int size)
	{
		return new DynamicRecordReader(DATA_SET_SIZE)
		{

			private static final long serialVersionUID = 1L;

			@Override
			public List<Writable> getNext(int pos)
			{

				if (pos % 4 == RANDOM)
				{

					List<Writable> convertFromPointsToAngles = convertFromPointsToAngles(getRandomPoints(10));
					convertFromPointsToAngles.add(0, new DoubleWriteable(RANDOM));
					return convertFromPointsToAngles;
				}

				if (pos % 4 == cornerAcuteSimulator.getLabel())
				{
					return getScan(cornerAcuteSimulator);
				}
				if (pos % 4 == cornerObtuseSimulater.getLabel())
				{
					return getScan(cornerObtuseSimulater);
				}
				if (pos % 4 == lineSimulator.getLabel())
				{
					return getScan(lineSimulator);
				}

				throw new RuntimeException("Bad type");
			}

		};
	}

	List<Writable> getScan(FeatureSimulatorBase sim)
	{

		Scan scan = sim.getLineScan();
		List<Writable> convertFromPointsToAngles = convertFromPointsToAngles(scan.points);
		convertFromPointsToAngles.add(0, new DoubleWriteable(scan.label));

		return convertFromPointsToAngles;

	}

	CornerAcuteSimulator cornerAcuteSimulator = new CornerAcuteSimulator();

	CornerObtuseSimulator cornerObtuseSimulater = new CornerObtuseSimulator();
	LineSimulator lineSimulator = new LineSimulator();

	List<Vector3D> getRandomPoints(double noise)
	{
		List<Vector3D> points = new LinkedList<>();
		for (int i = 0; i < 7; i++)
		{
			points.add(new Vector3D(getNoise(noise), getNoise(noise), 0));
		}

		return points;

	}

	List<Writable> convertFromPointsToAngles(List<Vector3D> list)
	{
		List<Writable> results = new LinkedList<>();

		Double lastAngle = null;

		for (int i = 1; i < list.size(); i++)
		{
			double deltaX = list.get(i - 1).getX() - list.get(i).getX();
			double deltaY = list.get(i - 1).getY() - list.get(i).getY();
			double length = Vector3D.distance(list.get(i - 1), list.get(i));
			double degrees = Math.toDegrees(Math.atan2(deltaY, deltaX));
			if (lastAngle == null)
			{
				lastAngle = degrees;
			} else
			{
				double delta = lastAngle - degrees;
				if (delta > 180)
				{
					delta = 360 - delta;
				}
				if (delta < -180)
				{
					delta = -360 - delta;
				}
				final Double value = delta;
				results.add(new DoubleWriteable(value));
				final Double value1 = length;
				results.add(new DoubleWriteable(value1));
				lastAngle = degrees;
			}

		}
		return results;

	}

	private double getNoise(double noise)
	{
		return rand.nextDouble() * noise;
	}
}
