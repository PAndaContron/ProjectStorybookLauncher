package org.projectStorybook.launcher;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.projectStorybook.launcher.json.ReleaseJSON;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.zafarkhaja.semver.Version;

public class Main {
	public static final Platform OS = Platform.detect();
	public static final String PATH_PREFIX = System.getProperty("user.home") + "/.project-storybook/versions/v";
	public static final int BUFFER_SIZE = 1024;

	public static void main(String... args) throws IOException, InterruptedException {
		Map<Version, ReleaseAsset> releases = getReleases();
		Version[] versions = releases.keySet().toArray(new Version[0]);
		Arrays.sort(versions, Version::compareTo);
		
		Version selected = (Version) JOptionPane.showInputDialog(
			null,
			"Select version",
			"Project Storybook Launcher",
			JOptionPane.PLAIN_MESSAGE,
			null,
			versions,
			versions[versions.length - 1]
		);
		
		if (selected != null) {
			File versionDir = new File(PATH_PREFIX + selected);
			
			if (!versionDir.exists()) {
				download(releases.get(selected), versionDir.getAbsolutePath());
			}
			
			if (OS.isNix()) {
				new File(versionDir + "/ProjectStorybook").setExecutable(true);
				new ProcessBuilder(versionDir + "/ProjectStorybook").start();
			} else if (OS.isMac()) {
				new ProcessBuilder("open", versionDir + "/ProjectStorybook.app").start();
			} else if (OS.isWin()) {
				new ProcessBuilder(versionDir + "/ProjectStorybook.exe").start();
			}
		}
	}
	
	private static void download(ReleaseAsset source, String dir) {
		try (InputStream is = source.url.openStream();
			ArchiveInputStream dl = source.url.getFile().endsWith("zip")
					? new ZipArchiveInputStream(is)
					: new TarArchiveInputStream(new GzipCompressorInputStream(is))) {
			ArchiveEntry entry;
			ProgressMonitor pm = new ProgressMonitor(
				null,
				"Downloading...",
				"",
				0,
				source.size
			);
			
			pm.setMillisToDecideToPopup(1);
			pm.setMillisToPopup(1);

			while ((entry = dl.getNextEntry()) != null) {
				String entryName = entry.getName();
				
				if (entryName.startsWith("Linux32/")
					|| entryName.startsWith("Linux64/")
					|| entryName.startsWith("Windows32/")
					|| entryName.startsWith("Windows64/")) {
					entryName = entryName.substring(entryName.indexOf('/') + 1);
				}
				
				if (entry.isDirectory()) {
					new File(dir + "/" + entryName).mkdirs();
				} else {
					int count;
					byte data[] = new byte[BUFFER_SIZE];
					pm.setNote(entryName);
					new File(dir + "/" + entryName).getParentFile().mkdirs();
					FileOutputStream fos = new FileOutputStream(dir + "/" + entryName, false);
					try (BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER_SIZE)) {
						while ((count = dl.read(data, 0, BUFFER_SIZE)) != -1) {
							dest.write(data, 0, count);
							pm.setProgress((int) dl.getBytesRead());
						}
					}
				}
			}
			
			pm.close();
		} catch (IOException e) {
			System.err.println("Download failed");
			throw new UncheckedIOException(e);
		}
	}
	
	private static Map<Version, ReleaseAsset> getReleases() {
		Map<Version, ReleaseAsset> out = new HashMap<>();
		
		for (ReleaseJSON r : getReleasesJSON()) {
			Version ver = Version.valueOf(r.tag_name.substring(1));
			Map<Platform, ReleaseAsset> assets = r.getAssets();
			
			if (assets.containsKey(OS)) {
				out.put(ver, assets.get(OS));
			}
		}
		
		return out;
	}
	
	private static List<ReleaseJSON> getReleasesJSON() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		try {
			URL url = new URL("https://api.github.com/repos/jcampbell11245/Project-Storybook/releases");
			try (InputStream is = url.openStream()) {
				return objectMapper.readValue(is, new TypeReference<List<ReleaseJSON>>(){});
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}

}
