package au.com.rsutton.mapping.particleFilter;

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
		setLocation(200, 200);
		setVisible(true);
		add(new DataPanel());

	}

}