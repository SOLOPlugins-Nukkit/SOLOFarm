package solo.solofarm.item;

import solo.solofarm.block.BlockSugarcane;

public class ItemSugarcane extends cn.nukkit.item.ItemSugarcane{

    public ItemSugarcane() {
        this(0, 1);
    }

    public ItemSugarcane(Integer meta) {
        this(meta, 1);
    }

    public ItemSugarcane(Integer meta, int count) {
        super(meta, count);
        this.block = new BlockSugarcane();
    }
}