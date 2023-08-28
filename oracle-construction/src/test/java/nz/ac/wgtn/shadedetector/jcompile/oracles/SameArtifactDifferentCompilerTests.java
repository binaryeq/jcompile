package nz.ac.wgtn.shadedetector.jcompile.oracles;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.util.List;
import static nz.ac.wgtn.shadedetector.jcompile.oracles.TestUtils.JARS;
import static nz.ac.wgtn.shadedetector.jcompile.oracles.TestUtils.pair;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SameArtifactDifferentCompilerTests {

    private static List<Pair<File, File>> oracle = null;

    @BeforeAll
    public static void buildOracle () throws IOException {
        oracle = new SameArtifactDifferentCompiler().build(JARS);
    }

    @AfterAll
    public static void resetOracle () throws IOException {
        oracle = null;
    }

    @Test
    public void testOracleSize() {
        // 5 artifacts / 3 compilers
        assertEquals(10,oracle.size());
    }

    @Test
    public void testBCEL1() {
        assertTrue(oracle.contains(pair("openjdk-8.0.302/bcel-6.4.0.jar","openjdk-8.0.342/bcel-6.4.0.jar")));
    }

    @Test
    public void testBCEL2() {
        assertTrue(oracle.contains(pair("openjdk-8.0.302/bcel-6.4.1.jar","openjdk-8.0.342/bcel-6.4.1.jar")));
    }

    @Test
    public void testBCEL3() {
        assertTrue(oracle.contains(pair("openjdk-8.0.302/bcel-6.5.0.jar","openjdk-8.0.342/bcel-6.5.0.jar")));
    }

    @Test
    public void testBCEL4() {
        assertTrue(oracle.contains(pair("openjdk-8.0.342/bcel-6.4.0.jar","openjdk-9.0.1/bcel-6.4.0.jar")));
    }

    @Test
    public void testBCEL5() {
        assertTrue(oracle.contains(pair("openjdk-8.0.342/bcel-6.4.1.jar","openjdk-9.0.1/bcel-6.4.1.jar")));
    }

    @Test
    public void testBCEL6() {
        assertTrue(oracle.contains(pair("openjdk-8.0.342/bcel-6.5.0.jar","openjdk-9.0.1/bcel-6.5.0.jar")));
    }

    @Test
    public void testCodec1() {
        assertTrue(oracle.contains(pair("openjdk-8.0.302/commons-codec-1.11.jar","openjdk-8.0.342/commons-codec-1.11.jar")));
    }

    @Test
    public void testCodec2() {
        assertTrue(oracle.contains(pair("openjdk-8.0.302/commons-codec-1.12.jar","openjdk-8.0.342/commons-codec-1.12.jar")));
    }


    @Test
    public void testCodec3() {
        assertTrue(oracle.contains(pair("openjdk-8.0.342/commons-codec-1.11.jar","openjdk-9.0.1/commons-codec-1.11.jar")));
    }

    @Test
    public void testCodec4() {
        assertTrue(oracle.contains(pair("openjdk-8.0.342/commons-codec-1.12.jar","openjdk-9.0.1/commons-codec-1.12.jar")));
    }

}
