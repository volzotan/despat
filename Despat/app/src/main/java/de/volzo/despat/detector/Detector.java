package de.volzo.despat.detector;


import java.io.File;

import de.volzo.despat.DrawSurface;

public abstract class Detector {

    public abstract void init() throws Exception;
    public abstract void load(File fullFilename);
    public abstract void run() throws Exception;
    public abstract void postprocess() throws Exception;
    public abstract void save() throws Exception;
    public abstract void display(DrawSurface surface);

    class Resultset {

    }
}