package cappycot.ocbuilding;

import net.minecraft.block.Block;

public class BlockTracker {

	public static final int TRACK_DELAY = 10;

	public Block[][][] blocks = new Block[16][256][16];
	public boolean[] compare = new boolean[256];
	public int ticks = 0;
	public int cx;
	public int cz;

	public BlockTracker(int x, int z) {
		cx = x;
		cz = z;
	}

	public void reset() {
		for (int k = 0; k < 256; k++)
			compare[k] = false;
		// ticks = BlockTracker.TRACK_DELAY;
	}
}
