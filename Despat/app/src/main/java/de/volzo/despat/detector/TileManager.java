package de.volzo.despat.detector;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.Log;
import android.util.Size;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TileManager {

    private int tilesize        = 500;
    private int outputsize      = 300;
    private boolean centered    = true;
    private boolean normalized  = true;

    private File filename       = null;
    private Bitmap image        = null;
    private Bitmap imageTile    = null;
    private Bitmap imageOutput  = null;
    private List<Tile> tiles    = new ArrayList<Tile>();

    private int xoffset = 0;
    private int yoffset = 0;

    public TileManager(File filename) {
        this.filename = filename;

        image = BitmapFactory.decodeFile(filename.getAbsolutePath());

        int imagewidth = image.getWidth();
        int imageheight = image.getHeight();

        if (this.centered) {
            xoffset = (imagewidth % tilesize) / 2;
            yoffset = (imageheight % tilesize) / 2;
        }

        int xTiles = imagewidth / tilesize;
        int yTiles = imageheight / tilesize;

        for (int y=0; y < yTiles; y++) {
            for (int x=0; x < xTiles; x++) {
                tiles.add(new Tile(y * xTiles + x, x, y));
            }
        }
    }

    public Size getImageSize() {
        return new Size(image.getWidth(), image.getHeight());
    }

    public List<Tile> getAllTiles() {
        return tiles;
    }

    private int[] getDimForTile(int x, int y) {
        int[] dim = new int[4];

        dim[0] = x * this.tilesize + this.xoffset;
        dim[1] = y * this.tilesize + this.yoffset;
        dim[2] = x * this.tilesize + this.xoffset + this.tilesize;
        dim[3] = y * this.tilesize + this.yoffset + this.tilesize;

        return dim;
    }

    private RectF convertCoordinates(Matrix matrix, RectF loc) {
        if (this.normalized) {

        }

        matrix.mapRect(loc);

        return loc;
    }

    public Bitmap getTileImage(Tile tile) {
        int[] dim = getDimForTile(tile.x, tile.y);

        imageTile = Bitmap.createBitmap(this.image, dim[0], dim[1], tilesize, tilesize);
        if (tilesize != outputsize) {
            imageOutput = Bitmap.createScaledBitmap(imageTile, outputsize, outputsize, false);
            imageTile.recycle();
        } else {
            imageOutput = imageTile;
        }

        return imageOutput;
    }

    public void passResult(Tile tile, List<TensorFlowDetector.Recognition> results) {
        tile.results = results;

        for(TensorFlowDetector.Recognition r : tile.results) {
            r.setLocation(convertCoordinates(tile.matrix, r.getLocation()));
        }
    }

    public List<TensorFlowDetector.Recognition> getFullResults() {
        List<TensorFlowDetector.Recognition> fullResultset = new LinkedList<TensorFlowDetector.Recognition>();

        for (Tile t : this.tiles) {
            fullResultset.addAll(t.results);
        }

        return fullResultset;
    }

    public class Tile {
        private int tileId;
        private int x;
        private int y;
        private Matrix matrix;
        List<TensorFlowDetector.Recognition> results;

        public Tile(int tileId, int x, int y) {
            this.tileId = tileId;
            this.x = x;
            this.y = y;

            this.matrix = new Matrix();
            matrix.setScale((float) tilesize / (float) outputsize, (float) tilesize/ (float) outputsize);
            matrix.postTranslate(xoffset + x * tilesize, yoffset + y * tilesize);
            Log.wtf("Tile", matrix.toString());
        }

        public void setResults(List<TensorFlowDetector.Recognition> results) {
            this.results = results;
        }
    }

}
