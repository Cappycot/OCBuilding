package cappycot.ocbuilding;

import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL11.glTranslated;
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
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class RenderBlockChanges {

	public static final int CHUNK_CULL_TICKS = 10; // Delete chunks every this ticks.
	public static final int MAX_CHUNK_DIST = 1; // Delete chunks that are this amount further than the player's current
												// chunk.
	public static final int TRACK_HEIGHT = 8; // Track a total of 16 blocks (8 up and 8 down) for changes.

	public int r = 256;
	public int g = 0;
	public int b = 0;
	public int seq = 0;

	KeyBinding keyTrack;
	KeyBinding keyClear;
	boolean clear = false;
	boolean draw = false;
	boolean ticked = false;

	HashMap<String, IChunk> chunks = new HashMap<String, IChunk>();
	HashMap<String, BlockTracker> trackers;
	LinkedList<BlockPos> positions = new LinkedList<BlockPos>();

	public RenderBlockChanges() {
		// for (int i = 0; i < 3; i++)
		// for (int j = 0; j < 3; j++)
		// trackers[i][j] = new BlockTracker();
		trackers = new HashMap<String, BlockTracker>();
		keyClear = new KeyBinding("ocbuilding.keybind.clearblocks", 296, "key.categories.misc");
		keyTrack = new KeyBinding("ocbuilding.keybind.trackblocks", 299, "key.categories.misc");
		ClientRegistry.registerKeyBinding(keyClear);
		ClientRegistry.registerKeyBinding(keyTrack);
	}

	private int lastTicks = 0;

	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event) {
		if (clear) {
			positions.clear();
			clear = false;
		} else {
			Minecraft minecraft = Minecraft.getInstance();

			if (minecraft.getRenderManager().info.isThirdPerson())
				return;

			Entity entity = minecraft.getRenderViewEntity();

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

			float rf = r / 256F; // idc because it's float in the end.
			float gf = g / 256F;
			float bf = b / 256F;

			GlStateManager.disableTexture();
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			GlStateManager.disableLighting();
			double px = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * event.getPartialTicks();
			double py = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * event.getPartialTicks()
					+ entity.getEyeHeight() - OCBuilding.OFFSET;
			double pz = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * event.getPartialTicks();
			glTranslated(-px, -py, -pz);
			glLineWidth(4.0F);
			glBegin(GL_LINES);
			GlStateManager.color4f(rf, gf, bf, 1.0F);

			for (BlockPos bp : positions) {
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
			// glTranslated(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
			glTranslated(px, py, pz);
			GlStateManager.enableLighting();
			GlStateManager.disableBlend();
			GlStateManager.enableTexture();
		}
	}

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

	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {

		// if (!draw)
		// return;
		Entity entity = Minecraft.getInstance().getRenderViewEntity();
		if (entity == null)
			return;
		int ex = entity.chunkCoordX;
		int ez = entity.chunkCoordZ;
		// if (!chunkChanged(entity)) // Have to run this here as well because of
		// potential server lag.
		// for (int i = 0; i < 3; i++)
		// for (int j = 0; j < 3; j++)
		// if (trackers[i][j].ticks < BlockTracker.TRACK_DELAY)
		// trackers[i][j].ticks += 1;

		if (entity.ticksExisted % CHUNK_CULL_TICKS == 0) {
			// System.out.println(trackers.size());
			Iterator<String> coords = trackers.keySet().iterator();
			while (coords.hasNext()) {
				String s = coords.next();
				BlockTracker tracker = trackers.get(s);
				if (tracker != null) {
					int cx = tracker.cx;
					int cz = tracker.cz;
					if (Math.abs(cx - ex) > MAX_CHUNK_DIST || Math.abs(cz - ez) > MAX_CHUNK_DIST)
						coords.remove();
				}
			}
		}
		ticked = true;
	}

	@SubscribeEvent
	public void onChunkEvent(ChunkEvent.Load event) {
		if (!draw || event.getWorld() == null || !event.getWorld().isRemote())
			return;
		IChunk chunk = ((ChunkEvent) event).getChunk();
		Entity entity = Minecraft.getInstance().getRenderViewEntity();
		if (entity == null)
			return;

		// chunkChanged(entity);

		int cx = chunk.getPos().x;
		int cz = chunk.getPos().z;
		int ex = entity.chunkCoordX;
		int ez = entity.chunkCoordZ;

		if (cx >= ex - MAX_CHUNK_DIST && cx <= ex + MAX_CHUNK_DIST && cz >= ez - MAX_CHUNK_DIST
				&& cz <= ez + MAX_CHUNK_DIST) {
			
			String coords = String.format("%d,%d", cx, cz);
			BlockTracker tracker = trackers.get(coords);
			if (tracker == null) {
				tracker = new BlockTracker(cx, cz);
				trackers.put(coords, tracker);
			}
			if (tracker.ticks + BlockTracker.TRACK_DELAY > entity.ticksExisted || !ticked)
				return;
			ticked = false;
			tracker.ticks = entity.ticksExisted;
			
			// BlockTracker tracker = trackers[1 + cx - ex][1 + cz - ez];
			// if (tracker.ticks < BlockTracker.TRACK_DELAY)
			// return;
			// tracker.ticks = 0;
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
								|| b instanceof AirBlock && b2 instanceof FlowingFluidBlock)) // b != Blocks.WATER && b2
																								// != Blocks.WATER)
							positions.add(bp);
						// System.out.printf("block changed at %d %d %d\n", cx + x, y, cz + z);
						tracker.blocks[x][y][z] = b;
					}
				}
				tracker.compare[y] = true;
			}
		}
	}
}
