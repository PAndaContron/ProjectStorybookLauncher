package org.projectStorybook.launcher;

public enum Platform {
	LINUX32("Linux32.tar.gz"),
	LINUX64("Linux64.tar.gz"),
	MACOS("MacOS.zip"),
	WINDOWS32("Windows32.zip"),
	WINDOWS64("Windows64.zip");
	
	public static Platform detect() {
		String osName = System.getProperty("os.name").toLowerCase();
		
		if (osName.contains("mac")) {
			return MACOS;
		}
		
		String arch = System.getProperty("sun.arch.data.model");
		
		if (osName.contains("win")) {
			return arch.equals("64") ? WINDOWS64 : WINDOWS32;
		}
		
		return arch.equals("64") ? LINUX64 : LINUX32;
	}
	
	private String filename;
	
	private Platform(String filename) {
		this.filename = filename;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public boolean isNix() {
		return this == LINUX32 || this == LINUX64;
	}
	
	public boolean isMac() {
		return this == MACOS;
	}
	
	public boolean isWin() {
		return this == WINDOWS32 || this == WINDOWS64;
	}
}
