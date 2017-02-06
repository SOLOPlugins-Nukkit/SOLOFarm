package solo.solofarm;

import java.io.File;
import java.util.LinkedHashMap;

import cn.nukkit.utils.Config;

public class Setting{
	
	private Setting(){
		
	}
	
	public static int CHECK_CROP_INTERVAL;
	public static int CHECK_SLEEP_CROP_INTERVAL;
	
	@SuppressWarnings({ "serial", "deprecation" })
	public static void init(){
		Config config = new Config(new File(Main.getInstance().getDataFolder(), "setting.yml"), Config.YAML, new LinkedHashMap<String, Object>(){{
			put("checkCropInterval", 480);
			put("checkSleepCropInterval", 2400);
		}});
		
		CHECK_CROP_INTERVAL = config.getInt("checkCropInterval");
		CHECK_SLEEP_CROP_INTERVAL = config.getInt("checkSleepCropInterval");
	}
	
}