package awildgoose.gooseboy;

import org.lwjgl.openal.AL10;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class RawAudioManager {
	private static class PlayingSound {
		int source, buffer;
	}

	private static final List<PlayingSound> active = new ArrayList<>();
	private static final int SAMPLE_RATE = 44100;

	public static void play(byte[] pcm) {
		// TODO check if all this is safe
		ByteBuffer bufferDirect = ByteBuffer.allocateDirect(pcm.length)
				.order(ByteOrder.nativeOrder());
		bufferDirect.put(pcm);
		bufferDirect.flip();

		int buffer = AL10.alGenBuffers();
		AL10.alBufferData(buffer, AL10.AL_FORMAT_MONO16, bufferDirect, SAMPLE_RATE);

		int source = AL10.alGenSources();
		AL10.alSourcei(source, AL10.AL_BUFFER, buffer);
		AL10.alSourcePlay(source);

		PlayingSound ps = new PlayingSound();
		ps.source = source;
		ps.buffer = buffer;
		active.add(ps);
	}

	public static void tick() {
		active.removeIf(ps -> {
			int state = AL10.alGetSourcei(ps.source, AL10.AL_SOURCE_STATE);
			if (state != AL10.AL_PLAYING) {
				AL10.alDeleteSources(ps.source);
				AL10.alDeleteBuffers(ps.buffer);
				return true;
			}
			return false;
		});
	}
}
