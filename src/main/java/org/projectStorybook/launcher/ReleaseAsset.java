package org.projectStorybook.launcher;

import java.net.URL;

public class ReleaseAsset {
	public URL url;
	public int size;
	
	public ReleaseAsset(URL url, int size) {
		this.url = url;
		this.size = size;
	}
}
