package nz.ac.wgtn.shadedetector.jcompile.oracles;

import org.apache.commons.lang3.tuple.Pair;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Computes a oracle for jar files.
 * @author jens dietrich
 */
public interface JarOracle {
    List<Pair<File,File>> build (File jarFolder) throws IOException ;
}
