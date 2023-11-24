package nz.ac.wgtn.shadedetector.jcompile.oracles.comparators;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static nz.ac.wgtn.shadedetector.jcompile.oracles.comparators.SemVer.compareSemVer;
import static nz.ac.wgtn.shadedetector.jcompile.oracles.comparators.SemVer.parseSemVer;

public class CompilerVersionsComparator implements Comparator<String> {

    public static final String LINEAGE_SEMVER = "^([^\\.]+)-(\\d+\\.\\d+\\.\\d+)";
    public static final Pattern LINEAGE_SEMVER_REGEX = Pattern.compile(LINEAGE_SEMVER);

    @Override
    public int compare(String o1, String o2) {
        String[] lineageAndSemVer1 = getLineageAndSemVer(o1);
        String[] lineageAndSemVer2 = getLineageAndSemVer(o2);

        int lineageDiff = lineageAndSemVer1[0].compareTo(lineageAndSemVer2[0]);
        if (lineageDiff != 0) {
            return lineageDiff;
        }

        int[] semver1 = parseSemVer(lineageAndSemVer1[1]);
        int[] semver2 = parseSemVer(lineageAndSemVer2[1]);

        return compareSemVer(semver1,semver2);
    }

    /**
     * @return array containing compiler lineage name as the first element, semver as the second element
     */
    public static String[] getLineageAndSemVer(String compilerName) {
        Matcher m = LINEAGE_SEMVER_REGEX.matcher(compilerName);
        if (m.find()) {
            return new String[] { m.group(1), m.group(2) };
        } else {
            throw new IllegalArgumentException("'" + compilerName + "' does not match " + LINEAGE_SEMVER);
        }
    }



}
