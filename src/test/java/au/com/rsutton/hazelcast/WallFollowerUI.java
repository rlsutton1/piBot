package au.com.rsutton.hazelcast;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Collection;

import javax.swing.JFrame;

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
		
		setSize(400, 400);
		setLocation(200, 200);
		setVisible(true);

	}
	
	public void showPoints(Collection<LidarObservation> laserData)
	{
		graph.showPoints(laserData);
		this.repaint();
	}

}
