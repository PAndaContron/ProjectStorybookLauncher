package org.projectStorybook.launcher.json;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.projectStorybook.launcher.Platform;
import org.projectStorybook.launcher.ReleaseAsset;

public class ReleaseJSON {
	public String tag_name;
	public List<ReleaseAssetJSON> assets;
	
	public Map<Platform, ReleaseAsset> getAssets() {
		Map<String, ReleaseAsset> tmp = new HashMap<>();
		
		for (ReleaseAssetJSON a : assets) {
			try {
				tmp.put(a.name, new ReleaseAsset(new URL(a.browser_download_url), a.size));
			} catch (MalformedURLException e) {}
		}
		
		Map<Platform, ReleaseAsset> out = new HashMap<>();
		
		for (Platform p : Platform.values()) {
			if (tmp.containsKey(p.getFilename())) {
				out.put(p, tmp.get(p.getFilename()));
			}
		}
		
		return out;
	}
}
