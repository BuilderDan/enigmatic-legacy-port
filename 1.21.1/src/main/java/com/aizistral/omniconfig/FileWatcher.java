package com.aizistral.omniconfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;

/**
 * Port-only. Replaces night-config's FileWatcher, which Forge ships but Fabric does not.
 * Runs the given callback when a watched file changes on disk; same API subset as the
 * original (defaultInstance, addWatch, removeWatch).
 */
public class FileWatcher {
	private static FileWatcher defaultInstance;

	private final Map<Path, Runnable> watches = new ConcurrentHashMap<>();
	private final Set<Path> watchedDirs = new HashSet<>();
	private final WatchService service;

	private FileWatcher() throws IOException {
		this.service = FileSystems.getDefault().newWatchService();

		Thread thread = new Thread(this::run, "Omniconfig FileWatcher");
		thread.setDaemon(true);
		thread.start();
	}

	public static synchronized FileWatcher defaultInstance() {
		if (defaultInstance == null) {
			try {
				defaultInstance = new FileWatcher();
			} catch (IOException ex) {
				throw new RuntimeException("Couldn't create config file watcher", ex);
			}
		}

		return defaultInstance;
	}

	public synchronized void addWatch(File file, Runnable onChange) throws IOException {
		Path path = file.toPath().toAbsolutePath().normalize();
		Path dir = path.getParent();

		if (this.watchedDirs.add(dir)) {
			dir.register(this.service, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);
		}

		this.watches.put(path, onChange);
	}

	public void removeWatch(File file) {
		this.watches.remove(file.toPath().toAbsolutePath().normalize());
	}

	private void run() {
		while (true) {
			WatchKey key;

			try {
				key = this.service.take();
			} catch (InterruptedException | ClosedWatchServiceException ex) {
				return;
			}

			Path dir = (Path) key.watchable();

			for (WatchEvent<?> event : key.pollEvents()) {
				if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
					continue;
				}

				Runnable callback = this.watches.get(dir.resolve((Path) event.context()).normalize());

				if (callback != null) {
					try {
						callback.run();
					} catch (Exception ex) {
						EnigmaticLegacy.LOGGER.catching(ex);
					}
				}
			}

			key.reset();
		}
	}
}
