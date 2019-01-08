package au.com.rsutton.mapping.particleFilter;

import java.awt.Dimension;

import javax.swing.JFrame;

public class DataWindow extends JFrame
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DataWindow()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setSize(400, 400);
		setLocation(200, 600);
		setVisible(true);
		DataPanel dataPanel = new DataPanel();
		add(dataPanel);

		dataPanel.setPreferredSize(new Dimension(400, 400));
		dataPanel.setVisible(true);
		this.setBounds(200, 600, 410, 410);

	}

}