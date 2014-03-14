package au.com.rsutton.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.entryPoint.units.Speed;
import au.com.rsutton.entryPoint.units.Time;
import au.com.rsutton.hazelcast.RobotLocation;
import au.com.rsutton.hazelcast.SetMotion;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

public class MainWindow extends JFrame implements
		MessageListener<RobotLocation>
{

	private static final long serialVersionUID = -4490943128993707547L;
	private JTextField heading;
	private JLabel xLocationLabel;
	private JLabel yLocationLabel;
	private JLabel headingLabel;
	private JLabel spaceLabel;

	public static void main(String[] args) throws InterruptedException,
			IOException
	{
		new MainWindow();

	}

	MainWindow()
	{
		this.setBounds(0, 0, 600, 400);

		FlowLayout experimentLayout = new FlowLayout();

		this.setLayout(experimentLayout);

		FlowLayout telemetryLayout = new FlowLayout();
		JPanel telemetryPanel = new JPanel(telemetryLayout);
		telemetryPanel.setName("Telemetry");
		telemetryPanel.setBorder(BorderFactory.createLineBorder(new Color(0)));
		this.add(telemetryPanel);

		xLocationLabel = new JLabel("X: 0");
		xLocationLabel.setPreferredSize(new Dimension(80, 30));
		yLocationLabel = new JLabel("Y: 0");
		yLocationLabel.setPreferredSize(new Dimension(80, 30));
		headingLabel = new JLabel("H: 0");
		headingLabel.setPreferredSize(new Dimension(80, 30));
		spaceLabel = new JLabel("S: 0");
		spaceLabel.setPreferredSize(new Dimension(80, 30));

		telemetryPanel.add(xLocationLabel);
		telemetryPanel.add(yLocationLabel);
		telemetryPanel.add(headingLabel);
		telemetryPanel.add(spaceLabel);
		this.add(telemetryPanel);

		RobotLocation locationMessage = new RobotLocation();
		locationMessage.addMessageListener(this);

		FlowLayout controlLayout = new FlowLayout();
		JPanel controlPanel = new JPanel(controlLayout);
		controlPanel.setName("Control");
		controlPanel.setBorder(BorderFactory.createLineBorder(new Color(0)));
		this.add(controlPanel);

		heading = new JTextField("0", 5);

		controlPanel.add(heading);
		controlPanel.add(createHeadingButton());

		JButton forwardButton = createForwardButton();
		controlPanel.add(createStopButton());
		controlPanel.add(forwardButton);
		this.setVisible(true);
	}

	private JButton createForwardButton()
	{
		JButton b = new JButton("Forward");
		b.setSize(50, 30);
		b.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				SetMotion message = new SetMotion();
				message.setSpeed(new Speed(new Distance(50, DistanceUnit.CM),
						Time.perSecond()));
				int v = Integer.parseInt(heading.getText());
				message.setHeading((double) v);
				message.publish();

			}
		});

		return b;
	}

	private JButton createStopButton()
	{
		JButton b = new JButton("Stop");
		b.setSize(50, 30);
		b.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				SetMotion message = new SetMotion();
				message.setSpeed(new Speed(new Distance(0, DistanceUnit.CM),
						Time.perSecond()));
				int v = Integer.parseInt(heading.getText());
				message.setHeading((double) v);
				message.publish();

			}
		});

		return b;
	}

	private JButton createHeadingButton()
	{
		JButton b = new JButton("Set Heading");
		b.setSize(50, 30);
		b.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				SetMotion message = new SetMotion();
				message.setSpeed(new Speed(new Distance(0, DistanceUnit.CM),
						Time.perSecond()));
				int v = Integer.parseInt(heading.getText());
				message.setHeading((double) v);
				message.publish();

			}
		});

		return b;
	}

	@Override
	public void onMessage(Message<RobotLocation> message)
	{
		RobotLocation m = message.getMessageObject();

		xLocationLabel.setText("X:" + (int) m.getX().convert(DistanceUnit.CM)
				+ "cm");
		yLocationLabel.setText("Y:" + (int) m.getY().convert(DistanceUnit.CM)
				+ "cm");
		headingLabel.setText("H:" + (int) m.getHeading());
		spaceLabel.setText("S:"
				+ (int) m.getClearSpaceAhead().convert(DistanceUnit.CM) + "cm");
	}
}
