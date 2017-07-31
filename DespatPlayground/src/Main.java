import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by volzotan on 21.01.17.
 */
public class Main {

    public static void main(String[] args) {
        Recognizer recognizer = new Recognizer();

        File[] foo = findAllFiles(new File("../datasets/ulm/5000px"), ".jpg");

        for (int i=0;i<foo.length;i++) {

            File image = new File(foo[i].getAbsolutePath());
            Recognizer.RecognizerResultset res = recognizer.run(image);

            System.out.format("image: %20s ; hits: %03d ; runtime: %2.3f%n", res.path.getName(), res.coordinates.length, res.runtime);

            Mat resimage = res.getBitmap();
            Imgcodecs.imwrite("result/result"+i+".jpg", resimage);
        }
    }

    public static File[] findAllFiles(File directory, String filterSuffix) {
        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String pathnamestr = pathname.getAbsolutePath();

                if (pathnamestr.length() > 4) {
                    String suffix = pathnamestr.substring(pathnamestr.length()-4);
                    if (suffix.equals(filterSuffix)) {
                        return true;
                    }
                }
                return false;
            }
        };

        File[] files = directory.listFiles(filter);
        return files;
    }
}
