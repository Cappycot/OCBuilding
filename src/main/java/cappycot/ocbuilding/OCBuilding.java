package cappycot.ocbuilding;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("ocbuilding")
public class OCBuilding {

	public static final double OFFSET = 0.005D;

	public OCBuilding() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
	}

	public void setup(FMLClientSetupEvent event) {

		BlockChangeRenderer blockChangeRenderer = new BlockChangeRenderer();
		Thread blockChangeThread = new Thread(blockChangeRenderer);
		blockChangeThread.start();
		MinecraftForge.EVENT_BUS.register(blockChangeRenderer);
		MinecraftForge.EVENT_BUS.register(new RenderChunkBorders());
		UnsafeBlockRenderer unsafeBlockRenderer = new UnsafeBlockRenderer();
		Thread unsafeBlockThread = new Thread(unsafeBlockRenderer);
		unsafeBlockThread.start();
		MinecraftForge.EVENT_BUS.register(unsafeBlockRenderer);

	}
}
