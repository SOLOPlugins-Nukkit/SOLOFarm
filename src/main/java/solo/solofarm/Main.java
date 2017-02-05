package solo.solofarm;

import cn.nukkit.event.Listener;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.block.BlockGrowEvent;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockUnknown;
import cn.nukkit.block.BlockLiquid;
import cn.nukkit.block.BlockIce;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.Config;

import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.io.File;
import java.util.Random;

import solo.solofarm.block.*;
import solo.solofarm.item.*;

public class Main extends PluginBase implements Listener{

	//need monitoring crops? just change the "debugMode" value to true!
	public boolean debugMode = false;

	public Config farmConfig;
	public LinkedHashMap<String, Long> farmData;

	public Config sleepConfig;
	public LinkedHashMap<String, Long> sleepData;

	public int updateInterval = 60000;
	public int sleepTime = 300000;

	//register blocks
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void onLoad(){
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

	@SuppressWarnings({ "deprecation", "serial" })
	@Override
	public void onEnable() {
		this.getDataFolder().mkdirs();
		Config settingConf = new Config(
			new File(this.getDataFolder(), "setting.yml"),
			Config.YAML,
			new LinkedHashMap<String, Object>(){{
				put("updateInterval", 8);
				put("sleepTime", 30);
			}}
		);
		this.updateInterval = settingConf.getInt("updateInterval", 8) * 60000;
		this.sleepTime = settingConf.getInt("sleepTime", 30) * 60000;
		
		LinkedHashMap<String, Object> data;

		this.farmConfig = new Config(new File(this.getDataFolder(), "crops.yml"), Config.YAML);
		data = (LinkedHashMap<String, Object>) (this.farmConfig.getAll());
		data.forEach((k, v) -> this.farmData.put(k, (long) v));

		this.sleepConfig = new Config(new File(this.getDataFolder(), "sleepCrops.yml"), Config.YAML);
		data = (LinkedHashMap<String, Object>) (this.sleepConfig.getAll());
		data.forEach((k, v) -> this.sleepData.put(k, (long) v));

		this.getServer().getPluginManager().registerEvents(this, this);

		this.getServer().getScheduler().scheduleRepeatingTask(new FarmTask(this), 20);
		this.getServer().getScheduler().scheduleRepeatingTask(new SleepTask(this), 200);

	}

	@Override
	public void onDisable(){
		this.save();
	}

	public void save() {
		LinkedHashMap<String, Object> data = new LinkedHashMap<>();
		
		this.farmData.forEach((k, v) -> data.put(k, v));
		this.farmConfig.setAll(data);
		this.farmConfig.save();
		
		data.clear();
		this.sleepData.forEach((k, v) -> data.put(k, v));
		this.sleepConfig.setAll(data);
		this.sleepConfig.save();
	}

	public void log(String msg){
		if(this.debugMode){
			this.getServer().getLogger().info(msg);
		}
	}

	public boolean isCrop(Block block){
		int id = block.getId();
		if(id == Block.AIR){
			 id = block.getLevel().getBlockIdAt((int) block.x, (int) block.y, (int) block.z);
		}

		switch(id){
			case Block.BEETROOT_BLOCK:
			case Block.CARROT_BLOCK:
			case Block.POTATO_BLOCK:
			case Block.WHEAT_BLOCK:
			case Block.COCOA_BLOCK:
			case Block.MELON_STEM:
			case Block.PUMPKIN_STEM:
			case 115:
				return true;

			case Block.SUGARCANE_BLOCK:
			case Block.CACTUS:
				return (block.getLevel().getBlockIdAt((int) block.x, (int) block.y - 1, (int) block.z) != id);

			default:
				return false;
		}
	}

	//if you register crops, they are continously growing unless their chunk is unloaded
	public void registerCrop(Position pos){
		this.farmData.put(
			pos.level.getFolderName() + ":" + Integer.toString((int) pos.x) + ":" + Integer.toString((int) pos.y) + ":" + Integer.toString((int) pos.z),
			System.currentTimeMillis() + this.updateInterval
		);
		this.log("crop is registered");
	}

	//for prevent leek server performance
	//if crops should grow continously, use this method.
	//cactus, sugarcane, pumpkin stem, melon stem....etc
	public void sleepCrop(Position pos){
		this.sleepData.put(
			pos.level.getFolderName() + ":" + Integer.toString((int) pos.x) + ":" + Integer.toString((int) pos.y) + ":" + Integer.toString((int) pos.z),
			System.currentTimeMillis() + this.sleepTime
		);
		this.log("crop is now sleep");
	}

	//just return true when crops are full grown.
	//actually using at sleep crops or unregiser.
	public boolean isFullGrown(Position pos){
		Block block = pos.getLevel().getBlock(pos);
		switch(block.getId()){
			case Block.BEETROOT_BLOCK:
			case Block.CARROT_BLOCK:
			case Block.POTATO_BLOCK:
			case Block.WHEAT_BLOCK:
				return (block.getDamage() >= 7);

			case Block.MELON_STEM:
			case Block.PUMPKIN_STEM:
				if(block.getDamage() >= 7){
					int outputId = (block.getId() == Block.MELON_STEM) ? Block.MELON_BLOCK : Block.PUMPKIN;
					boolean isBlocked = true;
					for(int side = 2; side <= 5; ++side){
						if(block.getSide(side).getId() == outputId){
							return true;
						}else if(block.getSide(side).getId() == Block.AIR){
							isBlocked = false;
						}
					}
					if(isBlocked){
						return true;
					}
				}
				return false;

			case 115:
				return (block.getDamage() >= 3);

			case Block.COCOA_BLOCK:
				return (block.getDamage() >= 8);

			case Block.CACTUS:
			case Block.SUGARCANE_BLOCK:
				int x = (int) block.x;
				int y = (int) block.y;
				int z = (int) block.z;
				for(int up = 1; up <= 2; ++up){
					int atUp = block.getLevel().getBlockIdAt(x, y + up, z);
					if(atUp == Block.AIR){
						return false;
					}else if(atUp != block.getId()){
						return true;
					}
				}
				return true;
		}
		return true;
	}

	//if no more need to grow, return true.
	//if block is not crop, return true.
	public boolean updateCrop(Block block){
		switch(block.getId()){
			case Block.BEETROOT_BLOCK:
			case Block.CARROT_BLOCK:
			case Block.POTATO_BLOCK:
			case Block.WHEAT_BLOCK:
			case Block.COCOA_BLOCK:
			case 115:
				block.onUpdate(Level.BLOCK_UPDATE_RANDOM);
				return this.isFullGrown(block);

			case Block.MELON_STEM:
			case Block.PUMPKIN_STEM:
			case Block.CACTUS:
				block.onUpdate(Level.BLOCK_UPDATE_RANDOM);
				if(this.isFullGrown(block)){
					this.sleepCrop(block);
					return true;
				}
				return false;

			case Block.SUGARCANE_BLOCK:
				//wtf sugarcane bug... this is a part of sugarcane's onUpdate method.
				if(block.getSide(Vector3.SIDE_DOWN).getId() == Block.SUGARCANE_BLOCK){
					return true;
				}
				if(new Random().nextInt(6) != 1){
					return false;
				}
				for(int y = 1; y <= 2; ++y){
					Block above = block.getLevel().getBlock(new Vector3(block.x, block.y + y, block.z));
					if(above.getId() == Block.SUGARCANE_BLOCK){
						continue;
					}
					if(above.getId() == Block.AIR){
						Block newState = Block.get(block.getId());
						BlockGrowEvent ev = new BlockGrowEvent(above, newState);
						this.getServer().getPluginManager().callEvent(ev);
						if(!ev.isCancelled()){
							block.getLevel().setBlock(above, ev.getNewState(), true);
						}
						break;
					}
				}
				if(this.isFullGrown(block)){
					this.sleepCrop(block);
					return true;
				}
				return false;

		}
		return false;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void onPlace(BlockPlaceEvent event){
		if(this.isCrop(event.getBlock())){
			this.registerCrop(event.getBlock());
		}
	}
}
