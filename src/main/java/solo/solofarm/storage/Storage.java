package solo.solofarm.storage;

import java.util.LinkedHashMap;

import cn.nukkit.utils.Config;

public abstract class Storage extends LinkedHashMap<String, Long>{
	
	public Config config;
	
	public abstract void load();
	
	public void save(){
		LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>();
		this.forEach((k, v) -> data.put(k, v));
		this.config.setAll(data);
		this.config.save();
	}
	
}