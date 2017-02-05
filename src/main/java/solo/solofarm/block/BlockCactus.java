package solo.solofarm.block;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.event.block.BlockGrowEvent;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;

public class BlockCactus extends cn.nukkit.block.BlockCactus{

	public BlockCactus(int meta) {
		super(meta);
	}

	public BlockCactus() {
		this(0);
	}

	@Override
	public int onUpdate(int type) {
		if (type == Level.BLOCK_UPDATE_NORMAL) {
			Block down = getSide(Vector3.SIDE_DOWN);
			if (down.getId() != SAND && down.getId() != CACTUS) {
				this.getLevel().useBreakOn(this);
			} else {
				for (int side = 2; side <= 5; ++side) {
					Block block = getSide(side);
					if (!block.canBeFlowedInto()) {
						this.getLevel().useBreakOn(this);
					}
				}
			}
		} else if (type == Level.BLOCK_UPDATE_RANDOM) {
			if (getSide(0).getId() != CACTUS) {
				if (this.meta >= 8) {
					for (int y = 1; y <= 2; ++y) {
						Block above = this.getLevel().getBlock(new Vector3(this.x, this.y + y, this.z));
						if(above.getId() == CACTUS){
							continue;
						}
						if (above.getId() == AIR) {
							BlockGrowEvent event = new BlockGrowEvent(above, new BlockCactus());
							Server.getInstance().getPluginManager().callEvent(event);
							if (!event.isCancelled()) {
								this.getLevel().setBlock(above, event.getNewState(), true);
							}
							break;
						}
					}
					this.meta = 0;
					this.getLevel().setBlock(this, this);
				} else {
					++this.meta;
					this.getLevel().setBlock(this, this);
				}
			}
		}
		return 0;
	}
}