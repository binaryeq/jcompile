package nz.ac.wgtn.shadedetector.jcompile.oracles;

import org.apache.commons.lang3.tuple.Pair;

import java.io.PrintStream;
import java.util.stream.Stream;

public class AdjacentVersionSameArtifactAndCompilerClassOracleRow extends ClassOracleRow {
    private RevApiJarComparer.RevApiResult revApiResult;

    public AdjacentVersionSameArtifactAndCompilerClassOracleRow(Pair<ZipPath, ZipPath> zPaths, RevApiJarComparer.RevApiResult revApiResult) {
        super(zPaths);
        this.revApiResult = revApiResult;
    }

    @Override
    public void printRow(PrintStream out) {
        out.println(String.join("\t", Stream.of(
                        getLeft().outerPath(),
                        getRight().outerPath(),
                        getLeft().innerPath(),
                        getRight().innerPath(),
                        getLeft().compilerName(),
                        getLeft().compilerMajorVersion(),
                        getLeft().compilerMinorVersion(),
                        getLeft().compilerPatchVersion(),
                        getLeft().compilerExtraConfiguration(),
                        getLeft().projectName(),
                        getLeft().projectMajorVersion(),
                        getRight().projectMajorVersion(),
                        getLeft().projectMinorVersion(),
                        getRight().projectMinorVersion(),
                        getLeft().projectPatchVersion(),
                        getRight().projectPatchVersion(),
                        getLeft().generatedBy(),
                        getRight().generatedBy(),
                        getLeft().bytecodeFeatures().contains("JEP181"),
                        getRight().bytecodeFeatures().contains("JEP181"),
                        getLeft().bytecodeFeatures().contains("JEP280"),
                        getRight().bytecodeFeatures().contains("JEP280"),
                        getLeft().scope(),
                        getRight().scope(),
                        getLeft().allInnerPaths().size() - 1,
                        getRight().allInnerPaths().size() - 1,
                        revApiResult.source() != RevApiJarComparer.Severity.BREAKING,
                        revApiResult.binary() != RevApiJarComparer.Severity.BREAKING,
                        revApiResult.semantic() != RevApiJarComparer.Severity.BREAKING)
                .map(Utils::hyphenateEmpty).toList()));
    }
}
