package nz.ac.wgtn.shadedetector.jcompile.oracles;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import static nz.ac.wgtn.shadedetector.jcompile.oracles.TestUtils.JARS;
import static nz.ac.wgtn.shadedetector.jcompile.oracles.TestUtils.pair;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AdjacentVersionSameArtifactAndCompilerJarOracleTest {

    private static List<Pair<Path, Path>> oracle = null;

    @BeforeAll
    public static void buildOracle () throws IOException {
        oracle = new AdjacentVersionSameArtifactAndCompilerJarOracle().build(JARS);
    }

    @AfterAll
    public static void resetOracle () throws IOException {
        oracle = null;
    }

    @Test
    public void testOracleSize() {
        // 3 adjacent version pairs / 3 compilers
        assertEquals(9,oracle.size());
    }

    @Test
    public void testBCEL1A() {
        assertTrue(oracle.contains(pair("openjdk-8.0.302/bcel-6.4.0.jar","openjdk-8.0.302/bcel-6.4.1.jar")));
    }

    @Test
    public void testBCEL2A() {
        assertTrue(oracle.contains(pair("openjdk-8.0.302/bcel-6.4.1.jar","openjdk-8.0.302/bcel-6.5.0.jar")));
    }

    @Test
    public void testCodecA() {
        assertTrue(oracle.contains(pair("openjdk-8.0.302/commons-codec-1.11.jar","openjdk-8.0.302/commons-codec-1.12.jar")));
    }

    @Test
    public void testBCEL1B() {
        assertTrue(oracle.contains(pair("openjdk-8.0.342/bcel-6.4.0.jar","openjdk-8.0.342/bcel-6.4.1.jar")));
    }

    @Test
    public void testBCEL2B() {
        assertTrue(oracle.contains(pair("openjdk-8.0.342/bcel-6.4.1.jar","openjdk-8.0.342/bcel-6.5.0.jar")));
    }

    @Test
    public void testCodecB() {
        assertTrue(oracle.contains(pair("openjdk-8.0.342/commons-codec-1.11.jar","openjdk-8.0.342/commons-codec-1.12.jar")));
    }

    @Test
    public void testBCEL1C() {
        assertTrue(oracle.contains(pair("openjdk-9.0.1/bcel-6.4.0.jar","openjdk-9.0.1/bcel-6.4.1.jar")));
    }

    @Test
    public void testBCEL2C() {
        assertTrue(oracle.contains(pair("openjdk-9.0.1/bcel-6.4.1.jar","openjdk-9.0.1/bcel-6.5.0.jar")));
    }

    @Test
    public void testCodecC() {
        assertTrue(oracle.contains(pair("openjdk-9.0.1/commons-codec-1.11.jar","openjdk-9.0.1/commons-codec-1.12.jar")));
    }

}
