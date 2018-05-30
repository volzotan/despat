package de.volzo.despat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import de.volzo.despat.preferences.Config;

/**
 * Created by volzotan on 08.03.18.
 */

public class Compressor {

    public static final String TAG = Compressor.class.getSimpleName();

//    private long[][][] mat;
    private Mat mat;
    private int width;
    private int height;

    public Compressor() {
        System.loadLibrary("opencv_java3");
    }

    public void test(Context context) {

        File imageFolder = Config.getImageFolder(context);
        List<File> images = new ArrayList<File>();

        for (final File fileEntry : imageFolder.listFiles()) {
            if (fileEntry.isDirectory()) {
                continue;
            }

            if (!fileEntry.getName().toLowerCase().endsWith(".jpg")) {
                continue;
            }

            images.add(fileEntry);
        }

        images = images.subList(0, 1);
        Bitmap bitmap = BitmapFactory.decodeFile(images.get(0).getAbsolutePath());

        init(bitmap.getWidth(), bitmap.getHeight());

        for (File f : images) {
            add(f);
        }

        toJpeg(new File(imageFolder, "compressed.jpg"));
    }

    public void init(int width, int height) {
        this.width = width;
        this.height = height;
//        mat = new long[this.width][this.height][1];
        mat = Mat.zeros(this.height, this.width, CvType.CV_16U);
    }

    public void add(Image image) {

    }

    public void add(File path) {
        Mat imgMat = Imgcodecs.imread(path.getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
        Mat mask = Mat.ones(mat.height(), mat.width(), CvType.CV_8UC1);
        Core.add(mat, imgMat, mat, mask, CvType.CV_16U);
    }

    public void store() {

    }

    public void unstore() {

    }

    public void toJpeg(File path) {
        mat.convertTo(mat, CvType.CV_8UC1);
        Mat mat2 = Mat.zeros(mat.height(), mat.width(), CvType.CV_8UC3);
        Imgproc.cvtColor(mat, mat2, Imgproc.COLOR_GRAY2RGB);
        Bitmap bitmap = Bitmap.createBitmap(mat2.width(), mat2.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat2, bitmap);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        try {
            FileOutputStream fileOuputStream = new FileOutputStream(path);
            fileOuputStream.write(byteArray);
            fileOuputStream.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
