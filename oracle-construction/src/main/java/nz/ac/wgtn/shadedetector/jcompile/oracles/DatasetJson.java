package nz.ac.wgtn.shadedetector.jcompile.oracles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * This is a hack that guesses the location of dataset.json and tries to load it on demand.
 * The Right Way would be have a ctor that is passed the filename location (taken from defaults or the command line),
 * but then the object would need to be threaded through lots of code.
 *
 * Necessitated by the fact that NEQ1 needs to be able to find all versions of a given project, but some projects
 * produce jars with names that vary unpredictably across versions, so sadly we can't rely on just parsing filenames.
 */
public class DatasetJson {
    private static Map<String, String> dataset;

    public static String getProjectNameForJarName(String jarName) {
        return getDataset().get(jarName);
    }

    private static synchronized Map<String, String> getDataset() {
        if (dataset == null) {
            dataset = loadDataset();
        }

        return dataset;
    }

    private static Map<String, String> loadDataset() {
        Map<String, String> result = new HashMap<>();

        JsonArray array = new JsonParser().parse(getDatasetJsonFileAsString()).getAsJsonArray();
        for (JsonElement project : array.asList()) {
            String projectName = project.getAsJsonObject().getAsJsonPrimitive("name").getAsString();
            String jarName = project.getAsJsonObject().getAsJsonPrimitive("jar").getAsString();
            result.put(jarName, projectName);
        }

        return result;
    }

    // Just tries a few parent directories...
    private static String getDatasetJsonFileAsString() {
        try {
            String path = "dataset.json";
            for (int i = 0; i < 5; ++i) {
                if (Files.exists(Path.of(path))) {
                    System.err.println("Found dataset.json at " + path);        //DEBUG
                    return Files.readString(Path.of(path));
                }

                path = "../" + path;
            }

            throw new RuntimeException("Could not find dataset.json!");
        } catch (IOException e) {
            throw new RuntimeException("Could not load dataset.json!", e);
        }
    }

    //DEBUG
    public static void main(String[] args) {
        System.out.println("Hello");
        System.out.println(DatasetJson.getProjectNameForJarName("bcel-6.7.0.jar"));
        System.out.println("The end.");
    }
}
