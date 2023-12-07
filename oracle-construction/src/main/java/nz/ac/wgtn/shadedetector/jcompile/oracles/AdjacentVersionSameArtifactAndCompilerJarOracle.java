package nz.ac.wgtn.shadedetector.jcompile.oracles;

import nz.ac.wgtn.shadedetector.jcompile.oracles.comparators.SemVerInJarFilenameComparator;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static nz.ac.wgtn.shadedetector.jcompile.oracles.Utils.sorted;

/**
 * Construct a negative oracle for jars, i.e. sets of jars that originate from different but similar source code (adjacent versions),
 * generated by the same compiler.
 * @author jens dietrich
 */
public class AdjacentVersionSameArtifactAndCompilerJarOracle implements JarOracle {

    @Override
    public List<Pair<Path, Path>> build(Path jarFolder) throws IOException {

        TreeMap<String,Set<Path>> jarsByBuildAndComponent = Utils.collectJarsByComponentAndBuildAndJarType(jarFolder);
        List<Pair<Path, Path>> oracle = new ArrayList<>();
        SemVerInJarFilenameComparator comparator = new SemVerInJarFilenameComparator();

        for (String artifact:jarsByBuildAndComponent.keySet()) {
            List<Path> differentVersion = jarsByBuildAndComponent.get(artifact).stream()
                .sorted(comparator)
                .collect(Collectors.toList());

            for (int i=1;i<differentVersion.size();i++) {
                oracle.add(Pair.of(differentVersion.get(i-1),differentVersion.get(i)));
            }
        }

        return oracle;
    }

    // for testing TODO: remove
    public static void main (String[] args) throws IOException {
        Path jarFolder = Path.of(args[0]);
        List<Pair<Path, Path>> oracle = new AdjacentVersionSameArtifactAndCompilerJarOracle().build(jarFolder) ;
        for (Pair<Path, Path> pair:oracle) {
            System.out.println(pair.getLeft().toFile() + "\t" + pair.getRight().toFile());
        }

    }

}
