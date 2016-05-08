package au.com.rsutton.mapping.v3.impl;

import java.util.Collection;

public interface ScanBufferListener
{

	void processScanData(Collection<ObservedPoint> values);

}
