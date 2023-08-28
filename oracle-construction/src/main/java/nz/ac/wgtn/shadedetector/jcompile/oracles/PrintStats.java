package nz.ac.wgtn.shadedetector.jcompile.oracles;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Print some statistics.
 * @author jens dietrich
 */
public class PrintStats {

    public static void main (String[] args) throws IOException {

        File jarFolder = new File(args[0]);
        Preconditions.checkArgument(jarFolder.exists());
        Preconditions.checkArgument(jarFolder.isDirectory());

        int numberOfJars = Utils.collectJars(jarFolder).size();
        System.out.println("jars: " + numberOfJars);

        int numberOfCompilers = Utils.collectJarsByCompilerUsed(jarFolder).size();
        System.out.println("jars: " + numberOfCompilers);

        int numberOfArtifacts = Utils.collectJarsByArtifact(jarFolder).size();
        System.out.println("artifacts (versioned): " + numberOfArtifacts);

        int numberOfComponents = Utils.collectJarsByComponent(jarFolder).size();
        System.out.println("components (unversioned): " + numberOfComponents);

        int sameArtifactDifferentCompilerOracleSize  = new SameArtifactDifferentCompiler().build(jarFolder).size() ;
        System.out.println("oracle -- same artifact, different (adjacent) compiler pairs: " + sameArtifactDifferentCompilerOracleSize);

        int sameComponentAndCompilerAdjacentVersionOracleSize  = new AdjacentVersionSameArtifactAndCompiler().build(jarFolder).size() ;
        System.out.println("oracle -- same component and component, different (adjacent) version pairs: " + sameComponentAndCompilerAdjacentVersionOracleSize);
    }
}
