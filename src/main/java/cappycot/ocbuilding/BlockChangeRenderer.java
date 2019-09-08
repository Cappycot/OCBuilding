package cappycot.ocbuilding;

import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL11.glVertex3d;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class BlockChangeRenderer extends LineRenderer {

	public static final long MILLIS_REST = 10L;
	public static final int CHUNK_RANGE = 1;
	private static final int SIDE_LENGTH = CHUNK_RANGE * 2 + 1;
	private static final int SQUARE_LENGTH = SIDE_LENGTH * SIDE_LENGTH;
	public static final int TRACK_HEIGHT = 16;
	private static final int[][] CHUNKS;
	static {
		CHUNKS = new int[SQUARE_LENGTH][2];
		for (int x = 0; x < SIDE_LENGTH; x++) {
			for (int z = 0; z < SIDE_LENGTH; z++) {
				CHUNKS[x * SIDE_LENGTH + z][0] = CHUNK_RANGE - x;
				CHUNKS[x * SIDE_LENGTH + z][1] = CHUNK_RANGE - z;
			}
		}
	}

	private KeyBinding keyTrack;
	private KeyBinding keyClear;

	private int ctr = 0;
	private HashMap<String, BlockTracker> trackers;
	private boolean clear = false;
	private boolean update = false;
	private LinkedList<BlockPos> positions;
	private LinkedList<BlockPos> renderPositions;

	public BlockChangeRenderer() {
		positions = new LinkedList<BlockPos>();
		trackers = new HashMap<String, BlockTracker>();
		keyClear = new KeyBinding("ocbuilding.keybind.clearblocks", 296, "key.categories.misc");
		keyTrack = new KeyBinding("ocbuilding.keybind.trackblocks", 299, "key.categories.misc");
		ClientRegistry.registerKeyBinding(keyClear);
		ClientRegistry.registerKeyBinding(keyTrack);
	}

	private int r = 256;
	private int g = 0;
	private int b = 0;
	private int seq = 0;
	private int lastTicks = 0;

	// 296 / 299
	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		if (event.getAction() == 1) {
			if (event.getKey() == keyClear.getKey().getKeyCode()) {
				clear = true; // positions.clear();
				Minecraft.getInstance().ingameGUI.getChatGUI()
						.printChatMessage(new TranslationTextComponent("ocbuilding.clearchanges"));
			} else if (event.getKey() == keyTrack.getKey().getKeyCode()) {
				draw = !draw;
				Minecraft.getInstance().ingameGUI.getChatGUI().printChatMessage(new TranslationTextComponent(
						draw ? "ocbuilding.trackchangeson" : "ocbuilding.trackchangesoff"));
				if (!draw)
					trackers.clear();
				// for (int i = 0; i < 3; i++)
				// for (int j = 0; j < 3; j++)
				// trackers[i][j].reset();
			}
		}
	}

	@Override
	public void render(Entity entity) {

		if (update) {
			renderPositions = new LinkedList<BlockPos>(positions);
			update = false;
		} else if (renderPositions == null)
			return;

		if (lastTicks != entity.ticksExisted) {
			lastTicks = entity.ticksExisted;
			switch (seq) {
			case 0:
				g += 8;
				if (g == 256)
					seq++;
				break;
			case 1:
				r -= 8;
				if (r == 0)
					seq++;
				break;
			case 2:
				b += 8;
				if (b == 256)
					seq++;
				break;
			case 3:
				g -= 8;
				if (g == 0)
					seq++;
				break;
			case 4:
				r += 8;
				if (r == 256)
					seq++;
				break;
			default:
				b -= 8;
				if (b == 0)
					seq = 0;
				break;
			}
		}

		glLineWidth(4.0F);
		glBegin(GL_LINES);
		// idc because it's float in the end.
		GlStateManager.color4f(r / 256F, g / 256F, b / 256F, 1.0F);

		for (BlockPos bp : renderPositions) {
			int x = bp.getX();
			int y = bp.getY();
			int z = bp.getZ();

			glVertex3d(x, y, z);
			glVertex3d(x + 1, y, z);
			glVertex3d(x + 1, y, z);
			glVertex3d(x + 1, y, z + 1);
			glVertex3d(x + 1, y, z + 1);
			glVertex3d(x, y, z + 1);
			glVertex3d(x, y, z + 1);
			glVertex3d(x, y, z);

			glVertex3d(x, y, z);
			glVertex3d(x, y + 1, z);
			glVertex3d(x + 1, y, z);
			glVertex3d(x + 1, y + 1, z);
			glVertex3d(x + 1, y, z + 1);
			glVertex3d(x + 1, y + 1, z + 1);
			glVertex3d(x, y, z + 1);
			glVertex3d(x, y + 1, z + 1);

			glVertex3d(x, y + 1, z);
			glVertex3d(x + 1, y + 1, z);
			glVertex3d(x + 1, y + 1, z);
			glVertex3d(x + 1, y + 1, z + 1);
			glVertex3d(x + 1, y + 1, z + 1);
			glVertex3d(x, y + 1, z + 1);
			glVertex3d(x, y + 1, z + 1);
			glVertex3d(x, y + 1, z);
		}

		glEnd();
	}

	@Override
	public void calculate() {
		while (true) {
			try {
				Thread.sleep(MILLIS_REST);
			} catch (InterruptedException e) {
				return;
			}
			if (update)
				continue;

			boolean changed = false;
			if (clear) { // TODO: Redo logic for this part.
				positions.clear();
				clear = false;
				changed = true;
				update = true;
			}

			if (!draw)
				continue;

			Minecraft minecraft = Minecraft.getInstance();
			if (minecraft == null)
				continue;
			Entity entity = Minecraft.getInstance().getRenderViewEntity();
			if (entity == null)
				continue;
			World world = entity.world;
			if (world == null)
				continue;

			int ex = entity.chunkCoordX;
			int ez = entity.chunkCoordZ;
			if (ctr % SQUARE_LENGTH == 0) {
				// System.out.println(trackers.size());
				Iterator<String> coords = trackers.keySet().iterator();
				while (coords.hasNext()) {
					String s = coords.next();
					BlockTracker tracker = trackers.get(s);
					if (tracker != null) {
						int cx = tracker.cx;
						int cz = tracker.cz;
						if (Math.abs(cx - ex) > CHUNK_RANGE || Math.abs(cz - ez) > CHUNK_RANGE)
							coords.remove(); // TODO: Feels like a leak.
					}
				}
			}
			int cx = ex + CHUNKS[ctr][0];
			int cz = ez + CHUNKS[ctr][1];
			ctr++;
			ctr %= SQUARE_LENGTH;

			Chunk chunk = world.getChunk(cx, cz);
			if (chunk == null)
				continue;

			String coords = String.format("%d,%d", cx, cz);
			BlockTracker tracker = trackers.get(coords);
			if (tracker == null) {
				tracker = new BlockTracker(cx, cz);
				trackers.put(coords, tracker);
			}
			int ey = (int) Math.floor(entity.posY);
			cx *= 16;
			cz *= 16;
			int yl = Math.max(0, ey - TRACK_HEIGHT);
			int yu = Math.min(256, ey + TRACK_HEIGHT);
			for (int y = yl; y < yu; y++) {
				for (int x = 0; x < 16; x++) {
					for (int z = 0; z < 16; z++) {
						BlockPos bp = new BlockPos(cx + x, y, cz + z);
						Block b = chunk.getBlockState(bp).getBlock();
						Block b2 = tracker.blocks[x][y][z];
						if (b == null)
							continue;
						if (tracker.compare[y] && b != b2 && !(b instanceof FlowingFluidBlock && b2 instanceof AirBlock
								|| b instanceof AirBlock && b2 instanceof FlowingFluidBlock)) {
							positions.add(bp);
							changed = true;
						}
						// System.out.printf("block changed at %d %d %d\n", cx + x, y, cz + z);
						tracker.blocks[x][y][z] = b;
					}
				}
				tracker.compare[y] = true;
			}

			update = changed;
		}
	}
}
