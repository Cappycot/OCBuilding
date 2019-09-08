package cappycot.ocbuilding;

import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL11.glTranslated;
import static org.lwjgl.opengl.GL11.glVertex3d;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class RenderChunkBorders {

	public int r = 255;
	public int g = 0;
	public int b = 0;
	public int seq = 0;

	KeyBinding key;
	boolean draw = false;

	public RenderChunkBorders() {
		key = new KeyBinding("ocbuilding.keybind.chunkborders", 298, "key.categories.misc");
		ClientRegistry.registerKeyBinding(key);
	}

	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event) {

		Minecraft minecraft = Minecraft.getInstance();

		if (!draw || minecraft.getRenderManager().info.isThirdPerson())
			return;

		Entity entity = minecraft.getRenderViewEntity();
		if (entity == null)
			return;

		GlStateManager.disableTexture();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.disableLighting();
		double px = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * event.getPartialTicks();
		double py = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * event.getPartialTicks();
		double pz = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * event.getPartialTicks();
		glTranslated(-px, -py - entity.getEyeHeight() + OCBuilding.OFFSET, -pz);
		glLineWidth(4.0F);
		glBegin(GL_LINES);
		GlStateManager.color4f(1.0F, 0F, 0F, 1.0F);

		for (int cx = -2; cx <= 2; cx++) {
			for (int cz = -2; cz <= 2; cz++) {
				double x1 = (entity.chunkCoordX + cx) << 4;
				double z1 = (entity.chunkCoordZ + cz) << 4;
				double x2 = x1 + 16;
				double z2 = z1 + 16;

				double dy = 128;
				double y1 = Math.floor(py - dy / 2);
				double y2 = y1 + dy;
				if (y1 < 0) {
					y1 = 0;
					y2 = dy;
				}

				if (y1 > entity.world.getHeight()) {
					y2 = entity.world.getHeight();
					y1 = y2 - dy;
				}

				// double dist = Math.pow(2.0, -(cx * cx + cz * cz));

				GlStateManager.color4f(1.0F, 0F, 0F, 0.8F); // (float) dist);
				if (cx >= 0 && cz >= 0) {
					glVertex3d(x2, y1, z2);
					glVertex3d(x2, y2, z2);
				}
				if (cx >= 0 && cz <= 0) {
					glVertex3d(x2, y1, z1);
					glVertex3d(x2, y2, z1);
				}
				if (cx <= 0 && cz >= 0) {
					glVertex3d(x1, y1, z2);
					glVertex3d(x1, y2, z2);
				}
				if (cx <= 0 && cz <= 0) {
					glVertex3d(x1, y1, z1);
					glVertex3d(x1, y2, z1);
				}

				if (cx == 0 && cz == 0) {
					dy = 32;
					y1 = Math.floor(py - dy / 2);
					y2 = y1 + dy;
					if (y1 < 0) {
						y1 = 0;
						y2 = dy;
					}

					if (y1 > entity.world.getHeight()) {
						y2 = entity.world.getHeight();
						y1 = y2 - dy;
					}

					GlStateManager.color4f(0F, 1.0F, 0F, 0.8F);
					for (double y = (int) y1; y <= y2; y++) {
						glVertex3d(x2, y, z1);
						glVertex3d(x2, y, z2);
						glVertex3d(x1, y, z1);
						glVertex3d(x1, y, z2);
						glVertex3d(x1, y, z2);
						glVertex3d(x2, y, z2);
						glVertex3d(x1, y, z1);
						glVertex3d(x2, y, z1);
					}
					for (double h = 1; h <= 15; h++) {
						glVertex3d(x1 + h, y1, z1);
						glVertex3d(x1 + h, y2, z1);
						glVertex3d(x1 + h, y1, z2);
						glVertex3d(x1 + h, y2, z2);
						glVertex3d(x1, y1, z1 + h);
						glVertex3d(x1, y2, z1 + h);
						glVertex3d(x2, y1, z1 + h);
						glVertex3d(x2, y2, z1 + h);
					}

					RayTraceResult mouseOver = minecraft.objectMouseOver;

					if (mouseOver.getType() == RayTraceResult.Type.BLOCK) {
						BlockPos bp = ((BlockRayTraceResult) mouseOver).getPos();
						GlStateManager.color4f(0F, 0.3F, 1.0F, 0.5F);
						double x = bp.getX(); // Math.floor(px);
						double z = bp.getZ(); // Math.floor(pz);
						double x11 = Math.min(x, x1);
						double x21 = Math.max(x + 1, x2);
						double z11 = Math.min(z, z1);
						double z21 = Math.max(z + 1, z2);
						y1 = bp.getY();
						y1 += ((py + entity.getEyeHeight() > y1 + 1) ? 1 : (-OCBuilding.OFFSET * 2)); // py;
						glVertex3d(x11, y1, z);
						glVertex3d(x21, y1, z);
						glVertex3d(x11, y1, z + 1D);
						glVertex3d(x21, y1, z + 1D);
						glVertex3d(x, y1, z11);
						glVertex3d(x, y1, z21);
						glVertex3d(x + 1D, y1, z11);
						glVertex3d(x + 1D, y1, z21);
					}
				}
			}
		}
		glEnd();
		glTranslated(px, py + entity.getEyeHeight() - OCBuilding.OFFSET, pz);
		GlStateManager.enableLighting();
		GlStateManager.disableBlend();
		GlStateManager.enableTexture();
	}

	// 298
	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		if (event.getKey() == key.getKey().getKeyCode() && event.getAction() == 1)
			draw = !draw;
	}
}