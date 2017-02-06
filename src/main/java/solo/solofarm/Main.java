package solo.solofarm;

import cn.nukkit.event.Listener;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockUnknown;
import cn.nukkit.block.BlockLiquid;
import cn.nukkit.block.BlockIce;
import cn.nukkit.item.Item;
import cn.nukkit.plugin.PluginBase;

import java.lang.reflect.Constructor;

import solo.solofarm.block.*;
import solo.solofarm.item.*;

public class Main extends PluginBase implements Listener{

	public static Main instance;
	
	public static Main getInstance(){
		return instance;
	}

	//register blocks
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void onLoad(){
		instance = this;
		
		Item.list[Item.NETHER_WART] = ItemNetherWart.class;
		Item.list[Item.MELON_SEEDS] = ItemSeedsMelon.class;
		Item.list[Item.PUMPKIN_SEEDS] = ItemSeedsPumpkin.class;
		//Item.list[Item.SUGARCANE] = ItemSugarcane.class;
		Item.list[Item.DYE] = ItemDye.class;

		Block.list[115] = BlockNetherWart.class;
		//Block.list[Block.MELON_STEM] = BlockMelonStem.class;
		//Block.list[Block.PUMPKIN_STEM] = BlockPumpkinStem.class; //They are fixed
		Block.list[Block.CACTUS] = BlockCactus.class;
		//Block.list[Block.SUGARCANE_BLOCK] = BlockSugarcane.class;
		Block.list[Block.COCOA_BLOCK] = BlockCocoa.class;

		for (int id = 0; id < 256; id++) {
			Class c = Block.list[id];
			if (c != null) {
				Block block;
				try {
					block = (Block) c.newInstance();
					Constructor constructor = c.getDeclaredConstructor(int.class);
					constructor.setAccessible(true);
					for (int data = 0; data < 16; ++data) {
						Block.fullList[(id << 4) | data] = (Block) constructor.newInstance(data);
					}
				} catch (Exception e) {
					this.getServer().getLogger().error("Error while registering block" + c.getName(), e);
					for (int data = 0; data < 16; ++data) {
						Block.fullList[(id << 4) | data] = new BlockUnknown(id, data);
					}
					return;
				}
				Block.solid[id] = block.isSolid();
				Block.transparent[id] = block.isTransparent();
				Block.hardness[id] = block.getHardness();
				Block.light[id] = block.getLightLevel();

				if (block.isSolid()) {
					if (block.isTransparent()) {
						if (block instanceof BlockLiquid || block instanceof BlockIce) {
							Block.lightFilter[id] = 2;
						} else {
							Block.lightFilter[id] = 1;
						}
					} else {
						Block.lightFilter[id] = 15;
					}
				} else {
					Block.lightFilter[id] = 1;
				}
			} else {
				Block.lightFilter[id] = 1;
				for (int data = 0; data < 16; ++data) {
					Block.fullList[(id << 4) | data] = new BlockUnknown(id, data);
				}
			}
		}
	}

	@Override
	public void onEnable() {
		this.getDataFolder().mkdirs();

		Setting.init();
		CropControl.init();
	}

	@Override
	public void onDisable(){
		CropControl.save();
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void onPlace(BlockPlaceEvent event){
		if(CropControl.isCrop(event.getBlock())){
			CropControl.registerCrop(event.getBlock());
		}
	}
}
