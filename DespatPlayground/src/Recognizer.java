import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfRect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.HOGDescriptor;

import java.io.File;

/**
 * Created by volzotan on 17.12.16.
 */
public class Recognizer {
    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        File image = new File("pedestriancrossing");

        HOGDescriptor hog = new HOGDescriptor();
        hog.setSVMDetector(HOGDescriptor.getDefaultPeopleDetector());

        Mat cvimage = Imgcodecs.imread(image.getAbsolutePath());
        MatOfRect foundLocations = new MatOfRect();
        MatOfDouble foundWeights = new MatOfDouble();
        hog.detectMultiScale(cvimage, foundLocations, foundWeights);

    }

}
