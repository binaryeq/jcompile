package nz.ac.wgtn.shadedetector.jcompile.oracles;

import org.apache.commons.lang3.tuple.Pair;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

/**
 * Computes an oracle for classes in jar files.
 * @author jens dietrich
 */
public interface ClassOracle {
    List<Pair<ZipPath,ZipPath>> build (Path jarFolder) throws IOException, URISyntaxException;
}
