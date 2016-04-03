package backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import common.Module;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Created by Joey on 28/3/16.
 */
public class ModulesParser {
    public static ArrayList<Module> getModulesFromFile(Path pathToFile) throws IOException {
        if (!Files.isReadable(pathToFile)) throw new IOException();

        NusmodsModule[] allRawModules = getRawModules(pathToFile);
        NusmodsModule[] relevantRawModules = filter(allRawModules, getRelevantPattern());
        ArrayList<Module> modules = parseModules(relevantRawModules);
        return modules;
    }

    private static NusmodsModule[] getRawModules(Path pathToFile) throws IOException{
        File moduleJson = pathToFile.toFile();
        ObjectMapper objectMapper = new ObjectMapper();
        NusmodsModule[] rawModules = objectMapper.readValue(moduleJson, NusmodsModule[].class);
        return rawModules;
    }

    private static NusmodsModule[] filter(NusmodsModule[] rawModules, Pattern pattern){
        return new NusmodsModule[0];
    }

    private static Pattern getRelevantPattern(){
        return Pattern.compile("");
    }

    private static ArrayList<Module> parseModules(NusmodsModule[] rawModules) {
        ArrayList<Module> modules = new ArrayList<>(rawModules.length);
        return modules;
    }
}
