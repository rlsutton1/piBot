package au.com.rsutton.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public enum HazelCastInstance
{
	SELF;

	private Config config = null;
	private HazelcastInstance h;

	HazelCastInstance()
	{

		config = new Config();
		config.getNetworkConfig().getJoin().getMulticastConfig()
				.setEnabled(true);
		TcpIpConfig tcpIpConfig = config.getNetworkConfig().getJoin()
				.getTcpIpConfig();
		tcpIpConfig.setEnabled(true);
		for (int i = 100; i < 122; i++)
		{
			tcpIpConfig.addMember("192.168.0." + i);
		}

		h = Hazelcast.newHazelcastInstance(config);

	}

	static HazelcastInstance getInstance()
	{
		return SELF.h;
	}

}
