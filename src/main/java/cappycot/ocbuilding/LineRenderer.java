package cappycot.ocbuilding;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glTranslated;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public abstract class LineRenderer implements Runnable {

	protected boolean draw;

	// Don't really know why I'm doing it this way but ok.
	public final void run() {
		calculate();
	}

	// TODO: Decide whether to consolidate all line renderers into a single glBegin.
	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event) {

		Minecraft minecraft = Minecraft.getInstance();

		if (minecraft.getRenderManager().info.isThirdPerson())
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
		glTranslated(-px, -py - entity.getEyeHeight(), -pz);

		render(entity);

		glTranslated(px, py + entity.getEyeHeight(), pz);
		GlStateManager.enableLighting();
		GlStateManager.disableBlend();
		GlStateManager.enableTexture();
	}

	public abstract void render(@Nonnull Entity entity);

	/**
	 * Calculations made on separate thread so that we get more frames I guess...
	 */
	public abstract void calculate();
}
