package au.com.rsutton.calabrate;

import java.util.concurrent.TimeUnit;

import com.maschel.roomba.RoombaJSSC;
import com.maschel.roomba.RoombaJSSCSerial;
import com.maschel.roomba.song.RoombaNote;
import com.maschel.roomba.song.RoombaNoteDuration;
import com.maschel.roomba.song.RoombaSongNote;

public class RoombaTest
{

	public RoombaTest() throws InterruptedException
	{

		RoombaJSSC roomba = new RoombaJSSCSerial();

		roomba.connect("/dev/ttyUSB0"); // Use portList() to get available
										// ports.

		// Make roomba ready for communication & control (safe mode)
		roomba.startup();

		TimeUnit.SECONDS.sleep(5);

		playsong(roomba);

		// Return to normal (human control) mode
		roomba.stop();

		// Close serial connection
		roomba.disconnect();
	}

	public void playsong(RoombaJSSC roomba)
	{
		// Fur Elise - Beethoven
		RoombaSongNote[] notes = {
				new RoombaSongNote(RoombaNote.E2, RoombaNoteDuration.EightNote),
				new RoombaSongNote(RoombaNote.D2Sharp, RoombaNoteDuration.EightNote),
				new RoombaSongNote(RoombaNote.E2, RoombaNoteDuration.EightNote),
				new RoombaSongNote(RoombaNote.D2Sharp, RoombaNoteDuration.EightNote),

				new RoombaSongNote(RoombaNote.E2, RoombaNoteDuration.EightNote),
				new RoombaSongNote(RoombaNote.B1, RoombaNoteDuration.EightNote),
				new RoombaSongNote(RoombaNote.D2, RoombaNoteDuration.EightNote),
				new RoombaSongNote(RoombaNote.C2, RoombaNoteDuration.EightNote),

				new RoombaSongNote(RoombaNote.A1, RoombaNoteDuration.QuarterNote),
				new RoombaSongNote(RoombaNote.Pause, RoombaNoteDuration.EightNote),
				new RoombaSongNote(RoombaNote.C1, RoombaNoteDuration.EightNote),
				new RoombaSongNote(RoombaNote.E1, RoombaNoteDuration.EightNote),
				new RoombaSongNote(RoombaNote.A1, RoombaNoteDuration.EightNote),
				new RoombaSongNote(RoombaNote.B1, RoombaNoteDuration.QuarterNote),
				new RoombaSongNote(RoombaNote.Pause, RoombaNoteDuration.EightNote),
				new RoombaSongNote(RoombaNote.E1, RoombaNoteDuration.EightNote) };
		// Save to song number 0, tempo (in BPM) 125
		roomba.song(0, notes, 125);
		// Play song 0
		roomba.play(0);
	}
}
