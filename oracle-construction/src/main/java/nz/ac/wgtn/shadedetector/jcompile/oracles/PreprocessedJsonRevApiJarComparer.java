package nz.ac.wgtn.shadedetector.jcompile.oracles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PreprocessedJsonRevApiJarComparer extends RevApiJarComparer {
    private static final RevApiResult EMPTY_RESULT = new RevApiResult(Severity.UNSPECIFIED, Severity.UNSPECIFIED, Severity.UNSPECIFIED);
    private List<Path> jars;        // Always contains 2 elements
    private Map<String, RevApiResult> revApiResults;

    public PreprocessedJsonRevApiJarComparer(Path p1, Path p2) {
        this.jars = List.of(p1, p2);
        revApiResults = loadResultsFromTsv();
    }

    public RevApiResult compareClassVersions(Path innerPath) {
        return revApiResults.getOrDefault(innerPath.toString(), EMPTY_RESULT);
    }

    private Path pathForJarPair() {
        return jars.get(0).getParent().resolve(jars.get(0).getFileName().toString().replaceFirst("\\.jar$", "__vs__" + jars.get(1).getFileName().toString().replaceFirst("\\.jar$", ".revapi.POTENTIALLY_BREAKING.tsv")));
    }

    // If there are multiple results for the same class, the max severity will be taken over each kind (source, binary, semantic).
    private Map<String, RevApiResult> loadResultsFromTsv() {
        Path tsvPath = pathForJarPair();
        Map<String, RevApiResult> map = new HashMap<>();
        try {
            for (String line : Files.readAllLines(tsvPath)) {
                String[] fields = line.split("\t");
                String classPath = fields[0].replaceAll("\\.", "/") + ".class";
                RevApiResult revApiResult = new RevApiResult(Severity.fromString(fields[1]), Severity.fromString(fields[2]), Severity.fromString(fields[3]));
                map.compute(classPath, (k, v) -> (v == null) ? revApiResult : combineRevApiResults(v, revApiResult));
            }

            return map;
        } catch (IOException e) {
            throw new RuntimeException("IO failure opening or reading from " + tsvPath + " file", e);
        }
    }

    private static RevApiResult combineRevApiResults(RevApiResult a, RevApiResult b) {
        return new RevApiResult(maxSeverity(a.source(), b.source()), maxSeverity(a.binary(), b.binary()), maxSeverity(a.semantic(), b.semantic()));
    }

    private static Severity maxSeverity(Severity a, Severity b) {
        return a.compareTo(b) > 0 ? a : b;
    }
}
