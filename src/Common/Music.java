package Common;

import javax.sound.midi.*;

public class Music implements MainVocabulary
{
	public Music()
	{
		if(playable)
		{
			try {
				Sequencer player = MidiSystem.getSequencer();
				Sequence seq = new Sequence(Sequence.PPQ,4);
				player.open();
				Track track = seq.createTrack();
				ShortMessage first = new ShortMessage();
				first. setMessage (192, 1, 102, 0);
				MidiEvent changelnstrument = new MidiEvent(first,1);
				track.add(changelnstrument);
				ShortMessage a = new ShortMessage();
				a.setMessage(144, 1, 44, 100);
				MidiEvent noteOn = new MidiEvent(a, 2);
				track.add(noteOn);
				ShortMessage b = new ShortMessage();
				b.setMessage(128, 1, 44, 100);
				MidiEvent noteOff = new MidiEvent(b,16);
				track.add(noteOff);
				float tempoFactor = player.getTempoFactor();
				player.setTempoFactor((float) (tempoFactor * 0.97));
				player.setSequence(seq);
				player.setTempoInBPM(220);
					player.start();
			} catch (MidiUnavailableException ex) {
	            MyLogger.getLogger().info(ex.getMessage());
	            MyLogger.send(ex.getMessage());
			} catch (InvalidMidiDataException ex) {
	            MyLogger.getLogger().info(ex.getMessage());
	            MyLogger.send(ex.getMessage());
			} 
		}
	}
}
