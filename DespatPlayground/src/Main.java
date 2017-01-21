import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

/**
 * Created by volzotan on 21.01.17.
 */
public class Main {

    public static void main(String[] args) {
        Recognizer recognizer = new Recognizer();

        File image = new File("/Users/volzotan/Desktop/export_pedestrian_2_muenster/DSCF7415.jpg");
        Recognizer.RecognizerResultset res = recognizer.run(image);

        Mat resimage = res.matrix;
        Imgproc.cvtColor(resimage, resimage, Imgproc.COLOR_BGR2RGB);
        Imgcodecs.imwrite("result.jpg", resimage);
    }
}
