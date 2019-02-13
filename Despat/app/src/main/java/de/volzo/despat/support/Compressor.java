package de.volzo.despat.support;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.Capture;
import de.volzo.despat.persistence.CaptureDao;
import de.volzo.despat.persistence.Session;
import de.volzo.despat.persistence.SessionDao;
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

public class Compressor implements Callable<Integer> {

    private static final String TAG = Compressor.class.getSimpleName();

    private static final int MAX_NUMBER_OF_IMAGES = 150;

    private Mat mat;
    private int counter = 0;

    private int width;
    private int height;

    public Compressor() {
        System.loadLibrary("opencv_java3");
    }

//    public void serializeTest(Context context) {
//        File imageFolder = Config.getImageFolder(context);
//
//        init(20, 10);
//        mat = Mat.ones(10, 20, CvType.CV_16UC1);
//
//        Core.add(mat, mat, mat);
//        Core.add(mat, mat, mat);
//        Core.add(mat, mat, mat);
//        Core.add(mat, mat, mat);
//        Core.add(mat, mat, mat);
//        Core.add(mat, mat, mat);
//
//        Log.wtf(TAG, mat.dump());
//
//        store(new File(imageFolder, "mat.dat"));
//
//        init(20, 10);
//        Log.wtf(TAG, mat.dump());
//
//        unstore(new File(imageFolder, "mat.dat"));
//
//        Log.wtf(TAG, mat.dump());
//    }

    public void test(Context context) {

        long time_searchFiles = System.currentTimeMillis();
        long time_init = 0;
        long time_add = 0;
        long time_store = 0;
        long time_unstore = 0;
        long time_toJpeg = 0;

        File imageFolder = Config.getImageFolders(context).get(0);
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

        images = images.subList(0, 20);
        Bitmap bitmap = BitmapFactory.decodeFile(images.get(0).getAbsolutePath());

        init(bitmap.getWidth()/2, bitmap.getHeight()/2);

        time_init = System.currentTimeMillis() - time_init;
        time_add = System.currentTimeMillis();

        for (File f : images) {
            try {
                add(f);
            } catch (Exception e) {
                Log.e(TAG, "adding failed", e);
            }
            Log.d(TAG, "image added: " + counter);
        }

        time_add = System.currentTimeMillis() - time_add;

        try {
            time_store = System.currentTimeMillis();

            System.out.println(this.mat.get(0, 0)[0]);

            File storeFile = new File(imageFolder, "test.pgm");
            store(storeFile);

            time_store = System.currentTimeMillis() - time_store;
            time_unstore = System.currentTimeMillis();

            unstore(storeFile);

            System.out.println(this.mat.get(0, 0)[0]);

            time_unstore = System.currentTimeMillis() - time_unstore;
        } catch (Exception e) {
            Log.e(TAG, "storing/unstoring failed", e);
        }

        time_toJpeg = System.currentTimeMillis();

        toJpeg(new File(imageFolder, "compressed.jpg"));

        time_toJpeg = System.currentTimeMillis() - time_toJpeg;

        System.out.println();
        System.out.println("searchFiles: " + time_searchFiles);
        System.out.println("init: " + time_init);
        System.out.println("add: " + time_add);
        System.out.println("add per image: " + time_add/counter);
        System.out.println("store: " + time_store);
        System.out.println("unstore: " + time_unstore);
        System.out.println("toJpeg: " + time_toJpeg);
    }

