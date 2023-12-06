package nz.ac.wgtn.shadedetector.jcompile.oracles;

import org.apache.commons.lang3.tuple.Pair;

import java.io.PrintStream;

/**
 * A class to represent a comparison between a pair of classes.
 * Contains {@link ZipPath}s for each class, as well as any pair-specific information.
 */
public abstract class ClassOracleRow {
    private final Pair<ZipPath, ZipPath> zPaths;

    public ClassOracleRow(Pair<ZipPath, ZipPath> zPaths) {
        this.zPaths = zPaths;
    }

    public ZipPath getLeft() {
        return zPaths.getLeft();
    }

    public ZipPath getRight() {
        return zPaths.getRight();
    }

    public abstract void printRow(PrintStream out);
}
