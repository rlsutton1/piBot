package au.com.rsutton.hazelcast;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Collection;
import java.util.List;

import javax.swing.JFrame;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.robot.rover.LidarObservation;

public class WallFollowerUI extends JFrame
{

	private static final long serialVersionUID = -4490943128993707547L;

	private WallFollowerGraph graph;

	WallFollowerUI()
	{
		this.setBounds(0, 0, 850, 900);

		FlowLayout experimentLayout = new FlowLayout();

		this.setLayout(experimentLayout);

		graph = new WallFollowerGraph();
		graph.setPreferredSize(new Dimension(750, 750));
		this.add(graph);
	}
	
	public void showPoints(Collection<LidarObservation> laserData)
	{
		graph.showPoints(laserData);
		this.repaint();
	}

}
