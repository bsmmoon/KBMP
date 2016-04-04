package backend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.Exam;
import common.Lesson;
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
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by Joey on 28/3/16.
 */
public class ModulesParser {
    private static String ERROR_MESSAGE_MODULES_NOT_READABLE = "Path to module database is not readable";
    private static String ERROR_MESSAGE_WHITELIST_NOT_READABLE = "Path to whitelisted modules database is not readable";
    private enum PatternTypes {ANY_ONE_MODULE};
    private static Hashtable<PatternTypes, Pattern> patterns = generatePatterns();
    private static Hashtable<String, Module> existingModules = null;

    public static void test(){
        Path modulesJson = Paths.get("data/AY1516_S1_modules.json");
        try {
            updateModulesFromPath(modulesJson, new Hashtable<>());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static ArrayList<Module> updateModulesFromPath(Path pathToFile, Hashtable<String, Module> existingModules) throws IOException {
        ModulesParser.existingModules = existingModules;
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
        Iterator<NusmodsModule> moduleIterator = rawModules.iterator();
        while (moduleIterator.hasNext()){
            String currentModuleCode = moduleIterator.next().ModuleCode.trim();
            // remove modules if they do not match any of the whitelisted module codes as defined in pattern.
            if (!pattern.matcher(currentModuleCode).matches()){
                moduleIterator.remove();
            }

            // if module has already been parsed in the previous semester, just add that it's present in this
            // semester and not parse the whole module again
            if (ModulesParser.existingModules.containsKey(currentModuleCode)){
                ModulesParser.existingModules.get(currentModuleCode).setSemesters(Module.Semester.BOTH);
                moduleIterator.remove();
            }
        }

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
            regex = prefix + "(\\d){4}([a-zA-Z]){0,2}";
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

        moduleBuilder.setWorkload(parseWorkload(rawModule));

        // if no prerequisites/corequisites/preclusions, leave as default empty arrayList.
        moduleBuilder.setPrerequisites(parsePrerequisites(rawModule));
        moduleBuilder.setCorequisites(parseCorequisites(rawModule));
        moduleBuilder.setPreclusions(parsePreclusions(rawModule));

        moduleBuilder.setTimetable(parseTimetable(rawModule));

        // if no exam, leave module.exam as null.
        moduleBuilder.setExam(parseExam(rawModule));

        return moduleBuilder.build();
    }

    private static Hashtable<Module.WorkloadTypes, Float> parseWorkload(NusmodsModule rawModule) {
        if (rawModule.Workload == null) {
            return new Hashtable<>();
        }

        String[] workloadTokens = rawModule.Workload.split("-");
        Hashtable<Module.WorkloadTypes, Float> workload = new Hashtable<>();
        workload.put(Module.WorkloadTypes.LECTURE, new Float(workloadTokens[0]));
        workload.put(Module.WorkloadTypes.TUTORIAL, new Float(workloadTokens[1]));
        workload.put(Module.WorkloadTypes.LABORATORY, new Float(workloadTokens[2]));
        workload.put(Module.WorkloadTypes.CONTINUOUS_ASSESSMENT, new Float(workloadTokens[3]));
        workload.put(Module.WorkloadTypes.PREPARATORY_WORK, new Float(workloadTokens[4]));
        return workload;
    }

    private static Exam parseExam(NusmodsModule rawModule) {
        if (rawModule.ExamDate == null) {
            return null;
        }

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

        return examBuilder.build();
    }

    private static ArrayList<String> parsePrerequisites(NusmodsModule rawModule) {
        ArrayList<String> prerequisites = new ArrayList<>();
        if (rawModule.Prerequisite == null) {
            return prerequisites;
        }

        String prerequisite = rawModule.Prerequisite.trim();

        Pattern anyOneModule = patterns.get(PatternTypes.ANY_ONE_MODULE);
        if (anyOneModule.matcher(prerequisite).matches()){
            prerequisites.add(prerequisite);
//            System.out.println("Matched: " + prerequisite);
//        } else {
//            System.out.println("Ignored: " + prerequisite);
        }

        return prerequisites;
    }

    private static ArrayList<String> parseCorequisites(NusmodsModule rawModule) {
        ArrayList<String> corequisites = new ArrayList<>();
        if (rawModule.Corequisite == null) {
            return corequisites;
        }

        String corequisite = rawModule.Corequisite.trim();

        Pattern anyOneModule = patterns.get(PatternTypes.ANY_ONE_MODULE);
        if (anyOneModule.matcher(corequisite).matches()){
            corequisites.add(corequisite);
//            System.out.println("Matched: " + corequisite);
//        } else {
//            System.out.println("Ignored: " + corequisite);
        }

        // extract "co-read ..." from prerequisites

        return corequisites;
    }

    private static ArrayList<String> parsePreclusions(NusmodsModule rawModule) {
        ArrayList<String> preclusions = new ArrayList<>();
        if (rawModule.Preclusion == null) {
            return preclusions;
        }

        String preclusion = rawModule.Preclusion.trim();

        Pattern anyOneModule = patterns.get(PatternTypes.ANY_ONE_MODULE);
        if (anyOneModule.matcher(preclusion).matches()){
            preclusions.add(preclusion);
//            System.out.println("Matched: " + preclusion);
//        } else {
//            System.out.println("Ignored: " + preclusion);
        }

        return preclusions;
    }

    private static Hashtable<PatternTypes, Pattern> generatePatterns() {
        Hashtable<PatternTypes, Pattern> patterns = new Hashtable<>();
        String regexAnyModule = "(([a-zA-Z]){0,2}(\\d){4}([a-zA-Z]){0,2})[a-zA-Z_\\s_\\p{Punct}]*";
        Pattern anyOne = Pattern.compile(regexAnyModule);
        patterns.put(PatternTypes.ANY_ONE_MODULE, anyOne);
        return patterns;
    }

    private static ArrayList<Lesson> parseTimetable(NusmodsModule rawModule) {
        return new ArrayList<>();
    }
}
