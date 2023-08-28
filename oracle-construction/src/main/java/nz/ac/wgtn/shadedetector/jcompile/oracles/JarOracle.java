package nz.ac.wgtn.shadedetector.jcompile.oracles;

import org.apache.commons.lang3.tuple.Pair;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Computes a oracle for jar files.
 * @author jens dietrich
 */
public interface JarOracle {
    List<Pair<Path,Path>> build (Path jarFolder) throws IOException ;
}
