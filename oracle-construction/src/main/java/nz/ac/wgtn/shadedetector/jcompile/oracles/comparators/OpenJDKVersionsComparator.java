package nz.ac.wgtn.shadedetector.jcompile.oracles.comparators;

import com.google.common.base.Preconditions;

import java.util.Comparator;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static nz.ac.wgtn.shadedetector.jcompile.oracles.comparators.SemVer.compareSemVer;
import static nz.ac.wgtn.shadedetector.jcompile.oracles.comparators.SemVer.parseSemVer;

public class OpenJDKVersionsComparator implements Comparator<String> {

    public static final String OPENJDK_SEMVER = "openjdk-\\d+\\.\\d+\\.\\d+";
    public static final Predicate<String> isOpenJdkSemVer = Pattern.compile(OPENJDK_SEMVER).asPredicate();

    @Override
    public int compare(String o1, String o2) {
        Preconditions.checkArgument(isOpenJdkSemVer.test(o1));
        Preconditions.checkArgument(isOpenJdkSemVer.test(o2));

        String version1 = o1.substring(o1.lastIndexOf("-")+1);
        String version2 = o2.substring(o2.lastIndexOf("-")+1);

        int[] semver1 = parseSemVer(version1);
        int[] semver2 = parseSemVer(version2);

        return compareSemVer(semver1,semver2);
    }



}
