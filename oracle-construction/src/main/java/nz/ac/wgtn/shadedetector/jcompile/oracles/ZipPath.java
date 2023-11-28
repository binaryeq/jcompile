package nz.ac.wgtn.shadedetector.jcompile.oracles;

import java.nio.file.Path;

/**
 * A zip path consists of a path
 */
public record ZipPath (Path outerPath, Path innerPath, String generatedBy) {}