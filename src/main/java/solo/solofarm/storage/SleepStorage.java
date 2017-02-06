package solo.solofarm.storage;

import java.io.File;
import java.util.HashSet;
import java.util.Map;

import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.utils.Config;
import solo.solofarm.CropControl;
import solo.solofarm.Main;
import solo.solofarm.Setting;

public class SleepStorage extends Storage{

	@Override
	public void load(){
		this.config = new Config(new File(Main.getInstance().getDataFolder(), "sleepCrops.yml"), Config.YAML);
		this.config.getAll().forEach((k, v) -> this.put(k, (long) v));
	}
	
	public void check(){
		HashSet<String> toRemove = new HashSet<String>();
		HashSet<String> toAdd = new HashSet<String>();

		Level level = null;
		String[] dat;
		Position pos;
		
		int check = 0;

		for(Map.Entry<String, Long> entry : this.entrySet()){
			if(entry.getValue() < System.currentTimeMillis()){
				dat = entry.getKey().split(":");
				if(level == null || ! level.getFolderName().equals(dat[0])){
					level = Server.getInstance().getLevelByName(dat[0]);
				}

				toRemove.add(entry.getKey());
				
				if(level == null){
					toAdd.add(entry.getKey()); // level is not loaded...
					continue;
				}
				
				pos = new Position(Integer.parseInt(dat[1]), Integer.parseInt(dat[2]), Integer.parseInt(dat[3]), level);
				
				if(CropControl.canContinuouslyGrow(pos)){
					if(CropControl.isFullGrown(pos)){ //if full grown, continously sleep
						toAdd.add(entry.getKey());
					}else{
						CropControl.farmStorage.put(entry.getKey(), System.currentTimeMillis() + Setting.CHECK_CROP_INTERVAL * 1000); // break sleep
					}
				}
			}else if(++check > 20){
				break;
			}
		}
		for(String key : toRemove){
			this.remove(key);
		}
		for(String key : toAdd){
			this.put(key, System.currentTimeMillis() + Setting.CHECK_CROP_INTERVAL * 1000);
		}
		//this.owner.log("breakFromSleep : " + Integer.toString(breakFromSleep) + " / continueSleep : " + Integer.toString(continueSleep) + " / error : " + Integer.toString(error));
	}
	
}