package solo.solofarm;

import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.block.Block;
import cn.nukkit.scheduler.PluginTask;
import cn.nukkit.math.Vector3;

import java.util.HashSet;

public class FarmTask extends PluginTask<Main> {

	public FarmTask(Main owner) {
		super(owner);
	}

	@Override
	public void onRun(int currentTick) {
		HashSet<String> toRemove = new HashSet<String>();
		HashSet<String> toAdd = new HashSet<String>();

		Level level = null;
		String[] dat;
		Block block;

		int updated = 0;
		int fullGrown = 0;
		int error = 0;

		int overloadedLock = 120;
		
		for(String key : this.owner.farmData.keySet()){
			if(this.owner.farmData.get(key) < System.currentTimeMillis()){
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
					++updated;
					if(this.owner.isCrop(block)){
						if(! this.owner.updateCrop(block)){
							toAdd.add(key); //grow continously
							continue;
						}else{
							++fullGrown;
						}
					}else{
						this.owner.log("Are crops breaked?");
						this.owner.log("Server found the another block " + Integer.toString(block.getId()));
					}
					toRemove.add(key);
				}catch (Exception e){
					toRemove.add(key);
					++error;
				}
			}else{
				break;
			}
		}
		for(String key : toRemove){
			this.owner.farmData.remove(key);
		}
		for(String key : toAdd){
			this.owner.farmData.remove(key);
			this.owner.farmData.put(key, System.currentTimeMillis() + this.owner.updateInterval);
		}
		this.owner.log("updated : " + Integer.toString(updated) + " / fullGrown : " + Integer.toString(fullGrown) + " / error : " + Integer.toString(error));
	}
}
