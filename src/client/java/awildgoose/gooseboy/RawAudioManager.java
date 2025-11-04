package awildgoose.gooseboy;

import org.lwjgl.openal.AL10;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RawAudioManager {
	private static class PlayingSound {
		int source, buffer;
		long id;
	}

	private static final List<PlayingSound> active = new CopyOnWriteArrayList<>();

	private static long AUDIO_ID;

	// TODO make these configurable
	private static final int MAX_AUDIO_SIZE = 10 * 1024 * 1024; // 10 MB
	private static final int MAX_CONCURRENT_SOUNDS = 32;
	private static final int SAMPLE_RATE = 44100;

	public static long play(byte[] pcm) {
		if (pcm == null || pcm.length > MAX_AUDIO_SIZE) return -1;
		if (pcm.length % 2 != 0) return -1;
		if (active.size() >= MAX_CONCURRENT_SOUNDS) return -1;

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
		ps.id = AUDIO_ID++;
		active.add(ps);
		return ps.id;
	}

	public static void stop(long id) {
		active.removeIf(ps -> {
			if (ps.id == id) {
				AL10.alSourceStop(ps.source);
				AL10.alDeleteSources(ps.source);
				AL10.alDeleteBuffers(ps.buffer);
				return true;
			}
			return false;
		});
	}

	// TODO export to WASM
	public static void stopAllSounds() {
		for (PlayingSound ps : List.copyOf(active)) {
			AL10.alSourceStop(ps.source);
			AL10.alDeleteSources(ps.source);
			AL10.alDeleteBuffers(ps.buffer);
		}
		active.clear();
	}

	// TODO make the limiters configurable
	public static void setVolume(long id, float volume) {
		for (PlayingSound ps : active) {
			if (ps.id == id) {
				float v = Math.max(0f, Math.min(10f, volume));
				AL10.alSourcef(ps.source, AL10.AL_GAIN, v);
				break;
			}
		}
	}

	public static void setPitch(long id, float pitch) {
		for (PlayingSound ps : active) {
			if (ps.id == id) {
				float p = Math.max(0.1f, Math.min(10f, pitch));
				AL10.alSourcef(ps.source, AL10.AL_PITCH, p);
				break;
			}
		}
	}

	public static boolean isPlaying(long id) {
		return active.stream().anyMatch(ps -> ps.id == id);
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
