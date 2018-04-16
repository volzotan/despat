package de.volzo.despat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by volzotan on 08.03.18.
 */

public class Compressor {

    private long[][][] mat;

    public Compressor() {

    }

    public void init(int width, int height) {
        mat = new long[width][height][3];
    }

    public void add(Image image) {

    }

    public void add(File path) {
        Bitmap bitmap = BitmapFactory.decodeFile(path.getAbsolutePath());

        for (int i=0; i<bitmap.getHeight(); i++) {
            for (int j=0; j<bitmap.getWidth(); j++) {
                int c = bitmap.getPixel(i, j);
                mat[i][j][0] += (c >> 16) & 0xff;
                mat[i][j][1] += (c >>  8) & 0xff;
                mat[i][j][2] += (c      ) & 0xff;
            }
        }
    }

    public void store() {

    }

    public void unstore() {

    }

    public void toJpeg(File path) {
        Bitmap bitmap = Bitmap.createBitmap(mat.length, mat[0].length, Bitmap.Config.ARGB_8888);
        for (int i=0; i<bitmap.getHeight(); i++) {
            for (int j=0; j<bitmap.getWidth(); j++) {
                int c = (1 & 0xff) << 24 | ((int) mat[i][j][0] & 0xff) << 16 | ((int) mat[i][j][1] & 0xff) << 8 | ((int) mat[i][j][2] & 0xff);
                bitmap.setPixel(i, j, c);
            }
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        try {
            FileOutputStream fileOuputStream = new FileOutputStream(path);
            fileOuputStream.write(byteArray);
            fileOuputStream.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
