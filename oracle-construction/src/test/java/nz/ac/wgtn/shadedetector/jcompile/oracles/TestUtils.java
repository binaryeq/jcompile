package nz.ac.wgtn.shadedetector.jcompile.oracles;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestUtils {

    public static Path JARS = Path.of(TestUtils.class.getResource("/jars").getFile());

    static Pair<Path,Path> pair(String path1, String path2) {
        Assertions.assertTrue(!path1.equals(path2));
        Path f1 = JARS.resolve(path1);
        Path f2 = JARS.resolve(path2);
        Assertions.assertTrue(Files.exists(f1));
        Assertions.assertTrue(Files.exists(f2));
        return Pair.of(f1,f2);
    }

}
