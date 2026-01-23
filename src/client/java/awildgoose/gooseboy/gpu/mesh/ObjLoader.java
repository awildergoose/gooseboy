package awildgoose.gooseboy.gpu.mesh;

import awildgoose.gooseboy.Gooseboy;
import awildgoose.gooseboy.gpu.vertex.VertexStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@SuppressWarnings("unused")
public class ObjLoader {
	public static void loadObj(String objPath, ResourceManager resourceManager, VertexStack stack) {
		ResourceLocation location = ResourceLocation.fromNamespaceAndPath(Gooseboy.MOD_ID, objPath);

		try {
			Resource resource = resourceManager.getResource(location)
					.orElseThrow();

			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(resource.open(), StandardCharsets.UTF_8))) {

				ArrayList<Vector3f> positions = new ArrayList<>();
				ArrayList<Vector2f> texCoords = new ArrayList<>();

				String line;
				while ((line = reader.readLine()) != null) {
					line = line.trim();
					if (line.startsWith("v ")) {
						// vertex position
						String[] tokens = line.split("\\s+");
						float x = Float.parseFloat(tokens[1]);
						float y = Float.parseFloat(tokens[2]);
						float z = Float.parseFloat(tokens[3]);
						positions.add(new Vector3f(x, y, z));
					} else if (line.startsWith("vt ")) {
						// texture coordinate
						String[] tokens = line.split("\\s+");
						float u = Float.parseFloat(tokens[1]);
						float v = Float.parseFloat(tokens[2]);
						texCoords.add(new Vector2f(u, 1 - v));
					} else if (line.startsWith("f ")) {
						// face
						String[] tokens = line.split("\\s+");
						for (int i = 1; i < tokens.length; i++) {
							String[] parts = tokens[i].split("/");
							int posIndex = Integer.parseInt(parts[0]) - 1;
							int uvIndex = parts.length > 1 && !parts[1].isEmpty() ? Integer.parseInt(parts[1]) - 1 : 0;

							Vector3f pos = positions.get(posIndex);
							Vector2f uv = texCoords.isEmpty() ? new Vector2f(0, 0) : texCoords.get(uvIndex);
							stack.push(new VertexStack.Vertex(pos.x, pos.y, pos.z, uv.x, uv.y));
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void pushCube(
			VertexStack stack,
			float x0, float y0, float z0,
			float x1, float y1, float z1
	) {
		stack
				.push(v(x0, y0, z1, 0, 0))
				.push(v(x0, y1, z1, 0, 1))
				.push(v(x1, y1, z1, 1, 1))
				.push(v(x1, y0, z1, 1, 0))
				.push(v(x1, y0, z0, 0, 0))
				.push(v(x1, y1, z0, 0, 1))
				.push(v(x0, y1, z0, 1, 1))
				.push(v(x0, y0, z0, 1, 0))
				.push(v(x0, y0, z0, 0, 0))
				.push(v(x0, y1, z0, 0, 1))
				.push(v(x0, y1, z1, 1, 1))
				.push(v(x0, y0, z1, 1, 0))
				.push(v(x1, y0, z1, 0, 0))
				.push(v(x1, y1, z1, 0, 1))
				.push(v(x1, y1, z0, 1, 1))
				.push(v(x1, y0, z0, 1, 0))
				.push(v(x0, y1, z1, 0, 0))
				.push(v(x0, y1, z0, 0, 1))
				.push(v(x1, y1, z0, 1, 1))
				.push(v(x1, y1, z1, 1, 0))
				.push(v(x0, y0, z0, 0, 0))
				.push(v(x0, y0, z1, 0, 1))
				.push(v(x1, y0, z1, 1, 1))
				.push(v(x1, y0, z0, 1, 0));
	}

	private static VertexStack.Vertex v(
			float x, float y, float z,
			float u, float v
	) {
		return new VertexStack.Vertex(
				x, y, z,
				u, v
		);
	}
}
