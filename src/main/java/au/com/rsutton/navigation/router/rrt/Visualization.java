package au.com.rsutton.navigation.router.rrt;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.commons.lang3.StringUtils;

public class Visualization extends JFrame
{

	private static final long serialVersionUID = -4490943128993707547L;

	JPanel graph;
	BufferedImage image;

	// static public void main(String[] args)
	// {
	// new MapDrawingWindow();
	//
	// }

	public Visualization(String string, int x, int y, int width, int height)
	{
		if (StringUtils.isNotBlank(string))
		{
			setTitle(string);
		}
		this.setBounds(0, 0, width, height);

		FlowLayout experimentLayout = new FlowLayout();

		this.setLayout(experimentLayout);

		graph = new JPanel()
		{

			@Override
			protected void paintComponent(Graphics g)
			{
				super.paintComponent(g);
				g.drawImage(image, 0, 0, null);

			}

		};
		graph.setPreferredSize(new Dimension(width, height));
		this.add(graph);

		setSize(width, height);
		setLocation(x, y);
		setVisible(true);

	}

	public void setImage(BufferedImage image2)
	{
		this.image = image2;
	}

}
