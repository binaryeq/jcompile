package nz.ac.wgtn.shadedetector.jcompile.oracles;

import java.nio.file.Path;
import java.util.List;

public class PreprocessedJsonRevApiJarComparer extends RevApiJarComparer {
    private List<Path> jars;        // Always contains 2 elements

    public PreprocessedJsonRevApiJarComparer(Path p1, Path p2) {
        this.jars = List.of(p1, p2);
    }

    public RevApiResult compareClassVersions(Path innerPath) {
        //TODO: Fix dummy values
        return new RevApiResult(
                jars.get(0).toString().matches(".*[02468]\\.jar") ? Severity.BREAKING : Severity.NON_BREAKING,
                innerPath.toString().matches(".*[a-m]\\.class") ? Severity.POTENTIALLY_BREAKING : Severity.NON_BREAKING,
                Severity.EQUIVALENT);
    }
}
