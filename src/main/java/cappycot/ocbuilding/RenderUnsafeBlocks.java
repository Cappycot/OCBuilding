package cappycot.ocbuilding;

import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL11.glTranslated;
import static org.lwjgl.opengl.GL11.glVertex3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class RenderUnsafeBlocks {

	public static final int UPDATE_TICKS = 10;
	public static final int RANGE = 8;

	KeyBinding key;
	boolean draw = false;

	public HashMap<String, BlockWarning> positions;
	private boolean update = false;
	public ArrayList<BlockWarning> renderPositions = null;

	public RenderUnsafeBlocks() {
		positions = new HashMap<String, BlockWarning>();
		renderPositions = new ArrayList<BlockWarning>();
		key = new KeyBinding("ocbuilding.keybind.unsafe", 295, "key.categories.misc");
		ClientRegistry.registerKeyBinding(key);
	}

	private class BlockWarning {
		public BlockPos pos;
		public int skyLight;

		public BlockWarning(BlockPos pos, int skyLight) {
			this.pos = pos;
			this.skyLight = skyLight;
		}
	}

	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {
		if (!draw)
			return;

		Entity entity = Minecraft.getInstance().getRenderViewEntity();
		if (entity == null || entity.ticksExisted % UPDATE_TICKS != 0 || update)
			return;

		World world = entity.world;

		boolean changed = false;
		BlockPos entityPos = entity.getPosition();
		Iterator<String> keys = positions.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			BlockWarning bw = positions.get(key);
			if (bw != null) {
				BlockPos pos = bw.pos;
				if (Math.abs(pos.getX() - entityPos.getX()) > RANGE || Math.abs(pos.getZ() - entityPos.getZ()) > RANGE
						|| Math.abs(pos.getY() - entityPos.getY()) > RANGE) {
					changed = true;
					keys.remove();
				}
			}
		}
		for (int i = -RANGE; i < 3; i++) {
			int y = entityPos.getY() + i;
			if (y < 0 || y > 254)
				continue;
			for (int j = -RANGE; j <= RANGE; j++) {
				int x = entityPos.getX() + j;
				for (int k = -RANGE; k <= RANGE; k++) {
					int z = entityPos.getZ() + k;
					BlockPos blockPos = new BlockPos(x, y, z);
					BlockPos blockPos2 = new BlockPos(x, y + 1, z);
					BlockState blockState = world.getBlockState(blockPos);
					BlockState blockState2 = world.getBlockState(blockPos2);
					Block block = blockState.getBlock();
					Block block2 = blockState2.getBlock();
					String key = String.format("%d,%d,%d", x, y, z);
					/*
					 * if (block == Blocks.AIR || block instanceof BarrierBlock) { if
					 * (positions.get(key) != null) { changed = true; positions.remove(key); }
					 * continue; }
					 */
					int blockLight = world.getLightFor(LightType.BLOCK, blockPos2);
					int skyLight = world.getLightFor(LightType.SKY, blockPos2);
					if ((blockState.isSolid() && Block.isOpaque(blockState.getShape(world, blockPos))
							|| block instanceof FarmlandBlock)
							&& !(blockState2.isSolid() && Block.isOpaque(blockState2.getShape(world, blockPos2)))
							&& !(block2 instanceof LeavesBlock || block2 instanceof FlowingFluidBlock)
							&& blockLight < 8) {
						BlockWarning bw = positions.get(key);
						if (bw == null) {
							changed = true;
							positions.put(key, new BlockWarning(blockPos, skyLight));
						} else if (bw.skyLight != skyLight) {
							changed = true;
							bw.skyLight = skyLight;
						}
					} else if (positions.get(key) != null) {
						changed = true;
						positions.remove(key);
					}
				}
			}
		}
		update = changed;
	}

	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event) {
		if (!draw)
			return;
		Minecraft minecraft = Minecraft.getInstance();

		if (minecraft.getRenderManager().info.isThirdPerson())
			return;

		Entity entity = minecraft.getRenderViewEntity();
		if (entity == null)
			return;
		if (update) {
			renderPositions = new ArrayList<BlockWarning>(positions.values());
			update = false;
		}

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

		for (BlockWarning bw : renderPositions) {
			GlStateManager.color4f(1.0F, bw.skyLight > 7 ? 1.0F : 0F, 0F, 1.0F);
			int x = bw.pos.getX();
			int y = bw.pos.getY();
			int z = bw.pos.getZ();

			glVertex3d(x, y + 1, z);
			glVertex3d(x + 1, y + 1, z + 1);
			glVertex3d(x + 1, y + 1, z);
			glVertex3d(x, y + 1, z + 1);
		}

		glEnd();
		// glTranslated(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
		glTranslated(px, py, pz);
		GlStateManager.enableLighting();
		GlStateManager.disableBlend();
		GlStateManager.enableTexture();
	}

	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		if (event.getKey() == key.getKey().getKeyCode() && event.getAction() == 1) {
			draw = !draw;
		}
	}
}
