package nz.ac.wgtn.shadedetector.jcompile.oracles.comparators;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

public class ProjectToJDKMajorVersion implements UnaryOperator<String> {
    public String apply(String compilerName) {
        compilerName = compilerName.replaceFirst("-nodebug-", "-");

        if (compilerName.startsWith("openjdk-") && compilerName.contains(".")) {
            return compilerName.substring(0, compilerName.indexOf('.')).substring(8);
        } else if (compilerName.startsWith("ecj-") && compilerName.contains("_")) {
            String ecjVersion = compilerName.substring(0, compilerName.indexOf('_')).substring(4);

            // Keep stripping period-delimited components until we find a match
            Map<String, String> jdkMajorVersionMap = getJdkMajorVersionMap();
            while (!jdkMajorVersionMap.containsKey(ecjVersion)) {
                int period = ecjVersion.lastIndexOf('.');
                if (period == -1) {
                    throw new RuntimeException("Could not find JDK major version for ECJ compiler name '" + compilerName + "' (final attempt: '" + ecjVersion + "')");
                }
                ecjVersion = ecjVersion.substring(0, period);
            }

            return jdkMajorVersionMap.get(ecjVersion);
        } else if (compilerName.startsWith("oraclejdk-") && compilerName.contains(".")) {
            return compilerName.substring(0, compilerName.indexOf('.')).substring(10);
        } else {
            throw new IllegalArgumentException("Could not identify compiler lineage for '" + compilerName + "'");
        }
    }

    private static synchronized Map<String, String> getJdkMajorVersionMap() {
        if (cachedJdkMajorVersionMap == null) {
            cachedJdkMajorVersionMap = loadJdkMajorVersionMap();
        }

        return cachedJdkMajorVersionMap;
    }

    private static Map<String, String> loadJdkMajorVersionMap() {
        try (InputStream is = ProjectToJDKMajorVersion.class.getResourceAsStream("/ecj-jdk-versions.tsv");
             Reader reader = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(reader)) {
            Map<String, String> map = new HashMap<>();
            for (String line : br.lines().toList()) {
                if (!line.startsWith("eclipse_release\t")) {              // Skip header line
                    String[] fields = line.trim().split("\t");      // Eclipse release, ECJ Compiler version, Max supported major JDK version
                    map.put(fields[1], fields[2]);
                }
            }

            return map;
        } catch (IOException e) {
            throw new RuntimeException("IO failure opening or reading from ECJ JDK version resource file", e);
        }
    }

    private static Map<String, String> cachedJdkMajorVersionMap;
}
