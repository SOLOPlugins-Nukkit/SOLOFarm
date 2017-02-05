package solo.solofarm.item;

import solo.solofarm.block.BlockStemMelon;

public class ItemSeedsMelon extends cn.nukkit.item.ItemSeedsMelon{

    public ItemSeedsMelon() {
        this(0, 1);
    }

    public ItemSeedsMelon(Integer meta) {
        this(meta, 1);
    }

    public ItemSeedsMelon(Integer meta, int count) {
        super(meta, count);
        this.block = new BlockStemMelon();
    }
}