    public void runForSession(Context context, Session session) throws Exception {
        AppDatabase db = AppDatabase.getAppDatabase(context);
        SessionDao sessionDao = db.sessionDao();
        CaptureDao captureDao = db.captureDao();

        if (session == null) {
            Log.w(TAG, "session null");
            return;
        }

        if (session.getCompressedImage() != null) {
            Log.w(TAG, "cannot run compressor. session already has compressed image: " + session);
            return;
        }

        List<Capture> captures = captureDao.getAllBySession(session.getId());

        if (captures == null || captures.size() < 1) {
            Log.w(TAG, "cannot run compressor. no captures for session: " + session);
            return;
        }

        init(session.getImageWidth(), session.getImageHeight());

        File imageFolder = Config.getImageFolders(context).get(0);
        File compressedImagePath = new File(imageFolder, session.getSessionName() + ".jpg");
        File compressedStorePath = new File(imageFolder, session.getSessionName() + ".pgm");
        if (compressedStorePath.exists()) {
            Log.i(TAG, "found existing store for session: " + session);
            unstore(compressedStorePath);
            counter = captureDao.getNumberOfCompressorProcessedCaptures(session.getId());
        }

//        // load first image to get width and height for initialization
//        File imageFile = captures.get(0).getImage();
//        if (imageFile == null || !imageFile.exists()) {
//            throw new Exception("first image file missing");
//        }
//
//        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
//        init(bitmap.getWidth(), bitmap.getHeight());

        captures = captures.subList(0, Math.min(captures.size(), MAX_NUMBER_OF_IMAGES));

        for (Capture c : captures) {

            if (c.isProcessed_compressor()) {
                continue;
            }

            try {
                add(c.getImage());
            } catch (Exception e) {
                Log.e(TAG, "adding to compressor failed", e);
                Util.saveErrorEvent(context, session.getId(), "compression error", e);
                return;
            }
            c.setProcessed_compressor(true);
            captureDao.update(c);

            Log.d(TAG, "image added: " + counter);
        }

        // complete if:
        // session has ended (=> has end date)
        // all images have processed_compressor == true
        // at least MAX_NUMBER_OF_IMAGES have processed_compressor = true
        int numberOfProcessedCaptures = captureDao.getNumberOfCompressorProcessedCaptures(session.getId());
        if (session.getEnd() != null && numberOfProcessedCaptures == captures.size() || numberOfProcessedCaptures >= MAX_NUMBER_OF_IMAGES) {
            toJpeg(compressedImagePath);
            session.setCompressedImage(compressedImagePath);
            sessionDao.update(session);

            if (compressedStorePath.exists()) {
                compressedStorePath.delete();
//                context.deleteFile(compressedStorePath.getAbsolutePath());
                Log.i(TAG, "removed store for session: " + session);
            }

            Log.d(TAG, "created compressed image for session " + session + " in " + compressedImagePath);
        } else {
            store(compressedStorePath);
            Log.d(TAG, "created intermediate store for session " + session + " in " + compressedStorePath);
        }
    }


    public void init(int width, int height) {
        this.width = width;
        this.height = height;
        mat = Mat.zeros(this.height, this.width, CvType.CV_16UC1);
    }

    public void add(File path) throws Exception {
        if (counter >= 255) {
            Log.e(TAG, "overflow imminent. image ignored.");
            return;
        }

        Mat imgMat = Imgcodecs.imread(path.getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);

        if (imgMat.empty()) {
            throw new Exception("image empty. not added to compressor");
        }

        Imgproc.resize(imgMat, imgMat, new Size(this.width, this.height));

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

    // Store as a Portable Greymap Image
    // Max value: 65536
    private void store(File path) throws Exception {
        Imgcodecs.imwrite(path.getAbsolutePath(), this.mat);
    }

    private void unstore(File path) throws Exception {
        this.mat = Imgcodecs.imread(path.getAbsolutePath(), Imgcodecs.IMREAD_UNCHANGED);
    }

    // Do NOT use for large matrices (ie. images). Slow.
//    public void store(File path) throws Exception {
//        long start = System.currentTimeMillis();
//        try {
//            FileOutputStream fos = new FileOutputStream(path);
//            short[] data = new short[1];
//            byte[] binary = new byte[2];
//
//            if (mat == null) {
//                throw new Exception("compressor must be initialized");
//            }
//
//            for (int i=0; i<mat.rows(); i++) {
//                for (int j=0; j<mat.cols(); j++) {
//                    mat.get(i, j, data);
//
//                    binary[1] = (byte)(data[0] >>> 8);
//                    binary[0] = (byte)(data[0]);
//                    fos.write(binary);
//                }
//            }
//
//            fos.flush();
//            fos.close();
//        } catch (IOException e) {
//            Log.e(TAG, "storing failed", e);
//            throw e;
//        }
//
//        Log.d(TAG, String.format("storing took %dms", System.currentTimeMillis() - start));
//    }
//
//    public void unstore(File path) throws Exception {
//        try {
//            short[] data = new short[1];
//            byte[] binary = new byte[2];
//
//            if (mat == null) {
//                throw new Exception("compressor must be initialized");
//            }
//
//            FileInputStream fis = new FileInputStream(path);
//
//            for (int i=0; i<mat.rows(); i++) {
//                for (int j=0; j<mat.cols(); j++) {
//                    int pos = i * mat.rows() * 2 + j * 2;
//
//                    try {
//                        int ret = fis.read(binary);
//                        if (ret <= 0) {
//                            throw new IOException("file ended prematurely");
//                        }
//                    } catch (ArrayIndexOutOfBoundsException e) {
//                        Log.e(TAG, "out of bounds. pos: " + pos);
//                        throw e;
//                    }
//
//                    data[0] = (short) ((binary[1] << 8) + binary[0]);
//                    mat.put(i, j, data);
//                }
//            }
//
//            fis.close();
//        } catch (IOException e) {
//            Log.e(TAG, "storing failed", e);
//            throw e;
//        }
//    }

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

    public void close() {
        if (mat != null) mat.release();
    }

    @Override
    public Integer call() throws Exception {
        return null;
    }
}
