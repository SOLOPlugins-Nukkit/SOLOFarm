package solo.solofarm.item;

import cn.nukkit.utils.DyeColor;
import solo.solofarm.block.BlockCocoa;

public class ItemDye extends cn.nukkit.item.ItemDye{

	public ItemDye() {
		this(0, 1);
	}

	public ItemDye(Integer meta) {
		this(meta, 1);
	}

	public ItemDye(DyeColor dyeColor) {
		this(dyeColor.getDyeData(), 1);
	}

	public ItemDye(DyeColor dyeColor, int amount) {
		this(dyeColor.getDyeData(), amount);
	}

	public ItemDye(Integer meta, int amount) {
		super(meta, amount);

		if (this.meta == DyeColor.BROWN.getDyeData()) {
			this.block = new BlockCocoa();
		}
	}
}