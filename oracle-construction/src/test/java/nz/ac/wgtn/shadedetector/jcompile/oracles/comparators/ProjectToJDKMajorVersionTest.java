package nz.ac.wgtn.shadedetector.jcompile.oracles.comparators;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ProjectToJDKMajorVersionTest {

    @Test
    public void testOpenJdk() {
        ProjectToJDKMajorVersion project = new ProjectToJDKMajorVersion();
        assertEquals("42", project.apply("openjdk-42.2.3"));
    }

    @Test
    public void testEcjFullMatch() {
        ProjectToJDKMajorVersion project = new ProjectToJDKMajorVersion();
        assertEquals("17", project.apply("ecj-3.29.0_openjdk-11.0.19"));
    }

    @Test
    public void testEcjMatchesFirstTwoComponents() {
        ProjectToJDKMajorVersion project = new ProjectToJDKMajorVersion();
        assertEquals("9", project.apply("ecj-3.15.1_openjdk-11.0.19"));
    }

    @Test
    public void testEcjMatchesFirstThreeComponents() {
        ProjectToJDKMajorVersion project = new ProjectToJDKMajorVersion();
        assertEquals("8", project.apply("ecj-3.11.1.v20150902-1521_openjdk-11.0.19"));
        assertEquals("15", project.apply("ecj-3.24.0.1_openjdk-11.0.19"));
    }
}
