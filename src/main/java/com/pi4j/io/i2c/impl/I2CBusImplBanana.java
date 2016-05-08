package com.pi4j.io.i2c.impl;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;

public class I2CBusImplBanana extends I2CBusImpl
{
	   /** Singleton instance of bus 0 */
    private static I2CBus bus0 = null;

    /** Singleton instance of bus 1 */
    private static I2CBus bus1 = null;


    /** Singleton instance of bus 2 */
    private static I2CBus bus2 = null;

	public I2CBusImplBanana(String filename) throws IOException
	{
		super(filename);
		// TODO Auto-generated constructor stub
	}
	
    public static I2CBus getBus(int busNumber) throws IOException {
        I2CBus bus = null;
        if (busNumber == 0) {
            bus = bus0;
            if (bus == null) {
                bus = new I2CBusImpl("/dev/i2c-0");
                bus0 = bus;
            }
        } else if (busNumber == 1) {
            bus = bus1;
            if (bus == null) {
                bus = new I2CBusImpl("/dev/i2c-1");
                bus1 = bus;
            }
        } else if (busNumber == 2) {
            bus = bus2;
            if (bus == null) {
                bus = new I2CBusImpl("/dev/i2c-2");
                bus2 = bus;
            }
        } else {
            throw new IOException("Unknown bus number " + busNumber);
        }
        return bus;
    }

}
