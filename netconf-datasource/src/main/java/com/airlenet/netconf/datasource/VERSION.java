package com.airlenet.netconf.datasource;

public class VERSION {

    public final static int MajorVersion = 2;
    public final static int MinorVersion = 0;
    public final static int RevisionVersion = 0;

    public static String getVersionNumber() {
        return VERSION.MajorVersion + "." + VERSION.MinorVersion + "." + VERSION.RevisionVersion;
    }
}
