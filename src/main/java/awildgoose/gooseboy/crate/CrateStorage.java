package awildgoose.gooseboy.crate;

import awildgoose.gooseboy.Gooseboy;
import com.dylibso.chicory.runtime.Memory;

import java.io.*;
import java.nio.file.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CrateStorage {
	public static final int STORAGE_SIZE = 512 * 1024; // 512 KB

	private final byte[] data = new byte[STORAGE_SIZE];
	private final Path filePath;

	public CrateStorage(String name) {
		this.filePath = Gooseboy.getGooseboyDirectory().resolve("storage").resolve(name + ".bin");
		this.load();
	}

	public int read(Memory mem, int offset, int wasmPtr, int len) {
		if (offset < 0 || offset >= STORAGE_SIZE) return 0;
		int toRead = Math.min(len, STORAGE_SIZE - offset);

		byte[] chunk = new byte[toRead];
		System.arraycopy(data, offset, chunk, 0, toRead);
		mem.write(wasmPtr, chunk);
		return toRead;
	}

	public int write(Memory mem, int offset, int wasmPtr, int len) {
		if (offset < 0 || offset >= STORAGE_SIZE) return 0;
		int toWrite = Math.min(len, STORAGE_SIZE - offset);

		byte[] chunk = mem.readBytes(wasmPtr, toWrite);
		System.arraycopy(chunk, 0, data, offset, toWrite);
		return toWrite;
	}

	public void clear() {
		java.util.Arrays.fill(data, (byte) 0);
	}

	public int size() {
		return STORAGE_SIZE;
	}

	// File-Format handling
	private static final int FF_VERSION = 1;

	public byte[] getGZIPCompressedData() {
		try (ByteArrayOutputStream output = new ByteArrayOutputStream();
			 GZIPOutputStream gzip = new GZIPOutputStream(output)) {

			gzip.write(data);
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

	public void load() {
		try {
			Files.createDirectories(filePath.getParent());

			if (!Files.exists(filePath)) {
				return;
			}

			try (DataInputStream in = new DataInputStream(Files.newInputStream(filePath))) {
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
					Gooseboy.LOGGER.error("Storage crate expected {} compressed bytes but read {}",
										  compressedLength, compressed.length);
					return;
				}

				byte[] decompressed = compressed;
				if (compressionType == CompressionType.GZIP) {
					decompressed = this.decompressGZIPData(compressed);
				}

				if (decompressed.length != uncompressedLength) {
					Gooseboy.LOGGER.error("Storage crate uncompressed size {} != expected {}",
										  decompressed.length, uncompressedLength);
					return;
				}

				System.arraycopy(decompressed, 0, data, 0, Math.min(decompressed.length, STORAGE_SIZE));
			}
		} catch (IOException e) {
			Gooseboy.LOGGER.error("Failed to load WASM storage crate:", e);
		}
	}

	public void save() {
		try {
			Files.createDirectories(filePath.getParent());

			var compressedData = this.getGZIPCompressedData();
			var compression = CompressionType.GZIP;

			Path temp = filePath.resolveSibling(filePath.getFileName().toString() + ".tmp");
			try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(temp, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
				out.writeInt(FF_VERSION);
				out.writeByte(compression.code);
				out.writeInt(compressedData.length);
				out.writeInt(data.length);
				out.write(compressedData);
			}

			try {
				Files.move(temp, filePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
			} catch (AtomicMoveNotSupportedException ex) {
				Files.move(temp, filePath, StandardCopyOption.REPLACE_EXISTING);
			}

		} catch (IOException e) {
			Gooseboy.LOGGER.error("Failed to save WASM storage crate:", e);
		}
	}

	enum CompressionType {
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
