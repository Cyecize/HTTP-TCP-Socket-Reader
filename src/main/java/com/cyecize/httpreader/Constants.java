package com.cyecize.httpreader;

public class Constants {
    /**
     * Path to the resource folder relative to this exact class.
     */
    public static final String FILES_DIR = "../../../files/";

    public static final String FILES_CANONICAL_PATH = Constants.class.getResource("").getFile() + FILES_DIR;
}
