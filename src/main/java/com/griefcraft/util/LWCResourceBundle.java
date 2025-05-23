package com.griefcraft.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

public class LWCResourceBundle extends ResourceBundle {

    /**
     * Stores bundles that override the defaults
     */
    private List<ResourceBundle> extensionBundles = new ArrayList<ResourceBundle>();

    public LWCResourceBundle(ResourceBundle parent) {
        this.parent = parent;
    }

    /**
     * Add a ResourceBundle to the extras
     *
     * @param bundle
     */
    public void addExtensionBundle(ResourceBundle bundle) {
        if (bundle == null) {
            return;
        }

        extensionBundles.add(bundle);
    }

    @Override
    public Enumeration<String> getKeys() {
        Set<String> keys = new HashSet<String>();
        keys.addAll(parent.keySet());

        // add the extension bundles' keys as well
        for (ResourceBundle bundle : extensionBundles) {
            keys.addAll(bundle.keySet());
        }

        return Collections.enumeration(keys);
    }

    /**
     * Get an object from one of the extension bundles
     *
     * @param key
     * @return
     */
    private Object getObjectFromExtensionBundles(String key) {
        try {
            for (ResourceBundle bundle : extensionBundles) {
                Object object = bundle.getObject(key);

                if (object != null) {
                    return object;
                }
            }
        } catch (MissingResourceException e) {
        }

        return null;
    }

    @Override
    protected Object handleGetObject(String key) {
        Object object = null;

        if ((object = getObjectFromExtensionBundles(key)) != null) {
            return object;
        }

        return parent.getObject(key);
    }

}
