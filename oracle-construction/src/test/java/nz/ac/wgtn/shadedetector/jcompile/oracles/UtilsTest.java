package nz.ac.wgtn.shadedetector.jcompile.oracles;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.Path;

import static nz.ac.wgtn.shadedetector.jcompile.oracles.TestUtils.JARS;
import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    public void testGetTopLevelClass_alreadyTopLevel() {
        Path p = Path.of("/abc/SomeClass.class");
        assertEquals(p, Utils.getTopLevelClass(p));
    }

    @Test
    public void testGetTopLevelClass_innerClass() {
        Path p = Path.of("/abc/SomeClass$InnerClass.class");
        assertEquals(Path.of("/abc/SomeClass.class"), Utils.getTopLevelClass(p));
    }

    @Test
    public void testGetTopLevelClass_anonymousInnerClass() {
        Path p = Path.of("/abc/SomeClass$123.class");
        assertEquals(Path.of("/abc/SomeClass.class"), Utils.getTopLevelClass(p));
    }

    @Test
    public void testGetTopLevelClass_multiLevelInnerClass() {
        Path p = Path.of("/abc/SomeClass$TopInnerClass$42$LowestInner.class");
        assertEquals(Path.of("/abc/SomeClass.class"), Utils.getTopLevelClass(p));
    }

    @Test
    public void testGetTopLevelClass_preservesFileSystem() throws URISyntaxException, IOException {
        try (FileSystem zipfs = Utils.getJarFileSystem(JARS.resolve("openjdk-9.0.1").resolve("commons-codec-1.11.jar"))) {
            Path p = zipfs.getPath("/abc/SomeClass.class");
            Path defaultFileSystemPath = Path.of("/abc/SomeClass.class");
            Path result = Utils.getTopLevelClass(p);
            assertEquals(result.getFileSystem(), p.getFileSystem());
            assertNotEquals(result, defaultFileSystemPath);
        }
    }
}
