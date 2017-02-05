package solo.solofarm.block;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.block.BlockGrowEvent;
import cn.nukkit.level.Level;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockFlowable;
import cn.nukkit.item.Item;
import cn.nukkit.math.Vector3;

import java.util.Random;

public class BlockNetherWart extends BlockFlowable{

	public BlockNetherWart(int meta){
		super(meta);
	}

	public BlockNetherWart(){
		this(0);
	}

	@Override
	public String getName(){
		return "Nether Wart";
	}

	@Override
	public int getId(){
		return 115;
	}

	@Override
	public boolean place(Item item, Block block, Block target, int face, double fx, double fy, double fz){
		return this.place(item, block, target, face, fx, fy, fz, null);
	}

	@Override
	public boolean place(Item item, Block block, Block target, int face, double fx, double fy, double fz, Player player){
		Block down = this.getSide(Vector3.SIDE_DOWN);
		if(down.getId() == SOUL_SAND){
			this.getLevel().setBlock(block, this, true, true);
			return true;
		}
		return false;
	}

	@Override
	public int onUpdate(int type){
		if(type == Level.BLOCK_UPDATE_NORMAL){
			if(this.getSide(Vector3.SIDE_DOWN).getId() != SOUL_SAND){
				this.getLevel().useBreakOn(this);
				return Level.BLOCK_UPDATE_NORMAL;
			}
		}else if(type == Level.BLOCK_UPDATE_RANDOM){
			if(new Random().nextInt(12) == 1){
				if(this.meta < 0x03){
					BlockNetherWart newState = (BlockNetherWart) this.clone();
					++newState.meta;
					BlockGrowEvent ev = new BlockGrowEvent(this, newState);
					Server.getInstance().getPluginManager().callEvent(ev);
					if(! ev.isCancelled()){
						this.getLevel().setBlock(this, ev.getNewState(), true, true);
					}else{
						return Level.BLOCK_UPDATE_RANDOM;
					}
				}
			}else{
				return Level.BLOCK_UPDATE_RANDOM;
			}
		}
		return 0;
	}

	@Override
	public int[][] getDrops(Item item){
		if(meta >= 0x03){
			return new int[][]{
				{372, 0, 2}
			};
		}else{
			return new int[][]{
				{372, 0, 1}
			};
		}
	}
}