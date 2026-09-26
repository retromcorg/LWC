package com.griefcraft.util;


import com.griefcraft.lwc.LWCInfo;
import com.griefcraft.scripting.ModuleLoader;
import com.griefcraft.sql.Database;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class Updater {

    /**
     * The logging object for this class
     */
    private Logger logger = Logger.getLogger(getClass().getSimpleName());

    /**
     * List of files to download
     */
    private final List<UpdaterFile> needsUpdating = Collections.synchronizedList(new ArrayList<UpdaterFile>());

    /**
     * The folder where libraries are stored
     */
    public final static String DEST_LIBRARY_FOLDER = "plugins/LWC/";

    /**
     * Download a file
     *
     * @param updaterFile
     */
    public void download(UpdaterFile updaterFile) {
        needsUpdating.add(updaterFile);

        try {
            update();
        } catch (Exception e) {
        }
    }

    /**
     * Check for dependencies
     */
    public void check() {
        if (Database.DefaultType == Database.Type.SQLite) {
            String[] shared = new String[]{"lib/sqlite.jar", getFullNativeLibraryPath()};

            for (String path : shared) {
                File file = new File(path);

                if (!file.exists() && !file.isDirectory()) {
                    UpdaterFile updaterFile = new UpdaterFile(path);
                    updaterFile.setLocalLocation(DEST_LIBRARY_FOLDER + path);

                    if (!needsUpdating.contains(updaterFile)) {
                        needsUpdating.add(updaterFile);
                    }
                }
            }
        }
    }

    public void downloadConfig(String config) {
        File file = new File(ModuleLoader.ROOT_PATH + config); // where to save to
        UpdaterFile updaterFile = new UpdaterFile(config);

        updaterFile.setLocalLocation(file.getPath());
        download(updaterFile);
    }

    /**
     * @return the full path to the native library for sqlite
     */
    public String getFullNativeLibraryPath() {
        return getOSSpecificFolder() + getOSSpecificFileName();
    }

    /**
     * @return the os/arch specific file name for sqlite's native library
     */
    public String getOSSpecificFileName() {
        String osname = System.getProperty("os.name").toLowerCase();

        if (osname.contains("windows")) {
            return "sqlitejdbc.dll";
        } else if (osname.contains("mac")) {
            return "libsqlitejdbc.jnilib";
        } else { /* We assume linux/unix */
            return "libsqlitejdbc.so";
        }
    }

    /**
     * @return the os/arch specific folder location for SQLite's native library
     */
    public String getOSSpecificFolder() {
        String osname = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();

        if (osname.contains("windows")) {
            return "lib/native/Windows/" + arch + "/";
        } else if (osname.contains("mac")) {
            return "lib/native/Mac/" + arch + "/";
        } else { /* We assume linux/unix */
            return "lib/native/Linux/" + arch + "/";
        }
    }

    /**
     * Load the latest versions
     *
     * @param background if true, will be run in the background
     */
    public void loadVersions(boolean background) {
        class Background_Check_Thread implements Runnable {
            public void run() {
                try {
                    check();
                    update();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        Background_Check_Thread worker = new Background_Check_Thread();

        if (background) {
            new Thread(worker).start();
        } else {
            worker.run();
        }
    }

    /**
     * Ensure we have all of the required files (if not, download them)
     */
    public void update() throws Exception {
        if (needsUpdating.size() == 0) {
            return;
        }

        /*
           * Make the folder hierarchy if needed
           */
        File folder = new File(DEST_LIBRARY_FOLDER + getOSSpecificFolder());
        folder.mkdirs();
        folder = new File(DEST_LIBRARY_FOLDER + "lib/");
        folder.mkdirs();

        synchronized (needsUpdating) {
            Iterator<UpdaterFile> iterator = needsUpdating.iterator();

            while (iterator.hasNext()) {
                UpdaterFile item = iterator.next();
                File file = new File(item.getLocalLocation());

                logger.info("Initializing: " + item.getLocalLocation());

                if (file.exists()) {
                    continue;
                }

                InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(item.getRemoteLocation());
                OutputStream outputStream = new FileOutputStream(file);

                saveTo(inputStream, outputStream);

                inputStream.close();
                outputStream.close();

                iterator.remove();
            }
        }
    }

    /**
     * Write an input stream to an output stream
     *
     * @param inputStream
     * @param outputStream
     */
    public static void saveTo(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;

        while ((len = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, len);
        }
    }

}
