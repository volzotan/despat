package de.volzo.despat.detector;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TileManager {

    private int tilesize        = 1000;
    private int outputsize      = 300;
    private boolean centered    = true;

    private File filename       = null;
    private Bitmap image        = null;
    private Bitmap imageTile    = null;
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

    public Bitmap getTileImage(Tile tile) {
        int[] dim = getDimForTile(tile.x, tile.y);

        imageTile = Bitmap.createBitmap(this.image, dim[0], dim[1], tilesize, tilesize);
        return imageTile;
    }


    public class Tile {
        private int tileId;
        private int x;
        private int y;
        List<TensorFlowDetector.Recognition> results;

        public Tile(int tileId, int x, int y) {
            this.tileId = tileId;
            this.x = x;
            this.y = y;
        }

        public void setResults(List<TensorFlowDetector.Recognition> results) {
            this.results = results;
        }
    }

}
