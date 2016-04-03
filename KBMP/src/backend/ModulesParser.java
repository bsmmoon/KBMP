package backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import common.Module;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Created by Joey on 28/3/16.
 */
public class ModulesParser {
    private static String ERROR_MESSAGE_MODULES_NOT_READABLE = "Path to module database is not readable";
    private static String ERROR_MESSAGE_WHITELIST_NOT_READABLE = "Path to whitelisted modules database is not readable";

    public static ArrayList<Module> getModulesFromPath(Path pathToFile) throws IOException {
        if (!Files.isReadable(pathToFile)) throw new IOException(ERROR_MESSAGE_MODULES_NOT_READABLE);

        NusmodsModule[] allRawModules = getRawModules(pathToFile);
        NusmodsModule[] relevantRawModules = filter(allRawModules, getRelevantPattern());
        ArrayList<Module> modules = parseModules(relevantRawModules);
        return modules;
    }

    private static NusmodsModule[] getRawModules(Path pathToFile) throws IOException {
        File moduleJson = pathToFile.toFile();
        ObjectMapper objectMapper = new ObjectMapper();
        NusmodsModule[] rawModules = objectMapper.readValue(moduleJson, NusmodsModule[].class);
        return rawModules;
    }

    private static NusmodsModule[] filter(NusmodsModule[] rawModules, Pattern pattern) {
        return new NusmodsModule[0];
    }

    private static Pattern getRelevantPattern() throws IOException {
        Path pathToWhitelistFile = Paths.get("data/Modules_Whitelist.txt");
        if (!Files.isReadable(pathToWhitelistFile)) throw new IOException(ERROR_MESSAGE_WHITELIST_NOT_READABLE);

        BufferedReader whitelist = Files.newBufferedReader(pathToWhitelistFile);


        return Pattern.compile("");
    }

    private static ArrayList<Module> parseModules(NusmodsModule[] rawModules) {
        ArrayList<Module> modules = new ArrayList<>(rawModules.length);
        return modules;
    }
}
