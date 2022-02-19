package au.com.rsutton.hazelcast;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public enum HazelCastInstance
{
	SELF;

	private Config config = null;
	private HazelcastInstance h;

	HazelCastInstance()
	{

		String hostname;
		InetAddress ip;
		try
		{
			ip = InetAddress.getLocalHost();
			hostname = ip.getHostName();
			System.out.println("Your current IP address : " + ip);
			System.out.println("Your current Hostname : " + hostname);

		} catch (UnknownHostException e)
		{

			e.printStackTrace();
		}

		config = new Config();
		config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(true);
		// TcpIpConfig tcpIpConfig =
		// config.getNetworkConfig().getJoin().getTcpIpConfig();
		// tcpIpConfig.setEnabled(true);
		// for (int i = 2; i < 30; i++)
		// {
		// tcpIpConfig.addMember("192.168.0." + i);
		// }

		h = Hazelcast.newHazelcastInstance(config);

	}

	static HazelcastInstance getInstance()
	{
		return SELF.h;
	}

}
