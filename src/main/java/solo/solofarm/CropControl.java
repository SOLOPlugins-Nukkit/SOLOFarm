package solo.solofarm;

import java.util.Random;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.event.block.BlockGrowEvent;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.Task;
import solo.solofarm.storage.CropStorage;
import solo.solofarm.storage.FarmStorage;
import solo.solofarm.storage.SleepStorage;

public class CropControl{
	
	private CropControl(){
		
	}
	
	public static CropStorage cropStorage;
	public static FarmStorage farmStorage;
	public static SleepStorage sleepStorage;
	
	public static void init(){
		cropStorage = new CropStorage();
		farmStorage = new FarmStorage();
		sleepStorage = new SleepStorage();
		
		cropStorage.load();
		farmStorage.load();
		sleepStorage.load();
		
		Server.getInstance().getScheduler().scheduleRepeatingTask(new Task(){
			@Override
			public void onRun(int currentTick){
				farmStorage.check();
			}
		}, 19);
		
		Server.getInstance().getScheduler().scheduleRepeatingTask(new Task(){
			@Override
			public void onRun(int currentTick){
				sleepStorage.check();
			}
		}, 300);
	}
	
	public static void save(){
		cropStorage.save();
		farmStorage.save();
		sleepStorage.save();
	}
	
	public static String getHash(Position pos){
		return pos.getLevel().getFolderName() + ":" + Integer.toString(pos.getFloorX()) + ":" + Integer.toString(pos.getFloorY()) + ":" + Integer.toString(pos.getFloorZ());
	}

	public static boolean isCrop(Position pos){
		int id = pos.getLevel().getBlockIdAt(pos.getFloorX(), pos.getFloorY(), pos.getFloorZ());

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
				return (pos.getLevel().getBlockIdAt(pos.getFloorX(), pos.getFloorY() - 1, pos.getFloorZ()) != id);

			default:
				return false;
		}
	}
	
	//if you register crops, they are continously growing unless their chunk is unloaded
	public static void registerCrop(Position pos){
		farmStorage.put(getHash(pos), System.currentTimeMillis() + Setting.CHECK_CROP_INTERVAL * 1000);
	}
	
	//just return true when crops are full grown.
	//actually using at sleep crops or unregiser.
	public static boolean isFullGrown(Position pos){
		int blockId = pos.getLevel().getBlockIdAt(pos.getFloorX(), pos.getFloorY(), pos.getFloorZ());
		int blockData = pos.getLevel().getBlockDataAt(pos.getFloorX(), pos.getFloorY(), pos.getFloorZ());
		switch(blockId){
			case Block.BEETROOT_BLOCK:
			case Block.CARROT_BLOCK:
			case Block.POTATO_BLOCK:
			case Block.WHEAT_BLOCK:
				return blockData >= 7;

			case Block.MELON_STEM:
			case Block.PUMPKIN_STEM:
				if(blockData >= 7){
					int outputId = (blockId == Block.MELON_STEM) ? Block.MELON_BLOCK : Block.PUMPKIN;
					boolean isBlocked = true;
					int[] check = new int[]{
							pos.getLevel().getBlockIdAt(pos.getFloorX() + 1, pos.getFloorY(), pos.getFloorZ()),
							pos.getLevel().getBlockIdAt(pos.getFloorX(), pos.getFloorY(), pos.getFloorZ() + 1),
							pos.getLevel().getBlockIdAt(pos.getFloorX() - 1, pos.getFloorY(), pos.getFloorZ()),
							pos.getLevel().getBlockIdAt(pos.getFloorX(), pos.getFloorY(), pos.getFloorZ() - 1),
					};
					for(int sideId : check){
						if(sideId == outputId){
							return true;
						}else if(sideId == Block.AIR){
							isBlocked = false;
						}
					}
					if(isBlocked){
						return true;
					}
				}
				return false;

			case 115:
				return blockData >= 3;

			case Block.COCOA_BLOCK:
				return blockData >= 8;

			case Block.CACTUS:
			case Block.SUGARCANE_BLOCK:
				int x = pos.getFloorX();
				int y = pos.getFloorY();
				int z = pos.getFloorZ();
				for(int up = 1; up <= 2; ++up){
					int atUp = pos.getLevel().getBlockIdAt(x, y + up, z);
					if(atUp == Block.AIR){
						return false;
					}else if(atUp != blockId){
						return true;
					}
				}
				return true;
		}
		return true;
	}
	
	public static boolean canContinuouslyGrow(Position pos){
		int id = pos.getLevel().getBlockIdAt(pos.getFloorX(), pos.getFloorY(), pos.getFloorZ());
		return (
				id == Block.MELON_STEM
				|| id == Block.PUMPKIN_STEM
				|| id == Block.CACTUS
				|| id == Block.SUGARCANE_BLOCK
			);
	}
	
	//if no more need to grow, return true.
	//if block is not crop, return true.
	public static boolean updateCrop(Position pos){
		Block block = pos.getLevel().getBlock(pos);
		switch(block.getId()){
			case Block.BEETROOT_BLOCK:
			case Block.CARROT_BLOCK:
			case Block.POTATO_BLOCK:
			case Block.WHEAT_BLOCK:
			case Block.COCOA_BLOCK:
			case Block.MELON_STEM:
			case Block.PUMPKIN_STEM:
			case Block.CACTUS:
			case 115:
				block.onUpdate(Level.BLOCK_UPDATE_RANDOM);
				return isFullGrown(block);

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
						Server.getInstance().getPluginManager().callEvent(ev);
						if(!ev.isCancelled()){
							block.getLevel().setBlock(above, ev.getNewState(), true);
						}
						break;
					}
				}
				return isFullGrown(block);

		}
		return false;
	}
	
	//for prevent leek server performance
	//if crops should grow continously, use this method.
	//cactus, sugarcane, pumpkin stem, melon stem....etc
	public static void sleepCrop(Position pos){
		sleepStorage.put(
			pos.level.getFolderName() + ":" + Integer.toString((int) pos.x) + ":" + Integer.toString((int) pos.y) + ":" + Integer.toString((int) pos.z),
			System.currentTimeMillis() + Setting.CHECK_SLEEP_CROP_INTERVAL * 1000
		);
		//this.log("crop is now sleep");
	}
	
}