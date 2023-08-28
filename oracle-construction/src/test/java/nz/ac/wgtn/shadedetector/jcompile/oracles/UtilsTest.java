package nz.ac.wgtn.shadedetector.jcompile.oracles;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UtilsTest {

    @Test
    public void testInnerClassDetector1() {
        assertTrue(Utils.isAnonymousInnerClass(Path.of("/org/apache/commons/csv/CSVFormat$1.class")));
    }

    @Test
    public void testInnerClassDetector2() {
        assertFalse(Utils.isAnonymousInnerClass(Path.of("/org/apache/commons/csv/CSVFormat$Predefined.class")));
    }

    @Test
    public void testInnerClassDetector3() {
        assertFalse(Utils.isAnonymousInnerClass(Path.of("/org/apache/commons/csv/IOUtils.class")));
    }

    @Test
    public void testPackageInfoDetector1() {
        Path p = Path.of("/org/apache/commons/csv/package-info.class");
        assertTrue(Utils.isPackageInfo(p));
    }

    @Test
    public void testPackageInfoDetector2() {
        Path p = Path.of("/org/apache/commons/csv/IOUtils.class");
        assertFalse(Utils.isPackageInfo(p));
    }
}
