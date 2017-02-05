package solo.solofarm;

import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.block.Block;
import cn.nukkit.scheduler.PluginTask;
import cn.nukkit.math.Vector3;

import java.util.HashSet;

public class SleepTask extends PluginTask<Main> {

	public SleepTask(Main owner) {
		super(owner);
	}

	@Override
	public void onRun(int currentTick) {
		HashSet<String> toRemove = new HashSet<String>();
		HashSet<String> toAdd = new HashSet<String>();

		Level level = null;
		String[] dat;
		Block block;

		int breakFromSleep = 0;
		int continueSleep = 0;
		int error = 0;

		int overloadedLock = 80;

		for(String key : this.owner.sleepData.keySet()){
			if(this.owner.sleepData.get(key) < System.currentTimeMillis()){
				try{
					if(--overloadedLock < 0){
						this.owner.log("loop is too long and growing task is force stopped.");
						break;
					}

					dat = key.split(":");
					if(level == null || ! level.getFolderName().equals(dat[0])){
						level = Server.getInstance().getLevelByName(dat[0]);
					}
					block = level.getBlock(new Vector3(
						Integer.parseInt(dat[1]),
						Integer.parseInt(dat[2]),
						Integer.parseInt(dat[3])
					));
					if(this.owner.isCrop(block)){
						if(this.owner.isFullGrown(block)){ //if full grown, continously sleep
							toAdd.add(key);
							++continueSleep;
							continue;
						}else{
							this.owner.farmData.put(key, System.currentTimeMillis() + this.owner.updateInterval);
							++breakFromSleep;
						}
					}
					toRemove.add(key);
				}catch (Exception e){
					++error;
					toRemove.add(key);
				}
			}else{
				break;
			}
		}
		for(String key : toRemove){
			this.owner.sleepData.remove(key);
		}
		for(String key : toAdd){
			this.owner.sleepData.remove(key);
			this.owner.sleepData.put(key, System.currentTimeMillis() + this.owner.sleepTime);
		}
		this.owner.log("breakFromSleep : " + Integer.toString(breakFromSleep) + " / continueSleep : " + Integer.toString(continueSleep) + " / error : " + Integer.toString(error));
	}
}
