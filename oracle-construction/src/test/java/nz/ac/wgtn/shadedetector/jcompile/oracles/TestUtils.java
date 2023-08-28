package nz.ac.wgtn.shadedetector.jcompile.oracles;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import java.io.File;

public class TestUtils {

    public static File JARS = new File(TestUtils.class.getResource("/jars").getFile());

    static Pair<File,File> pair(String path1, String path2) {
        Assertions.assertTrue(!path1.equals(path2));
        File f1 = new File(JARS,path1);
        File f2 = new File(JARS,path2);
        Assertions.assertTrue(f1.exists());
        Assertions.assertTrue(f2.exists());
        return Pair.of(f1,f2);
    }

}
