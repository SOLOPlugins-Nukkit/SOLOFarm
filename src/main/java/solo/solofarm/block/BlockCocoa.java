package solo.solofarm.block;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.block.BlockGrowEvent;
import cn.nukkit.level.Level;
import cn.nukkit.item.Item;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockWood;

import java.util.Random;

public class BlockCocoa extends cn.nukkit.block.BlockCocoa{

	public BlockCocoa() {
		this(0);
	}

	public BlockCocoa(int meta) {
		super(meta);
	}

	@Override
	public boolean place(Item item, Block block, Block target, int face, double fx, double fy, double fz){
		return this.place(item, block, target, face, fx, fy, fz, null);
	}

	@Override
	public boolean place(Item item, Block block, Block target, int face, double fx, double fy, double fz, Player player) {
		if (target.getId() == Block.WOOD && target.getDamage() == BlockWood.JUNGLE) {
			if(face == 0 || face == 1){
				return false;
			}else if(face == 2){
				this.meta = 0;
			}else if(face == 3){
				this.meta = 2;
			}else if(face == 4){
				this.meta = 3;
			}else if(face == 5){
				this.meta = 1;
			}
			this.level.setBlock(block, this, true, true);
			return true;
		}
		return false;
	}

	@Override
	public int onUpdate(int type) {
		if (type == Level.BLOCK_UPDATE_NORMAL) {
			int[] faces = new int[]{
				3, 4, 2, 5, 3, 4, 2, 5, 3, 4, 2, 5
			};

			Block side = this.getSide(faces[this.meta]);

			if (side.getId() != WOOD && side.getDamage() != BlockWood.JUNGLE) {
				this.getLevel().useBreakOn(this);
				return Level.BLOCK_UPDATE_NORMAL;
			}
		} else if (type == Level.BLOCK_UPDATE_RANDOM) {
			if (new Random().nextInt(2) == 1) {
				if (this.meta <= 7) {
					BlockCocoa block = (BlockCocoa) this.clone();
					block.meta += 4;
					BlockGrowEvent ev = new BlockGrowEvent(this, block);
					Server.getInstance().getPluginManager().callEvent(ev);

					if (!ev.isCancelled()) {
						this.getLevel().setBlock(this, ev.getNewState(), true, true);
					} else {
						return Level.BLOCK_UPDATE_RANDOM;
					}
				}
			} else {
				return Level.BLOCK_UPDATE_RANDOM;
			}
		}

		return 0;
	}
	
	@Override
	public int[][] getDrops(Item item){
		if(this.meta >= 8){
			return new int[][]{
				{Item.DYE, 3, 3}
			};
		}else{
			return new int[][]{
				{Item.DYE, 3, 1}
			};
		}
	}
}