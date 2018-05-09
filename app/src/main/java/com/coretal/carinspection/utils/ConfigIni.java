package com.coretal.carinspection.utils;

/**
 * Created by Kangtle_R on 3/5/2018.
 */


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigIni {

    private Properties configuration;
    private String configurationFile = "config.ini";

    public ConfigIni() {
        configuration = new Properties();
    }

    public boolean load() {
        boolean retval = false;

        try {
            configuration.load(new FileInputStream(Contents.Config.FILE_PATH));
            retval = true;
        } catch (IOException e) {
            System.out.println("Configuration error: " + e.getMessage());
        }

        return retval;
    }

    public boolean store() {
        boolean retval = false;

        try {
            configuration.store(new FileOutputStream(Contents.Config.FILE_PATH), null);
            retval = true;
        } catch (IOException e) {
            System.out.println("Configuration error: " + e.getMessage());
        }
        return retval;
    }

    public void set(String key, String value) {
        configuration.setProperty(key, value);
    }

    public String get(String key) {
        return configuration.getProperty(key);
    }

    public Object getObject(String key) {
        return configuration.get(key);
    }
}
