package com.griefcraft.util.config;

import com.griefcraft.lwc.LWC;
import com.griefcraft.scripting.ModuleLoader;
import com.griefcraft.util.Updater;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.reader.UnicodeReader;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

public class Configuration extends ConfigurationNode {
    private Yaml yaml;
    private File file;

    /**
     * List of loaded config files
     */
    private static Map<String, Configuration> loaded = new HashMap<String, Configuration>();

    public Configuration(File file) {
        super(new HashMap<String, Object>());

        DumperOptions options = new DumperOptions();
        options.setIndent(4);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        yaml = new Yaml(new SafeConstructor(), new Representer(), options);

        this.file = file;
    }

    /**
     * Reload the configuration maps
     */
    public static void reload() {
        for (Configuration configuration : loaded.values()) {
            configuration.load();
        }
    }

    /**
     * @return the list of loaded config files
     */
    public static Map<String, Configuration> getLoaded() {
        return loaded;
    }

    /**
     * Create and load a configuration file
     *
     * @param config
     * @return
     */
    public static Configuration load(String config) {
        File file = new File(ModuleLoader.ROOT_PATH + config);

        // if it does not exist, attempt to download it if possible :-)
        if (!file.exists()) {
            Updater updater = LWC.getInstance().getPlugin().getUpdater();
            updater.downloadConfig(config);
        }

        Configuration configuration = new Configuration(file);
        configuration.load();
        loaded.put(config, configuration);

        return configuration;
    }

    /**
     * Loads the configuration file. All errors are thrown away.
     */
    public void load() {
        FileInputStream stream = null;

        try {
            stream = new FileInputStream(file);
            read(yaml.load(new UnicodeReader(stream)));
        } catch (IOException e) {
            root = new HashMap<String, Object>();
        } catch (ConfigurationException e) {
            root = new HashMap<String, Object>();
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
            }
        }
    }

    /**
     * Saves the configuration to disk. All errors are clobbered.
     *
     * @return true if it was successful
     */
    public boolean save() {
        FileOutputStream stream = null;

        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }

        try {
            stream = new FileOutputStream(file);
            yaml.dump(root, new OutputStreamWriter(stream, "UTF-8"));
            return true;
        } catch (IOException e) {
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    private void read(Object input) throws ConfigurationException {
        try {
            if (null == input) {
                root = new HashMap<String, Object>();
            } else {
                root = (Map<String, Object>) input;
            }
        } catch (ClassCastException e) {
            throw new ConfigurationException("Root document must be an key-value structure");
        }
    }

    /**
     * This method returns an empty ConfigurationNode for using as a
     * default in methods that select a node from a node list.
     *
     * @return
     */
    public static ConfigurationNode getEmptyNode() {
        return new ConfigurationNode(new HashMap<String, Object>());
    }
}
