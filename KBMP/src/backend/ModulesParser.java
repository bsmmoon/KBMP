package backend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.Exam;
import common.Lesson;
import common.Module;
import javafx.util.Pair;

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
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

/**
 * Created by Joey on 28/3/16.
 */
public class ModulesParser {
    private static String ERROR_MESSAGE_MODULES_NOT_READABLE = "Path to module database is not readable";
    private static String ERROR_MESSAGE_WHITELIST_NOT_READABLE = "Path to whitelisted modules database is not readable";
    private enum PatternTypes {ANY_ONE_MODULE_GREEDY}
    private static Hashtable<PatternTypes, Pattern> patterns = generatePatterns();
    private static Hashtable<String, Module> existingModules = null;
    private static Module.Semester currentSemester;
    public static String AND_WORD = " and ";
    public static String AND = "and";
    public static String OR_WORD = " or ";
    public static String OR = "or";
    public static String OPEN_BRACKET = "(";
    public static String CLOSE_BRACKET = ")";
    private enum Operator {AND, OR}

    public static Hashtable<String, Module> updateModulesFromPath(Path pathToFile, Hashtable<String, Module> existingModules,
                                                                  Module.Semester currentSemester) throws IOException {
        ModulesParser.existingModules = existingModules;
        ModulesParser.currentSemester = currentSemester;

        if (!Files.isReadable(pathToFile)) throw new IOException(ERROR_MESSAGE_MODULES_NOT_READABLE);
        ArrayList<NusmodsModule> allRawModules = getRawModules(pathToFile);
        ArrayList<NusmodsModule> relevantRawModules = filterByModuleCode(allRawModules, getRelevantPatternsFromWhitelist());
        System.out.println(relevantRawModules.size() + " modules selected.");
        Hashtable<String, Module> modules = parseModules(relevantRawModules);
        modules.putAll(existingModules);

        // reset instance-specific data.
        ModulesParser.existingModules = null;
        ModulesParser.currentSemester = null;

        return modules;
    }

    private static ArrayList<NusmodsModule> getRawModules(Path pathToFile) throws IOException {
        File moduleJson = pathToFile.toFile();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ArrayList<NusmodsModule> rawModules = objectMapper.readValue(moduleJson, new
                TypeReference<ArrayList<NusmodsModule>>(){});
        return rawModules;
    }

