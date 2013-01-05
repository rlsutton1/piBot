package au.com.rsutton.entryPoint;

public class Nibbler
{

	long[] bytes = new long[24];

	Nibbler()
	{

		for (int i = 0; i < 24; i++)
		{
			bytes[i] = 0;
		}
	}

	public byte[] getBytes()
	{
		byte ret[] = new byte[24];
		for (int i = 0; i < 24; i++)
		{
			// eliminate the sign
			ret[i] = (byte) (bytes[i] & 0xFF);
		}
		return ret;

	}

	public void setPinPwmPercentage(int pin, int percent)
	{

		// split out the 3 nibbles
		long tnibbles[] = new long[] { ((percent >> 0) & 0x0F), ((percent >> 4) & 0x0F), ((percent >> 8) & 0x0F), };

		long nibbles[] = new long[3];
		int c = 0;
		// invert the nibbles
		for (long nibble : tnibbles)
		{
			nibbles[c++] = ((nibble & 1) << 3) + ((nibble & 2) << 1) + ((nibble & 4) >> 1) + ((nibble & 8) >> 3);
		}

		// insert the nibbles into the byte array
		for (int i = 0; i < 3; i++)
		{
			long pos = (pin * 3) + i;
			int b = (int) (pos / 2);
			int n = (int) (pos % 2);
			long mask = 0x0F << (((n + 1) % 2) * 4);

			bytes[b] = (bytes[b] & mask | nibbles[i] << (n * 4));
		}
	}
}
