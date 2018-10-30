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


    public TileManager(File filename, int tilesize, int outputsize) {
        this(BitmapFactory.decodeFile(filename.getAbsolutePath()), tilesize, outputsize);
        this.filename = filename;
    }

    public TileManager(Bitmap bitmap, int tilesize, int outputsize) {
        this.tilesize = tilesize;
        this.outputsize = outputsize;

        image = bitmap;

        int imagewidth = image.getWidth();
        int imageheight = image.getHeight();

        init(new Size(imagewidth, imageheight));
    }

    /*
     * Initializes an empty TileManager which should _not_ be used to try to access image tile
     * contents. Only usecase is to obtain information about the placement and size of the
     * tiling for an overlay on the compressed image during visualization or
     * estimating computation time
     *
     */
    public TileManager(Size imageSize, int tilesize) {
        this.tilesize = tilesize;
        init(imageSize);
    }

    private void init(Size imageSize) {
        if (this.centered) {
            xoffset = (imageSize.getWidth() % tilesize) / 2;
            yoffset = (imageSize.getHeight() % tilesize) / 2;
        }

        int xTiles = imageSize.getWidth() / tilesize;
        int yTiles = imageSize.getHeight() / tilesize;

        for (int y=0; y < yTiles; y++) {
            for (int x=0; x < xTiles; x++) {
                tiles.add(new Tile(y * xTiles + x, x, y));
            }
        }
    }

    public Size getImageSize() {
        return new Size(image.getWidth(), image.getHeight());
    }

    public List<RectF> getTileBoxes() {
        List<RectF> rectangles = new ArrayList<RectF>();

        for (Tile tile : tiles) {
            int[] dim = getDimForTile(tile.x, tile.y);
            rectangles.add(new RectF(dim[0], dim[1], dim[2], dim[3]));
        }

        return rectangles;
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

    public void passResult(Tile tile, List<Detector.Recognition> results) {
        tile.results = results;

        for(Detector.Recognition r : tile.results) {
            r.setLocation(convertCoordinates(tile.matrix, r.getLocation()));
        }
    }

    public List<Detector.Recognition> getFullResults() {
        List<Detector.Recognition> fullResultset = new LinkedList<Detector.Recognition>();

        for (Tile t : this.tiles) {
            fullResultset.addAll(t.results);
        }

        return fullResultset;
    }

    public void close() {
        if (image != null) image.recycle();
        if (imageTile != null) imageTile.recycle();
        if (imageOutput != null) imageOutput.recycle();
        if (tiles != null) tiles.clear();
    }

    public class Tile {
        private int tileId;
        private int x;
        private int y;
        private Matrix matrix;
        List<Detector.Recognition> results;

        public Tile(int tileId, int x, int y) {
            this.tileId = tileId;
            this.x = x;
            this.y = y;

            this.matrix = new Matrix();
            matrix.setScale((float) tilesize / (float) outputsize, (float) tilesize/ (float) outputsize);
            matrix.postTranslate(xoffset + x * tilesize, yoffset + y * tilesize);
        }

        public void setResults(List<Detector.Recognition> results) {
            this.results = results;
        }
    }

}
