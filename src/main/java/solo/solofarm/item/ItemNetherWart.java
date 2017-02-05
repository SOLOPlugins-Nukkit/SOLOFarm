package solo.solofarm.item;

import cn.nukkit.item.Item;
import solo.solofarm.block.BlockNetherWart;

public class ItemNetherWart extends Item{

	public ItemNetherWart(){
		this(0, 1);
	}

	public ItemNetherWart(Integer meta){
		this(meta, 1);
	}

	public ItemNetherWart(Integer meta, int count){
		super(372, meta, count, "Nether Wart");
		this.block = new BlockNetherWart();
	}

	protected ItemNetherWart(int id, Integer meta, int count, String name){
		super(id, meta, count, name);
	}
}