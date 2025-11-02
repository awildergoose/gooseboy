package awildgoose.gooseboy.storage;

import awildgoose.gooseboy.Gooseboy;
import com.dylibso.chicory.runtime.Memory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class StorageCrate {
	public static final int STORAGE_SIZE = 512 * 1024; // 512 KB

	private final String name;
	private final byte[] data = new byte[STORAGE_SIZE];
	private final Path filePath;

	public StorageCrate(String name) {
		this.name = name;
		this.filePath = Gooseboy.getGooseboyDirectory().resolve("storage").resolve(name + ".bin");

		byte[] fileData;
		try {
			fileData = Files.readAllBytes(filePath);
		} catch (IOException e) {
			fileData = new byte[STORAGE_SIZE];
		}
		System.arraycopy(fileData, 0, data, 0, Math.min(fileData.length, STORAGE_SIZE));
	}

	public String getName() {
		return name;
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

	public void save() {
		try {
			Files.createDirectories(filePath.getParent());
			Files.write(filePath, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			Gooseboy.LOGGER.error("Failed to save WASM storage crate:");
			e.printStackTrace();
		}
	}

	public int size() {
		return STORAGE_SIZE;
	}
}
