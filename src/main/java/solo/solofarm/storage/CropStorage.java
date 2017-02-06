package solo.solofarm.storage;

import java.io.File;

import cn.nukkit.utils.Config;
import solo.solofarm.Main;

public class CropStorage extends Storage{
	
	@Override
	public void load(){
		this.config = new Config(new File(Main.getInstance().getDataFolder(), "fullGrownCrops.yml"), Config.YAML);
		this.config.getAll().forEach((k, v) -> this.put(k, (long) v));
	}
}