package backend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.Exam;
import common.Module;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by Joey on 28/3/16.
 */
public class ModulesParser {
    private static String ERROR_MESSAGE_MODULES_NOT_READABLE = "Path to module database is not readable";
    private static String ERROR_MESSAGE_WHITELIST_NOT_READABLE = "Path to whitelisted modules database is not readable";

    public static void test(){
        Path modulesJson = Paths.get("data/AY1516_S1_modules.json");
        try {
            getModulesFromPath(modulesJson);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static ArrayList<Module> getModulesFromPath(Path pathToFile) throws IOException {
        if (!Files.isReadable(pathToFile)) throw new IOException(ERROR_MESSAGE_MODULES_NOT_READABLE);

        ArrayList<NusmodsModule> allRawModules = getRawModules(pathToFile);
        ArrayList<NusmodsModule> relevantRawModules = filter(allRawModules, getRelevantPattern());
        //ArrayList<Module> modules = parseModules(relevantRawModules);

        return new ArrayList<Module>();
    }

    private static ArrayList<NusmodsModule> getRawModules(Path pathToFile) throws IOException {
        File moduleJson = pathToFile.toFile();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ArrayList<NusmodsModule> rawModules = objectMapper.readValue(moduleJson, new
                TypeReference<ArrayList<NusmodsModule>>(){});
        return rawModules;
    }

    private static ArrayList<NusmodsModule> filter(ArrayList<NusmodsModule> rawModules, Pattern pattern) {
        // remove modules if they do not match any of the whitelisted module codes as defined in pattern.
        rawModules.removeIf(module -> !pattern.matcher(module.ModuleCode).matches());
        return rawModules;
    }

    private static Pattern getRelevantPattern() throws IOException {
        Path pathToWhitelistFile = Paths.get("data/Modules_Whitelist.txt");
        if (!Files.isReadable(pathToWhitelistFile)) throw new IOException(ERROR_MESSAGE_WHITELIST_NOT_READABLE);

        ArrayList<String> patterns = new ArrayList<>();
        String commentPrefix = "//";
        BufferedReader whitelist = Files.newBufferedReader(pathToWhitelistFile);
        String currentLine = whitelist.readLine();
        while (currentLine != null) {
            currentLine = currentLine.trim();
            if (!(currentLine.startsWith(commentPrefix) || currentLine.isEmpty())) {
                patterns.add(extractRegex(currentLine));
            }
            currentLine = whitelist.readLine();
        }
        whitelist.close();

        String combinedPattern = "(" + String.join(")|(", patterns) + ")";
        return Pattern.compile(combinedPattern);
    }

    private static String extractRegex(String input) {
        String[] tokens = input.split(":", 2);
        String prefix = "(" + tokens[0].trim() + ")";
        String rest = tokens[1].trim();

        String regex = "";
        String asterisk = "*";
        if (rest.contentEquals(asterisk)){
            // match prefix exactly once, followed by 4 digits, and 0-2 alphabets/numbers
            regex = prefix + "(\\d){4}(\\w){0,2}";
        } else {
            String[] restOfTokens = rest.split(",");
            ArrayList<String> regexTokens = new ArrayList<>();
            for (String currentToken : restOfTokens){
                String token = currentToken.trim();
                int indexOfAsterisk = token.indexOf(asterisk);
                if (indexOfAsterisk == -1){
                    regexTokens.add(token); // exact match (after prefix)
                } else {
                    String fixed = token.substring(0, indexOfAsterisk);
                    // match whatever's before the * followed by any alphabet/number up to a total of 6 characters
                    regexTokens.add(fixed + "(\\w){0," + (6 - indexOfAsterisk) + "}");
                }
            }

            if (regexTokens.size() > 1) {
                regex = prefix + "(" + String.join("|", regexTokens) + ")";
            } else {
                regex = prefix + regexTokens.get(0);
            }
        }

        return regex;
    }

    private static ArrayList<Module> parseModules(ArrayList<NusmodsModule> rawModules) {
        ArrayList<Module> modules = rawModules.stream().map(module -> parseModule(module)).collect(Collectors
                .toCollection(ArrayList::new));
        return modules;
    }

    private static Module parseModule(NusmodsModule rawModule){
        Module.Builder moduleBuilder = Module.builder();
        moduleBuilder.setCode(rawModule.ModuleCode.trim()).setName(rawModule.ModuleTitle.trim()).setCredits(rawModule
                .ModuleCredit).setDepartment(rawModule.Department.trim());

        // workload
        // if no exam, leave module.workload as null
        if (rawModule.Workload != null) {
            String[] workloadTokens = rawModule.Workload.split("-");
            Hashtable<Module.WorkloadTypes, Float> workload = new Hashtable<>();
            workload.put(Module.WorkloadTypes.LECTURE, new Float(workloadTokens[0]));
            workload.put(Module.WorkloadTypes.TUTORIAL, new Float(workloadTokens[1]));
            workload.put(Module.WorkloadTypes.LABORATORY, new Float(workloadTokens[2]));
            workload.put(Module.WorkloadTypes.CONTINUOUS_ASSESSMENT, new Float(workloadTokens[3]));
            workload.put(Module.WorkloadTypes.PREPARATORY_WORK, new Float(workloadTokens[4]));
            moduleBuilder.setWorkload(workload);
        }

        // prerequisites
        // corequisites
        // preclusions
        // timetable

        // exam
        // if no exam, leave module.exam as null.
        if (rawModule.ExamDate != null) {
            Exam.Builder examBuilder = Exam.builder();

            // format Nusmods' date, necessary for parsing
            String rawDate = rawModule.ExamDate;
            String[] dateTokens = rawDate.split("\\+", 2);
            dateTokens[1] = dateTokens[1].substring(0, 2) + ":" + dateTokens[1].substring(2, 4);
            rawDate = dateTokens[0] + ":00+" + dateTokens[1];

            // format Nusmods' duration, necessary for parsing
            String rawDuration = rawModule.ExamDuration;
            rawDuration =  rawDuration.substring(0, 1) + "T" + rawDuration.substring(1);
            examBuilder.setDuration(Duration.parse(rawDuration));

            examBuilder.setDate(OffsetDateTime.parse(rawDate));
            examBuilder.setVenue(rawModule.ExamVenue).setOpenBook(rawModule.ExamOpenBook);

            moduleBuilder.setExam(examBuilder.build());
        }

        return moduleBuilder.build();
    }
}
