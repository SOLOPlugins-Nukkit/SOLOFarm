package solo.solofarm.block;

import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;

public class BlockStemMelon extends cn.nukkit.block.BlockStemMelon{

	public BlockStemMelon(){
		this(0);
	}

	public BlockStemMelon(int meta){
		super(meta);
	}

	@Override
	public int onUpdate(int type){
		if(type == Level.BLOCK_UPDATE_NORMAL){
			if(this.getSide(Vector3.SIDE_DOWN).getId() != FARMLAND){
				this.getLevel().useBreakOn(this);
				return Level.BLOCK_UPDATE_NORMAL;
			}
		}else{
			return super.onUpdate(type);
		}
		return 0;
	}
}