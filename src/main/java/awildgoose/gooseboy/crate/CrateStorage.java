package awildgoose.gooseboy.crate;

import awildgoose.gooseboy.ConfigManager;
import awildgoose.gooseboy.Gooseboy;
import com.dylibso.chicory.runtime.Instance;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CrateStorage {
	private final ByteBuffer data;
	private final Path filePath;
	private boolean dirty = false;

	// file format handling
	private static final byte[] FF_MAGIC = new byte[]{'g', 's', 'b', 'c', 'r', 'a', 't', 'e'};
	private static final int FF_VERSION = 1;

	public CrateStorage(String name, Path goosePath) {
		this.data = ByteBuffer.allocate(this.size(name));
		this.filePath = resolveFilePath(goosePath, name);
		this.load(name);
	}

	public CrateStorage(Path filePath) throws IOException {
		int size = Math.toIntExact(Files.size(filePath));
		this.data = ByteBuffer.allocate(size);
		this.filePath = filePath;
		this.load(size);
	}

	public static Path resolveFilePath(Path goosePath, String name) {
		return goosePath.resolve("storage")
				.resolve(name + ".gsb");
	}

	public static long getSizeOf(String name, Path goosePath) {
		Path filePath = resolveFilePath(goosePath, name);

		if (!Files.exists(filePath)) {
			return 0;
		}

		try {
			return Files.size(filePath);
		} catch (IOException e) {
			return 0;
		}
	}

	public int read(Instance instance, int offset, int wasmPtr, int len) {
		if (offset < 0 || offset >= this.size(instance)) return 0;
		int toRead = Math.min(len, this.size(instance) - offset);

		byte[] chunk = new byte[toRead];
		System.arraycopy(this.data.array(), offset, chunk, 0, toRead);
		instance.memory()
				.write(wasmPtr, chunk);
		return toRead;
	}

	public int write(Instance instance, int offset, int wasmPtr, int len) {
		if (offset < 0 || offset >= this.size(instance)) return 0;
		int toWrite = Math.min(len, this.size(instance) - offset);

		byte[] chunk = instance.memory()
				.readBytes(wasmPtr, toWrite);
		System.arraycopy(chunk, 0, this.data.array(), offset, toWrite);
		this.dirty = true;
		return toWrite;
	}

	public void clear() {
		Arrays.fill(this.data.array(), (byte) 0);
		this.dirty = true;
	}

	// Slop
	public int size(Instance instance) {
		return this.size(Gooseboy.getCrate(instance));
	}

	public int size(GooseboyCrate crate) {
		return this.size(crate.name);
	}

	public int size(String name) {
		return ConfigManager.getStorageSize(name);
	}

	public byte[] gzipCompressData() {
		try (ByteArrayOutputStream output = new ByteArrayOutputStream();
		     GZIPOutputStream gzip = new GZIPOutputStream(output)) {

			gzip.write(this.data.array());
			gzip.finish();
			return output.toByteArray();
		} catch (IOException e) {
			Gooseboy.LOGGER.error("Failed to compress storage crate:", e);
			return new byte[0];
		}
	}

	public byte[] decompressGZIPData(byte[] data) {
		try (ByteArrayInputStream input = new ByteArrayInputStream(data);
		     GZIPInputStream gzip = new GZIPInputStream(input);
		     ByteArrayOutputStream output = new ByteArrayOutputStream()) {

			byte[] buffer = new byte[4096];
			int len;
			while ((len = gzip.read(buffer)) != -1) {
				output.write(buffer, 0, len);
			}
			return output.toByteArray();
		} catch (IOException e) {
			Gooseboy.LOGGER.error("Failed to decompress storage crate:", e);
			return new byte[0];
		}
	}

	public void load(int size) {
		try {
			if (!Files.exists(this.filePath)) {
				return;
			}

			try (DataInputStream in = new DataInputStream(Files.newInputStream(this.filePath))) {
				byte[] magic = in.readNBytes(FF_MAGIC.length);
				if (!Arrays.equals(magic, FF_MAGIC)) {
					Gooseboy.LOGGER.error("Invalid storage crate file, magic identifier is wrong!");
					return;
				}

				int readVersion = in.readInt();
				if (readVersion != FF_VERSION) {
					Gooseboy.LOGGER.error("Incompatible storage crate version: {}", readVersion);
					return;
				}

				byte compressionCode = in.readByte();
				CompressionType compressionType;
				try {
					compressionType = CompressionType.fromCode(compressionCode);
				} catch (IllegalArgumentException ex) {
					Gooseboy.LOGGER.error("Unknown compression code in storage crate: {}", compressionCode);
					return;
				}

				int compressedLength = in.readInt();
				int uncompressedLength = in.readInt();

				byte[] compressed = in.readNBytes(compressedLength);
				if (compressed.length != compressedLength) {
					Gooseboy.LOGGER.error(
							"Storage crate expected {} compressed bytes but read {}",
							compressedLength, compressed.length);
					return;
				}

				byte[] decompressed = compressed;
				if (compressionType == CompressionType.GZIP) {
					decompressed = this.decompressGZIPData(compressed);
				}

				if (decompressed.length != uncompressedLength) {
					Gooseboy.LOGGER.error(
							"Storage crate uncompressed size {} != expected {}",
							decompressed.length, uncompressedLength);
					return;
				}

				System.arraycopy(decompressed, 0, this.data.array(), 0, Math.min(decompressed.length, size));
			}
		} catch (IOException e) {
			Gooseboy.LOGGER.error("Failed to load WASM storage crate:", e);
		}
	}

	public void load(String name) {
		this.load(this.size(name));
	}

	public byte[] getCompressedData(CompressionType compression) {
		return switch (compression) {
			case NONE -> this.data.array();
			case GZIP -> this.gzipCompressData();
		};
	}

	public void writeSerialized(DataOutputStream out) throws IOException {
		CompressionType compression = CompressionType.NONE;
		byte[] compressedData = this.getCompressedData(compression);

		out.write(FF_MAGIC);
		out.writeInt(FF_VERSION);
		out.writeByte(compression.code);
		out.writeInt(compressedData.length);
		out.writeInt(this.data.array().length);
		out.write(compressedData);
	}

	public void save() {
		if (!this.dirty)
			return;

		try {
			Files.createDirectories(this.filePath.getParent());

			Path temp = this.filePath.resolveSibling(this.filePath.getFileName()
														.toString() + ".tmp");
			try (DataOutputStream out = new DataOutputStream(
					Files.newOutputStream(temp, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
				this.writeSerialized(out);
			}

			try {
				Files.move(temp, this.filePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
			} catch (AtomicMoveNotSupportedException ex) {
				Files.move(temp, this.filePath, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException e) {
			Gooseboy.LOGGER.error("Failed to save WASM storage crate:", e);
		}
	}

	public ByteBuffer readAll() {
		return this.data.duplicate();
	}

	public boolean writeDirect(int offset, ByteBuffer data) {
		int len = data.remaining();
		if (offset < 0 || len > this.data.capacity() - offset) return false;
		this.data.position(offset);
		this.data.put(data);
		this.data.position(offset + len);
		this.dirty = true;
		return true;
	}

	public enum CompressionType {
		NONE((byte) 0),
		GZIP((byte) 1);

		private final byte code;

		CompressionType(byte code) {
			this.code = code;
		}

		public static CompressionType fromCode(byte code) {
			for (CompressionType e : values()) if (e.code == code) return e;
			throw new IllegalArgumentException("Unknown code: " + code);
		}
	}
}
