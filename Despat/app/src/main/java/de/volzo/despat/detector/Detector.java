package de.volzo.despat.detector;


import java.io.File;

public abstract class Detector {

    public abstract void init() throws Exception;
    public abstract void load(File fullFilename);
    public abstract void run() throws Exception;

    class Resultset {

    }
}