    private static ArrayList<NusmodsModule> filterByModuleCode(ArrayList<NusmodsModule> rawModules, Pattern pattern) {
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

    private static Pattern getRelevantPatternsFromWhitelist() throws IOException {
        Path pathToWhitelistFile = Paths.get("data/Modules_Whitelist.txt");
        if (!Files.isReadable(pathToWhitelistFile)) throw new IOException(ERROR_MESSAGE_WHITELIST_NOT_READABLE);

        ArrayList<String> patterns = new ArrayList<>();
        String commentPrefix = "//";
        BufferedReader whitelist = Files.newBufferedReader(pathToWhitelistFile);
        String currentLine = whitelist.readLine();
        while (currentLine != null) {
            currentLine = currentLine.trim();
            if (!(currentLine.startsWith(commentPrefix) || currentLine.isEmpty())) {
                patterns.add(extractRegexFromWhitelist(currentLine));
            }
            currentLine = whitelist.readLine();
        }
        whitelist.close();

        String combinedPattern = "(" + String.join(")|(", patterns) + ")";
        return Pattern.compile(combinedPattern);
    }

    private static String extractRegexFromWhitelist(String input) {
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

    private static Hashtable<String, Module> parseModules(ArrayList<NusmodsModule> rawModules) {
        Hashtable<String, Module> modules = new Hashtable<>();

        Iterator<NusmodsModule> moduleIterator = rawModules.iterator();
        while (moduleIterator.hasNext()){
            NusmodsModule currentRawModule = moduleIterator.next();
            try {
                Module currentModule = parseModule(currentRawModule);
                modules.put(currentModule.getCode(), currentModule);
            } catch (NullPointerException e) {
                System.out.println("Parsing failed for \n" + currentRawModule);
            }
        }

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

        Module newModule = moduleBuilder.build();
        newModule.setSemesters(ModulesParser.currentSemester);
        return newModule;
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

        // if venue or open book information is not available, leave as null.
        examBuilder.setVenue(rawModule.ExamVenue).setOpenBook(rawModule.ExamOpenBook);

        // format Nusmods' date, necessary for parsing
        String rawDate = rawModule.ExamDate;
        String[] dateTokens = rawDate.split("\\+", 2);
        dateTokens[1] = dateTokens[1].substring(0, 2) + ":" + dateTokens[1].substring(2, 4);
        rawDate = dateTokens[0] + ":00+" + dateTokens[1];
        examBuilder.setDate(OffsetDateTime.parse(rawDate));

        // format Nusmods' duration, necessary for parsing
        // if duration is not available, leave as null.
        String rawDuration = rawModule.ExamDuration;
        Duration duration = null;
        if (rawDuration != null) {
            rawDuration = rawDuration.substring(0, 1) + "T" + rawDuration.substring(1);
            duration = Duration.parse(rawDuration);
        }
        examBuilder.setDuration(duration);


        return examBuilder.build();
    }

    private static ArrayList<String> parsePrerequisites(NusmodsModule rawModule) {
        ArrayList<String> prerequisites = new ArrayList<>();
        if (rawModule.Prerequisite == null) {
            return prerequisites;
        }

        String prerequisite = rawModule.Prerequisite.trim();

        Pattern anyOneModule = patterns.get(PatternTypes.ANY_ONE_MODULE_GREEDY);
        if (anyOneModule.matcher(prerequisite).matches()) {
            // matches from the start
            ArrayList<String> codes = extractModuleCodesFromOneModuleCode(prerequisite);
            prerequisites.add(codes.get(0));
//            StringBuilder matchesBuilder = new StringBuilder();
//            for (String code : codes) {
//                matchesBuilder.append(" " + code);
//            }
//            System.out.println("Matched: " + matchesBuilder.toString());
//        } else {
//            System.out.println("Ignored: " + prerequisite);
        } else if (prerequisite.contains("(") && !prerequisite.contains("[")) {
            // "(CS1020 or its equivalent) and (MA1101R or MA1506)"
            System.out.println("Original: " + prerequisite);
            Pair<ArrayList<Operator>, ArrayList<String>> topLevel = extractTopLevel(prerequisite);

            ArrayList<Operator> secondLevelOperators = new ArrayList<>();
            ArrayList<ArrayList<String>> allModuleCodes = new ArrayList<>();
            for (String token : topLevel.getValue()) {
                Pair<Operator, ArrayList<String>> secondLevel = extractSecondLevel(token);
                if (secondLevel.getKey() != null && !secondLevel.getValue().isEmpty()) {
                    secondLevelOperators.add(secondLevel.getKey());
                    allModuleCodes.add(secondLevel.getValue());
                }
            }

            String collatedPrerequisite = generatePrerequisiteString(topLevel.getKey(), secondLevelOperators, allModuleCodes);
            System.out.println("Processed: " + collatedPrerequisite);
        }

        return prerequisites;
    }

    private static String generatePrerequisiteString(ArrayList<Operator> topLevel, ArrayList<Operator>
            secondLevelOperators, ArrayList<ArrayList<String>> allModuleCodes) {
        StringBuilder prereqBuilder = new StringBuilder();

        Iterator<Operator> topLevelOperatorIter = topLevel.iterator();
        Iterator<Operator> secondLevelOperatorIter = secondLevelOperators.iterator();
        for (int i = 0; i < allModuleCodes.size(); i++) {
            ArrayList<String> modules = allModuleCodes.get(i);
            Operator currentSecondLevelOperator = secondLevelOperatorIter.next();
            String currentSecondLevelOperatorString = "";
            if (currentSecondLevelOperator == Operator.AND) {
                currentSecondLevelOperatorString = AND_WORD;
            } else if (currentSecondLevelOperator == Operator.OR) {
                currentSecondLevelOperatorString = OR_WORD;
            }

            if (modules.size() > 1 && allModuleCodes.size() > 1) {
                prereqBuilder.append(OPEN_BRACKET);
            }
            for (int j = 0; j < modules.size() - 1; j++) {
                prereqBuilder.append(modules.get(j));
                prereqBuilder.append(currentSecondLevelOperatorString);
            }
            prereqBuilder.append(modules.get(modules.size()-1));
            if (modules.size() > 1 && allModuleCodes.size() > 1) {
                prereqBuilder.append(CLOSE_BRACKET);
            }

            try {
                Operator currentTopLevelOperator = topLevelOperatorIter.next();
                String currentTopLevelOperatorString = "";
                if (currentTopLevelOperator == Operator.AND) {
                    currentTopLevelOperatorString = AND_WORD;
                } else if (currentTopLevelOperator == Operator.OR) {
                    currentTopLevelOperatorString = OR_WORD;
                }
                prereqBuilder.append(currentTopLevelOperatorString);
            } catch (NoSuchElementException e) {
                // last set of modules won't have a corresponding operator to go after
                // do nothing
            }
        }

        return prereqBuilder.toString();
    }

    // tokenize according to brackets then recognize operator between brackets
    private static Pair<ArrayList<Operator>, ArrayList<String>> extractTopLevel(String rawInput) {
        String[] rawTokens = rawInput.split("\\(");
        ArrayList<String> tokens = new ArrayList<>(rawTokens.length);
        ArrayList<Operator> topLevelOperators = new ArrayList<>();
        for (String token : rawTokens) {
            token = token.trim();
            if (token.isEmpty()) {
                // do nothing
            } else if (token.endsWith(ModulesParser.AND)) {
                // ModulesParser.AND_WORD is lowercase "and". Prerequisites don't contain camelCase or uppercase "and"s.
                topLevelOperators.add(Operator.AND);
                tokens.add(token.substring(0, token.length() - ModulesParser.AND.length()));
            } else if (token.endsWith(ModulesParser.OR)) {
                topLevelOperators.add(Operator.OR);
                tokens.add(token.substring(0, token.length() - ModulesParser.OR.length()));
            } else {
                tokens.add(token);
            }
        }

        return new Pair<>(topLevelOperators, tokens);
    }

    // tokens contain at least one operator, and the operators within a token must be of the same type.
    private static Pair<Operator, ArrayList<String>> extractSecondLevel(String token) {
        Pattern anyOneModule = patterns.get(PatternTypes.ANY_ONE_MODULE_GREEDY);
        ArrayList<String> moduleCodes = new ArrayList<>();
        Operator operator = null;

        // split by "or" first because some module codes include "and"
        // => token might contain "and" but actually the operator is only "or"
        if (token.contains(ModulesParser.OR_WORD)) {
            String[] rawModuleTokens = token.split(ModulesParser.OR_WORD);
            operator = Operator.OR;
            for (String rawModuleToken : rawModuleTokens) {
                if (anyOneModule.matcher(rawModuleToken).matches()){
                    ArrayList<String> codes = extractModuleCodesFromOneModuleCode(rawModuleToken);
                    moduleCodes.addAll(codes);
                } else {
                    System.out.println("DOESNT MATCH: " + rawModuleToken);
                }
            }
        } else {
            String[] rawModuleTokens = token.split(ModulesParser.AND_WORD);
            int count = 0;
            for (String rawModuleToken : rawModuleTokens) {
                if (anyOneModule.matcher(rawModuleToken).matches()){
                    count++;
                    ArrayList<String> codes = extractModuleCodesFromOneModuleCode(rawModuleToken);
                    moduleCodes.addAll(codes);
                } else {
                    System.out.println("DOESNT MATCH: " + rawModuleToken);
                }
            }
            if (count > 0) { // "and" isn't present only within a module's title
                operator = Operator.AND;
            }
        }
        return new Pair<>(operator, moduleCodes);
    }

    private static ArrayList<String> extractModuleCodesFromOneModuleCode(String code) {
        String trimmedCode = code.split(" ")[0];
        ArrayList<String> codes = new ArrayList<>();
        if (trimmedCode.contains("/")) {
            String[] tokens = trimmedCode.split("/");
            codes.add(tokens[0]);
            for (int i = 1; i < tokens.length; i++) {
                codes.add(tokens[0] + tokens[i]);
            }
        } else {
            codes.add(trimmedCode);
        }
        return codes;
    }

    private static ArrayList<String> parseCorequisites(NusmodsModule rawModule) {
        ArrayList<String> corequisites = new ArrayList<>();
        if (rawModule.Corequisite == null) {
            return corequisites;
        }

        String corequisite = rawModule.Corequisite.trim();

        Pattern anyOneModule = patterns.get(PatternTypes.ANY_ONE_MODULE_GREEDY);
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

        Pattern anyOneModule = patterns.get(PatternTypes.ANY_ONE_MODULE_GREEDY);
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

        //eg CS1010, CS1231R, CS2103\T
        String regexAnyModule = "(([a-zA-Z]){0,2}(\\d){4}([a-zA-Z]){0,2})[a-zA-Z_\\s_\\p{Punct}]*";
        Pattern anyOne = Pattern.compile(regexAnyModule);
        patterns.put(PatternTypes.ANY_ONE_MODULE_GREEDY, anyOne);

        return patterns;
    }

    private static ArrayList<Lesson> parseTimetable(NusmodsModule rawModule) {
        return new ArrayList<>();
    }
}
