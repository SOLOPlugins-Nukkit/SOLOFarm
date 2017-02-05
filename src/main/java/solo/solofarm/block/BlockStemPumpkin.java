package solo.solofarm.block;

import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;

public class BlockStemPumpkin extends cn.nukkit.block.BlockStemPumpkin{

	public BlockStemPumpkin(){
		this(0);
	}

	public BlockStemPumpkin(int meta){
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