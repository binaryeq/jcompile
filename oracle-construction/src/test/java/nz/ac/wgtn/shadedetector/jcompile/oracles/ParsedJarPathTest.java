package nz.ac.wgtn.shadedetector.jcompile.oracles;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ParsedJarPathTest {
    @Test
    public void testParsedJarPath_parseRegularPath() {
        Path p = Path.of("/foo/bar/openjdk-1.2.3/some-project-4.5.6.jar");

        ParsedJarPath result = ParsedJarPath.parse(p);

        assertNotNull(result);
        assertNotNull(result.compiler());
        assertEquals("openjdk", result.compiler().name());
        assertEquals("1", result.compiler().majorVersion());
        assertEquals("2", result.compiler().minorVersion());
        assertEquals("3", result.compiler().patchVersion());
        assertNull(result.compiler().extraConfiguration());
        assertNotNull(result.compiler());
        assertEquals("some-project", result.project().name());
        assertEquals("4", result.project().majorVersion());
        assertEquals("5", result.project().minorVersion());
        assertEquals("6", result.project().patchVersion());
        assertEquals("", result.project().jarType());
    }

    @Test
    public void testParsedJarPath_parseTestPath() {
        Path p = Path.of("/foo/bar/openjdk-1.2.3/some-project-4.5.6-tests.jar");

        ParsedJarPath result = ParsedJarPath.parse(p);

        assertNotNull(result);
        assertNotNull(result.compiler());
        assertEquals("openjdk", result.compiler().name());
        assertEquals("1", result.compiler().majorVersion());
        assertEquals("2", result.compiler().minorVersion());
        assertEquals("3", result.compiler().patchVersion());
        assertNull(result.compiler().extraConfiguration());
        assertNotNull(result.compiler());
        assertEquals("some-project", result.project().name());
        assertEquals("4", result.project().majorVersion());
        assertEquals("5", result.project().minorVersion());
        assertEquals("6", result.project().patchVersion());
        assertEquals("-tests", result.project().jarType());
    }

    @Test
    public void testParsedJarPath_parseEcj() {
        Path p = Path.of("/foo/bar/ecj-10.11.12_openjdk-1.2.3/some-project-4.5.6-tests.jar");

        ParsedJarPath result = ParsedJarPath.parse(p);

        assertNotNull(result);
        assertNotNull(result.compiler());
        assertEquals("ecj", result.compiler().name());
        assertEquals("10", result.compiler().majorVersion());
        assertEquals("11", result.compiler().minorVersion());
        assertEquals("12", result.compiler().patchVersion());
        assertEquals("openjdk-1.2.3", result.compiler().extraConfiguration());
        assertNotNull(result.compiler());
        assertEquals("some-project", result.project().name());
        assertEquals("4", result.project().majorVersion());
        assertEquals("5", result.project().minorVersion());
        assertEquals("6", result.project().patchVersion());
        assertEquals("-tests", result.project().jarType());
    }

    @Test
    public void testParsedJarPath_parseHorribleEcj() {
        Path p = Path.of("jars/ecj-3.11.1.v20150902-1521_openjdk-11.0.19/commons-codec-1.11.jar");

        ParsedJarPath result = ParsedJarPath.parse(p);

        assertNotNull(result);
        assertNotNull(result.compiler());
        assertEquals("ecj", result.compiler().name());
        assertEquals("3", result.compiler().majorVersion());
        assertEquals("11", result.compiler().minorVersion());
        assertEquals("1.v20150902-1521", result.compiler().patchVersion());
        assertEquals("openjdk-11.0.19", result.compiler().extraConfiguration());
        assertNotNull(result.compiler());
        assertEquals("commons-codec", result.project().name());
        assertEquals("1", result.project().majorVersion());
        assertEquals("11", result.project().minorVersion());
        assertNull(result.project().patchVersion());
        assertEquals("-tests", result.project().jarType());
    }

}