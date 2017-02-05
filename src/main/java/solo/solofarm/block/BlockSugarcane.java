package solo.solofarm.block;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.item.Item;
import cn.nukkit.event.block.BlockGrowEvent;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;

public class BlockSugarcane extends cn.nukkit.block.BlockSugarcane{

	public int ohmygod_Metadoesntwork = 0;

	public BlockSugarcane(int meta) {
		super(meta);
	}

	public BlockSugarcane() {
		this(0);
	}

	@Override
	public boolean canBeActivated() {
		return true;
	}

	@Override
	public boolean onActivate(Item item, Player player){
		if (item.getId() == Item.DYE && item.getDamage() == 15) {
			int height = 0;
			int below1 = this.getLevel().getBlockIdAt((int) this.x, (int) this.y -1, (int) this.z);
			int below2 = this.getLevel().getBlockIdAt((int) this.x, (int) this.y -2, (int) this.z);
			if(below1 == SUGARCANE_BLOCK){
				height++;
				if(below2 == SUGARCANE_BLOCK){
					return true;
				}
			}
			if (this.getSide(Vector3.SIDE_DOWN).getId() != SUGARCANE_BLOCK) {
				for (y = 1; y <= (2 - height); ++y) {
					Block b = this.getLevel().getBlock(new Vector3(this.x, this.y + y, this.z));
					if (b.getId() == AIR) {
						BlockGrowEvent ev = new BlockGrowEvent(b, new BlockSugarcane());
						Server.getInstance().getPluginManager().callEvent(ev);
						if (! ev.isCancelled()){
							this.getLevel().setBlock(b, ev.getNewState(), true, true);
						}
					}
				}
				this.meta = 0;
				this.getLevel().setBlock(this, this, true);
			}
			if ((player.gamemode & 0x01) == 0) {
				item.count--;
			}
			return true;
		}
		return false;
	}

	@Override
	public int onUpdate(int type) {
		if (type == Level.BLOCK_UPDATE_NORMAL) {
			Block down = this.getSide(Vector3.SIDE_DOWN);
			if (down.isTransparent() && down.getId() != SUGARCANE_BLOCK){
				this.getLevel().useBreakOn(this);
				return Level.BLOCK_UPDATE_NORMAL;
			}
		} else if (type == Level.BLOCK_UPDATE_RANDOM) {
			if (this.getSide(Vector3.SIDE_DOWN).getId() != SUGARCANE_BLOCK) {
				if (this.ohmygod_Metadoesntwork >= 8) {
					for (int up = 1; up <= 2; ++up) {
						Block above = this.getLevel().getBlock(new Vector3(this.x, this.y + up, this.z));
						if(above.getId() == SUGARCANE_BLOCK){
							continue;
						}
						if (above.getId() == AIR) {
							BlockGrowEvent event = new BlockGrowEvent(this.getLevel().getBlock(above), new BlockSugarcane());
							Server.getInstance().getPluginManager().callEvent(event);
							if (!event.isCancelled()) {
								this.getLevel().setBlock(above, event.getNewState(), true);
							}
							break;
						}
					}
					this.ohmygod_Metadoesntwork = 0;
					this.getLevel().setBlock(this, this);
				} else {
					++this.ohmygod_Metadoesntwork;
					this.getLevel().setBlock(this, this);
				}
			}
		}
		return 0;
	}
}