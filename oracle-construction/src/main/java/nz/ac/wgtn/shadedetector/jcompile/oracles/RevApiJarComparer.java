package nz.ac.wgtn.shadedetector.jcompile.oracles;

import java.nio.file.Path;

public abstract class RevApiJarComparer {
    public abstract RevApiResult compareClassVersions(Path innerPath);

    public record RevApiResult(Severity source, Severity binary, Severity semantic) {}

    public enum Severity {
        EQUIVALENT,
        NON_BREAKING,
        POTENTIALLY_BREAKING,
        BREAKING
    }
}
