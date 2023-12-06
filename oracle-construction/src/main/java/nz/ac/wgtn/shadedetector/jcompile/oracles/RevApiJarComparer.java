package nz.ac.wgtn.shadedetector.jcompile.oracles;

import java.nio.file.Path;

public abstract class RevApiJarComparer {
    public abstract RevApiResult compareClassVersions(Path innerPath);

    public record RevApiResult(Severity source, Severity binary, Severity semantic) {}

    public enum Severity {
        UNSPECIFIED,
        EQUIVALENT,
        NON_BREAKING,
        POTENTIALLY_BREAKING,
        BREAKING;

        public static Severity fromString(String s) {
            if (s.equals("-")) {
                return UNSPECIFIED;
            } else {
                return valueOf(s);
            }
        }

        public static String toPrettyString(Severity sev) {
            if (sev == UNSPECIFIED) {
                return "-";
            } else {
                return sev.name();
            }
        }
    }
}
