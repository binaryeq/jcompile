package nz.ac.wgtn.shadedetector.jcompile.oracles.comparators;

import com.google.common.base.Preconditions;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import static nz.ac.wgtn.shadedetector.jcompile.oracles.comparators.SemVer.compareSemVer;
import static nz.ac.wgtn.shadedetector.jcompile.oracles.comparators.SemVer.parseSemVer;

/**
 * Compares two jar files by looking at the -semver extension.
 * @author jens dietrich
 */
public class SemVerInJarFilenameComparator implements Comparator<Path> {

    public static final String SEMVERED_JAR = ".*-\\d+(\\.\\d+(\\.\\d+(\\.\\d+)?)?)?\\.jar";
    public static final Predicate<String> isSemVeredJar = Pattern.compile(SEMVERED_JAR).asPredicate();

    @Override
    public int compare(Path f1, Path f2) {

        String n1 = f1.getFileName().toString();
        String n2 = f2.getFileName().toString();
        Preconditions.checkArgument(isSemVeredJar.test(n1));
        Preconditions.checkArgument(isSemVeredJar.test(n2));

        n1 = n1.replace(".jar","");
        n2 = n2.replace(".jar","");

        String version1 = n1.substring(n1.lastIndexOf("-")+1);
        String version2 = n2.substring(n2.lastIndexOf("-")+1);

        int[] semver1 = parseSemVer(version1);
        int[] semver2 = parseSemVer(version2);

        return compareSemVer(semver1,semver2);

    }
}
