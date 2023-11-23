package nz.ac.wgtn.shadedetector.jcompile.oracles;

import nz.ac.wgtn.shadedetector.jcompile.oracles.comparators.OpenJDKVersionsComparator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class OpenJDKVersionsComparatorTest {

    @Test
    public void testBadInput1() {
        assertThrows(IllegalArgumentException.class, () -> new OpenJDKVersionsComparator().compare("openjdk-1.2.3","openjdk-x.y.z"));
    }

    @Test
    public void testBadInput2() {
        assertThrows(IllegalArgumentException.class, () -> new OpenJDKVersionsComparator().compare("openjdk-x.y.z","openjdk-1.2.3"));
    }

    @Test
    public void testBadInput3() {
        assertThrows(IllegalArgumentException.class, () -> new OpenJDKVersionsComparator().compare("otherjdk-1.2.3","openjdk-1.2.3"));
    }


    @Test
    public void testEqual1() {
        assertEquals(0,new OpenJDKVersionsComparator().compare("openjdk-1.2.3","openjdk-1.2.3"));
    }

    @Test
    public void testEqual2() {
        assertEquals(0,new OpenJDKVersionsComparator().compare("openjdk-12.233.3445","openjdk-12.233.3445"));
    }

    @Test
    public void testMicro1() {
        assertTrue(0 > new OpenJDKVersionsComparator().compare("openjdk-1.2.2","openjdk-1.2.3"));
    }

    @Test
    public void testMicro2() {
        assertTrue(0 < new OpenJDKVersionsComparator().compare("openjdk-1.2.4","openjdk-1.2.3"));
    }

    @Test
    public void testMicro3() {
        assertTrue(0 > new OpenJDKVersionsComparator().compare("openjdk-1.2.2","openjdk-1.2.23"));
    }

    @Test
    public void testMinor1() {
        assertTrue(0 > new OpenJDKVersionsComparator().compare("openjdk-1.1.3","openjdk-1.2.3"));
    }

    @Test
    public void testMinor2() {
        assertTrue(0 < new OpenJDKVersionsComparator().compare("openjdk-1.4.3","openjdk-1.2.3"));
    }

    @Test
    public void testMinor3() {
        assertTrue(0 > new OpenJDKVersionsComparator().compare("openjdk-1.2.3","openjdk-1.23.3"));
    }

    @Test
    public void testMajor1() {
        assertTrue(0 > new OpenJDKVersionsComparator().compare("openjdk-1.2.3","openjdk-2.2.3"));
    }

    @Test
    public void testMajor2() {
        assertTrue(0 < new OpenJDKVersionsComparator().compare("openjdk-3.2.3","openjdk-2.2.3"));
    }

    @Test
    public void testMajor3() {
        assertTrue(0 > new OpenJDKVersionsComparator().compare("openjdk-2.2.3","openjdk-23.2.3"));
    }

}
