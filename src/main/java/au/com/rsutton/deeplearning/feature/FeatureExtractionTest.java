package au.com.rsutton.deeplearning.feature;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.datavec.api.conf.Configuration;
import org.datavec.api.records.listener.RecordListener;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.split.InputSplit;
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

public class FeatureExtractionTest
{

	Random rand = new Random();

	@Test
	public void test1()
	{
		RecordReader trainer = getReader(1000);
		RecordReader test = getReader(1000);

		dl4jText(trainer, test);
	}

	void dl4jText(RecordReader trainer, RecordReader test)
	{

		int seed = 123;
		double learningRate = 0.005;
		int batchSize = 50;
		int nEpochs = 200;

		int numInputs = 5;
		int numOutputs = 3;
		int numHiddenNodes = 50;

		// Load the training data:
		DataSetIterator trainIter = new RecordReaderDataSetIterator(trainer, batchSize, 0, 3);

		// Load the test/evaluation data:
		DataSetIterator testIter = new RecordReaderDataSetIterator(test, batchSize, 0, 3);

		MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder().seed(seed).iterations(1)
				.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).learningRate(learningRate)
				.updater(Updater.NESTEROVS).momentum(0.9).list()
				.layer(0,
						new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes).weightInit(WeightInit.XAVIER)
								.activation("relu").build())
				.layer(1,
						new OutputLayer.Builder(LossFunction.NEGATIVELOGLIKELIHOOD).weightInit(WeightInit.XAVIER)
								.activation("softmax").weightInit(WeightInit.XAVIER).nIn(numHiddenNodes)
								.nOut(numOutputs).build())
				.pretrain(false).backprop(true).build();

		MultiLayerNetwork model = new MultiLayerNetwork(conf);
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
		return new RecordReader()
		{

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			int pos = 0;

			@Override
			public void setConf(Configuration conf)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public Configuration getConf()
			{
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void close() throws IOException
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void setListeners(Collection<RecordListener> listeners)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void setListeners(RecordListener... listeners)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void reset()
			{
				pos = 0;

			}

			@Override
			public List<Writable> record(URI uri, DataInputStream dataInputStream) throws IOException
			{
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public List<Writable> next()
			{
				pos++;

				// if (pos % 3 == 0)
				// {
				//
				// List<Writable> convertFromPointsToAngles =
				// convertFromPointsToAngles(getRandomPoints(10));
				// convertFromPointsToAngles.add(0, getWritable(0.0));
				// // System.out.println(convertFromPointsToAngles.toString());
				// return convertFromPointsToAngles;
				// }
				//
				// if (pos % 3 == 1)
				// {
				//
				// List<Writable> convertFromPointsToAngles =
				// convertFromPointsToAngles(
				// getObtuseCorner(new Rotation(RotationOrder.XYZ, 0, 0,
				// rand.nextInt(180)), 3));
				// convertFromPointsToAngles.add(0, getWritable(1.0));
				// // System.out.println(convertFromPointsToAngles.toString());
				// return convertFromPointsToAngles;
				// }

				List<Writable> convertFromPointsToAngles = convertFromPointsToAngles(
						getLine(new Rotation(RotationOrder.XYZ, 0, 0, rand.nextInt(180)), 3));
				convertFromPointsToAngles.add(0, getWritable(2.0));

				// System.out.println(convertFromPointsToAngles.toString());

				return convertFromPointsToAngles;
			}

			@Override
			public void initialize(Configuration conf, InputSplit split) throws IOException, InterruptedException
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void initialize(InputSplit split) throws IOException, InterruptedException
			{
				// TODO Auto-generated method stub

			}

			@Override
			public boolean hasNext()
			{
				return pos < size;
			}

			@Override
			public List<RecordListener> getListeners()
			{
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public List<String> getLabels()
			{
				// TODO Auto-generated method stub
				return null;
			}
		};
	}

	List<Vector3D> getObtuseCorner(Rotation rotation, double noise)
	{
		List<Vector3D> points = new LinkedList<>();

		points.add(new Vector3D(-30 + getNoise(noise), 0 + getNoise(noise), 0));
		points.add(new Vector3D(-20 + getNoise(noise), 0 + getNoise(noise), 0));
		points.add(new Vector3D(-10 + getNoise(noise), 0 + getNoise(noise), 0));

		points.add(new Vector3D(0 + getNoise(noise), 0 + getNoise(noise), 0));

		points.add(new Vector3D(0 + getNoise(noise), 10 + getNoise(noise), 0));
		points.add(new Vector3D(0 + getNoise(noise), 20 + getNoise(noise), 0));
		points.add(new Vector3D(0 + getNoise(noise), 30 + getNoise(noise), 0));

		List<Vector3D> rotated = new LinkedList<>();
		for (Vector3D point : points)
		{
			rotated.add(rotation.applyTo(point));
		}

		return rotated;

	}

	List<Vector3D> getLine(Rotation rotation, double noise)
	{
		List<Vector3D> points = new LinkedList<>();

		points.add(new Vector3D(-30 + getNoise(noise), 0 + getNoise(noise), 0));
		points.add(new Vector3D(-20 + getNoise(noise), 0 + getNoise(noise), 0));
		points.add(new Vector3D(-10 + getNoise(noise), 0 + getNoise(noise), 0));

		points.add(new Vector3D(0 + getNoise(noise), 0 + getNoise(noise), 0));

		points.add(new Vector3D(10 + getNoise(noise), 0 + getNoise(noise), 0));
		points.add(new Vector3D(20 + getNoise(noise), 0 + getNoise(noise), 0));
		points.add(new Vector3D(30 + getNoise(noise), 0 + getNoise(noise), 0));

		List<Vector3D> rotated = new LinkedList<>();
		for (Vector3D point : points)
		{
			rotated.add(rotation.applyTo(point));
		}

		return rotated;

	}

	List<Vector3D> getRandomPoints(double noise)
	{
		List<Vector3D> points = new LinkedList<>();
		for (int i = 0; i < 7; i++)
		{
			points.add(new Vector3D(getNoise(noise), getNoise(noise), 0));
		}

		return points;

	}

	Writable getWritable(final Double value)
	{
		return new Writable()
		{

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public String toString()
			{
				return "" + value;
			}

			@Override
			public void write(DataOutput out) throws IOException
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void readFields(DataInput in) throws IOException
			{
				// TODO Auto-generated method stub

			}

			@Override
			public double toDouble()
			{
				return value;
			}

			@Override
			public float toFloat()
			{
				// TODO Auto-generated method stub
				return value.floatValue();
			}

			@Override
			public int toInt()
			{
				// TODO Auto-generated method stub
				return value.intValue();
			}

			@Override
			public long toLong()
			{
				// TODO Auto-generated method stub
				return value.longValue();
			}
		};
	}

	List<Writable> convertFromPointsToAngles(List<Vector3D> list)
	{
		List<Writable> results = new LinkedList<>();

		Double lastAngle = null;

		for (int i = 1; i < list.size(); i++)
		{
			double deltaX = list.get(i - 1).getX() - list.get(i).getX();
			double deltaY = list.get(i - 1).getY() - list.get(i).getY();
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
				results.add(getWritable(delta));
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
