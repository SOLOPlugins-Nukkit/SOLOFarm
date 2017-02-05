package solo.solofarm.item;

import solo.solofarm.block.BlockStemPumpkin;

public class ItemSeedsPumpkin extends cn.nukkit.item.ItemSeedsPumpkin{

    public ItemSeedsPumpkin() {
        this(0, 1);
    }

    public ItemSeedsPumpkin(Integer meta) {
        this(meta, 1);
    }

    public ItemSeedsPumpkin(Integer meta, int count) {
        super(meta, count);
        this.block = new BlockStemPumpkin();
    }
}