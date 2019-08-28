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
import java.util.LinkedList;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.block.Block;
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

	public static final int TRACK_HEIGHT = 8; // Track a total of 16 blocks (8 up and 8 down) for changes.

	public int r = 255;
	public int g = 0;
	public int b = 0;
	public int seq = 0;

	KeyBinding keyTrack;
	KeyBinding keyClear;
	boolean draw = false;

	HashMap<String, IChunk> chunks = new HashMap<String, IChunk>();
	int lastEx = Integer.MAX_VALUE;
	int lastEz = Integer.MAX_VALUE;
	BlockTracker[][] trackers = new BlockTracker[3][3];
	LinkedList<BlockPos> positions = new LinkedList<BlockPos>();

	public RenderBlockChanges() {
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++)
				trackers[i][j] = new BlockTracker();
		keyClear = new KeyBinding("ocbuilding.keybind.clearblocks", 296, "key.categories.misc");
		keyTrack = new KeyBinding("ocbuilding.keybind.trackblocks", 299, "key.categories.misc");
		ClientRegistry.registerKeyBinding(keyClear);
		ClientRegistry.registerKeyBinding(keyTrack);
	}

	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event) {

		Minecraft minecraft = Minecraft.getInstance();

		if (minecraft.getRenderManager().info.isThirdPerson())
			return;

		Entity entity = minecraft.getRenderViewEntity();

		switch (seq) {
		case 0:
			g += 5;
			if (g == 255)
				seq++;
			break;
		case 1:
			r -= 5;
			if (r == 0)
				seq++;
			break;
		case 2:
			b += 5;
			if (b == 255)
				seq++;
			break;
		case 3:
			g -= 5;
			if (g == 0)
				seq++;
			break;
		case 4:
			r += 5;
			if (r == 255)
				seq++;
			break;
		default:
			b -= 5;
			if (b == 0)
				seq = 0;
			break;
		}

		float rf = r / 255F;
		float gf = g / 255F;
		float bf = b / 255F;

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

	// 296 / 299
	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		if (event.getAction() == 1) {
			if (event.getKey() == keyClear.getKey().getKeyCode()) {
				positions.clear();
				Minecraft.getInstance().ingameGUI.getChatGUI()
						.printChatMessage(new TranslationTextComponent("ocbuilding.clearchanges"));
			} else if (event.getKey() == keyTrack.getKey().getKeyCode()) {
				draw = !draw;
				Minecraft.getInstance().ingameGUI.getChatGUI().printChatMessage(new TranslationTextComponent(
						draw ? "ocbuilding.trackchangeson" : "ocbuilding.trackchangesoff"));
				if (!draw)
					for (int i = 0; i < 3; i++)
						for (int j = 0; j < 3; j++)
							trackers[i][j].reset();
			}
		}
	}

	private boolean chunkChanged(Entity entity) {

		int ex = entity.chunkCoordX;
		int ez = entity.chunkCoordZ;

		if (lastEx != ex || lastEz != ez) {
			// System.out.println("Start changing chunk.");
			int xc = ex - lastEx;
			int zc = ez - lastEz;
			System.out.printf("Change in x = %d, z = %d\n", xc, zc);
			if (Math.abs(xc) > 2 || Math.abs(zc) > 2)
				for (int i = 0; i < 3; i++)
					for (int j = 0; j < 3; j++)
						trackers[i][j].reset();
			else {
				// x difference
				int xcm = xc + 3;
				BlockTracker t0;
				BlockTracker t1;
				BlockTracker t2;
				for (int z = 0; z < 3; z++) {
					t0 = trackers[xcm % 3][z];
					t1 = trackers[(xcm + 1) % 3][z];
					t2 = trackers[(xcm + 2) % 3][z];
					trackers[0][z] = t0;
					trackers[1][z] = t1;
					trackers[2][z] = t2;
					switch (xc) {
					case -2:
						trackers[1][z].reset();
					case -1:
						trackers[0][z].reset();
						break;
					case 2:
						trackers[1][z].reset();
					case 1:
						trackers[2][z].reset();
						break;
					}
				}
				// z difference
				int zcm = zc + 3;
				for (int x = 0; x < 3; x++) {
					t0 = trackers[x][zcm % 3];
					t1 = trackers[x][(zcm + 1) % 3];
					t2 = trackers[x][(zcm + 2) % 3];
					trackers[x][0] = t0;
					trackers[x][1] = t1;
					trackers[x][2] = t2;
					switch (zc) {
					case -2:
						trackers[x][1].reset();
					case -1:
						trackers[x][0].reset();
						break;
					case 2:
						trackers[x][1].reset();
					case 1:
						trackers[x][2].reset();
						break;
					}
				}
			}
			lastEx = ex;
			lastEz = ez;
			return true;
			// System.out.println("Done changing chunk.");
		} else
			return false;
	}

	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {
		if (!draw)
			return;
		Entity entity = Minecraft.getInstance().getRenderViewEntity();
		if (entity == null)
			return;
		if (!chunkChanged(entity)) // Have to run this here as well because of potential server lag.
			for (int i = 0; i < 3; i++)
				for (int j = 0; j < 3; j++)
					if (trackers[i][j].ticks < BlockTracker.TRACK_DELAY)
						trackers[i][j].ticks += 1;
	}

	@SubscribeEvent
	public void onChunkEvent(ChunkEvent.Load event) {
		if (!draw)
			return;
		else if (event.getWorld() == null || !event.getWorld().isRemote())
			return;
		IChunk chunk = ((ChunkEvent) event).getChunk();
		Entity entity = Minecraft.getInstance().getRenderViewEntity();
		if (entity == null)
			return;

		chunkChanged(entity);

		int cx = chunk.getPos().x;
		int cz = chunk.getPos().z;
		int ex = entity.chunkCoordX;
		int ez = entity.chunkCoordZ;

		if (lastEx != ex || lastEz != ez) {
			// System.out.println("Start changing chunk.");
			int xc = ex - lastEx;
			int zc = ez - lastEz;
			System.out.printf("Change in x = %d, z = %d\n", xc, zc);
			if (Math.abs(xc) > 2 || Math.abs(zc) > 2)
				for (int i = 0; i < 3; i++)
					for (int j = 0; j < 3; j++)
						trackers[i][j].reset();
			else {
				// x difference
				int xcm = xc + 3;
				BlockTracker t0;
				BlockTracker t1;
				BlockTracker t2;
				for (int z = 0; z < 3; z++) {
					t0 = trackers[xcm % 3][z];
					t1 = trackers[(xcm + 1) % 3][z];
					t2 = trackers[(xcm + 2) % 3][z];
					trackers[0][z] = t0;
					trackers[1][z] = t1;
					trackers[2][z] = t2;
					switch (xc) {
					case -2:
						trackers[1][z].reset();
					case -1:
						trackers[0][z].reset();
						break;
					case 2:
						trackers[1][z].reset();
					case 1:
						trackers[2][z].reset();
						break;
					}
				}
				// z difference
				int zcm = zc + 3;
				for (int x = 0; x < 3; x++) {
					t0 = trackers[x][zcm % 3];
					t1 = trackers[x][(zcm + 1) % 3];
					t2 = trackers[x][(zcm + 2) % 3];
					trackers[x][0] = t0;
					trackers[x][1] = t1;
					trackers[x][2] = t2;
					switch (zc) {
					case -2:
						trackers[x][1].reset();
					case -1:
						trackers[x][0].reset();
						break;
					case 2:
						trackers[x][1].reset();
					case 1:
						trackers[x][2].reset();
						break;
					}
				}
			}
			lastEx = ex;
			lastEz = ez;
			// System.out.println("Done changing chunk.");
		}
		if (cx >= ex - 1 && cx <= ex + 1 && cz >= ez - 1 && cz <= ez + 1) {
			BlockTracker tracker = trackers[1 + cx - ex][1 + cz - ez];
			if (tracker.ticks < BlockTracker.TRACK_DELAY)
				return;
			tracker.ticks = 0;
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
						if (b == null)
							continue;
						if (tracker.compare[y] && tracker.blocks[x][y][z] != b)
							positions.add(bp); // System.out.printf("block changed at %d %d %d\n", cx + x, y, cz + z);
						tracker.blocks[x][y][z] = b;
					}
				}
				tracker.compare[y] = true;
			}
		}
	}
}
