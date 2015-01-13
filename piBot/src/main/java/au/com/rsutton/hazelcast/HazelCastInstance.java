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
		TcpIpConfig tcpIpConfig = config.getNetworkConfig().getJoin().getTcpIpConfig();
        tcpIpConfig.setEnabled(true);
        tcpIpConfig.addMember("192.168.0.115");
        tcpIpConfig.addMember("192.168.0.108");
        tcpIpConfig.addMember("192.168.0.111");
        tcpIpConfig.addMember("192.168.0.118");
        tcpIpConfig.addMember("192.168.0.116");
        tcpIpConfig.addMember("192.168.0.120");
        tcpIpConfig.addMember("192.168.0.106");
        
        
		h = Hazelcast.newHazelcastInstance(config);

	}

	static HazelcastInstance getInstance()
	{
		return SELF.h;
	}

}
