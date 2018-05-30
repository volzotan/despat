package de.volzo.despat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.volzo.despat.preferences.Config;

/**
 *  The compressor takes up to 256 images and
 *  computes the mean.
 *
 *  The images are stacked in a opencv 16UC1 matrix,
 *  which overflows at 2^16=65536. JPEG consists of up to
 *  2^8=256 per pixel. Thus, a maximum of 2^8=256 jpegs
 *  can be stacked.
 *
 *  To preserve memory only a single grayscale channel
 *  is used.
 *
 */

public class Compressor {

    public static final String TAG = Compressor.class.getSimpleName();

    private Mat mat;
    private int counter = 0;

    private int width;
    private int height;

    public Compressor() {
        System.loadLibrary("opencv_java3");
    }

    public void test(Context context) {

        long time_searchFiles = System.currentTimeMillis();
        long time_init = 0;
        long time_add = 0;
        long time_toJpeg = 0;

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

        time_searchFiles = System.currentTimeMillis() - time_searchFiles;
        time_init = System.currentTimeMillis();

        images = images.subList(0, 10);
        Bitmap bitmap = BitmapFactory.decodeFile(images.get(0).getAbsolutePath());

        init(bitmap.getWidth(), bitmap.getHeight());

        time_init = System.currentTimeMillis() - time_init;
        time_add = System.currentTimeMillis();

        for (File f : images) {
            add(f);
            System.out.println(counter);
        }

        time_add = System.currentTimeMillis() - time_add;
        time_toJpeg = System.currentTimeMillis();

        toJpeg(new File(imageFolder, "compressed.jpg"));

        time_toJpeg = System.currentTimeMillis() - time_toJpeg;

        System.out.println();
        System.out.println("searchFiles: " + time_searchFiles);
        System.out.println("init: " + time_init);
        System.out.println("add: " + time_add);
        System.out.println("add per image: " + time_add/counter);
        System.out.println("toJpeg: " + time_toJpeg);
    }

    public void init(int width, int height) {
        this.width = width;
        this.height = height;
        mat = Mat.zeros(this.height, this.width, CvType.CV_16UC1);
    }

    public void add(Image image) {

    }

    public void add(File path) {
        if (counter >= 255) {
            Log.e(TAG, "overflow imminent. image ignored.");
            return;
        }

        Mat imgMat = Imgcodecs.imread(path.getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);

        if (imgMat.width() != this.width || imgMat.height() != this.height) {
            Log.w(TAG, "incompatible size of file: " + path.getName());
            return;
        }

        Mat mask = Mat.ones(mat.height(), mat.width(), CvType.CV_8UC1);
        Core.add(mat, imgMat, mat, mask, CvType.CV_16U);

        imgMat.release();
        mask.release();

        counter++;
    }

    public void store(File path) throws Exception {
        try {
            FileOutputStream fos = new FileOutputStream(path);
            short[] data = new short[1];
            byte[] binary = new byte[2];

            if (mat == null) {
                throw new Exception("compressor must be initialized");
            }

            for (int i=0; i<mat.rows(); i++) {
                for (int j=0; j<mat.cols(); j++) {
                    mat.get(i, j, data);

                    binary[1] = (byte)(data[0] >>> 8);
                    binary[0] = (byte)(data[0]);
                    fos.write(binary);
                }
            }

            fos.flush();
            fos.close();
        } catch (IOException e) {
            Log.e(TAG, "storing failed", e);
            throw e;
        }
    }

    public void unstore(File path) throws Exception {
        try {
            short[] data = new short[1];
            byte[] binary = new byte[2];

            if (mat == null) {
                throw new Exception("compressor must be initialized");
            }

            FileInputStream fis = new FileInputStream(path);

            for (int i=0; i<mat.rows(); i++) {
                for (int j=0; j<mat.cols(); j++) {
                    int pos = i * mat.rows() * 2 + j * 2;

                    try {
                        int ret = fis.read(binary);

                        if (ret <= 0) {
                            throw new IOException("file ended prematurely");
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        Log.e(TAG, "out of bounds. pos: " + pos);
                        throw e;
                    }

                    data[0] = (short) ((binary[1] << 8) + binary[0]);

                    mat.put(i, j, data);
                }
            }

            fis.close();
        } catch (IOException e) {
            Log.e(TAG, "storing failed", e);
            throw e;
        }
    }



    public void toJpeg(File path) {
        Mat matExportGray = Mat.zeros(mat.height(), mat.width(), CvType.CV_8UC1);
        Core.multiply(mat, new Scalar(1.0/counter), matExportGray);
        matExportGray.convertTo(matExportGray, CvType.CV_8UC1);

        Mat matExportColor = Mat.zeros(mat.height(), mat.width(), CvType.CV_8UC3);
        Imgproc.cvtColor(matExportGray, matExportColor, Imgproc.COLOR_GRAY2RGB);
        matExportGray.release();
        Bitmap bitmap = Bitmap.createBitmap(matExportColor.width(), matExportColor.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(matExportColor, bitmap);
        matExportColor.release();

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
