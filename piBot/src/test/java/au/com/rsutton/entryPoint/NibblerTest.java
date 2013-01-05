package au.com.rsutton.entryPoint;

import static org.junit.Assert.*;

import org.junit.Test;

public class NibblerTest {

	
	@Test
	public void charTest()
	{
		Nibbler nib = new Nibbler();

		

		nib.setPinPwmPercentage(0, 0x111);
//		assertTrue(unsignedByteToInt(nib.getBytes()[0]) == 0x10);
//		assertTrue(unsignedByteToInt(nib.getBytes()[1]) == 0xFF);

		String buffer2 = new String();
		for (byte b : nib.getBytes())
		{
			buffer2 += Character.toString((char) b);
		}
		System.out.println(buffer2);
		assertTrue(false);
	}
	@Test
	public void test1() {
		Nibbler nib = new Nibbler();

		nib.setPinPwmPercentage(0, 0);
		assertTrue(unsignedByteToInt(nib.getBytes()[0]) == 0);
		

		nib.setPinPwmPercentage(0, 0x1F);
		assertTrue(unsignedByteToInt(nib.getBytes()[0]) == 0x10);
		assertTrue(unsignedByteToInt(nib.getBytes()[1]) == 0x0F);

		nib.setPinPwmPercentage(0, 0x1FF);
		assertTrue("got " + unsignedByteToInt(nib.getBytes()[0]),
				unsignedByteToInt(nib.getBytes()[0]) == 0xF1);
		assertTrue("got " + unsignedByteToInt(nib.getBytes()[1]),
				unsignedByteToInt(nib.getBytes()[1]) == 0x0F);

		nib.setPinPwmPercentage(0, 0);
		nib.setPinPwmPercentage(1, 0);
		assertTrue(unsignedByteToInt(nib.getBytes()[0]) == 0);
		assertTrue("got " + unsignedByteToInt(nib.getBytes()[1]),
				unsignedByteToInt(nib.getBytes()[1]) == 0);

		nib.setPinPwmPercentage(1, 0x1F);
		assertTrue(unsignedByteToInt(nib.getBytes()[0]) == 0);
		assertTrue("got " + (unsignedByteToInt(nib.getBytes()[1])),
				unsignedByteToInt(nib.getBytes()[1]) == 0);
		assertTrue("got " + nib.getBytes()[2],
				unsignedByteToInt(nib.getBytes()[2]) == 0xF1);

		nib.setPinPwmPercentage(1, 0x1FF);
		assertTrue(unsignedByteToInt(nib.getBytes()[0]) == 0);
		assertTrue("got " + unsignedByteToInt(nib.getBytes()[1]),
				unsignedByteToInt(nib.getBytes()[1]) == 0x10);
		assertTrue("got " + unsignedByteToInt(nib.getBytes()[2]),
				unsignedByteToInt(nib.getBytes()[2]) == 0xFF);

		nib.setPinPwmPercentage(1, 0xFFF);
		assertTrue(unsignedByteToInt(nib.getBytes()[0]) == 0);
		assertTrue("got " + unsignedByteToInt(nib.getBytes()[1]),
				unsignedByteToInt(nib.getBytes()[1]) == 0xF0);
		assertTrue("got " + unsignedByteToInt(nib.getBytes()[2]),
				unsignedByteToInt(nib.getBytes()[2]) == 0xFF);

	}

	int unsignedByteToInt(Byte b) {
		int v = b.intValue();
		if (v < 0)
			v = v + 256;
		return v;
	}
}
