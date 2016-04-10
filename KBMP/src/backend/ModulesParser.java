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
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by Joey on 28/3/16.
 */
public class ModulesParser {
    private static String ERROR_MESSAGE_MODULES_NOT_READABLE = "Path to module database is not readable";
    private static String ERROR_MESSAGE_WHITELIST_NOT_READABLE = "Path to whitelisted modules database is not readable";
    private enum PatternTypes {ANY_ONE_MODULE_GREEDY, ANY_ONE_MODULE_EXACT, ANY_TWO_MODULES}
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
//        System.out.println(relevantRawModules.size() + " modules selected.");
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

    private static ArrayList<NusmodsModule> filterByModuleCode(ArrayList<NusmodsModule> rawModules, Pattern whitelist) {
        Iterator<NusmodsModule> moduleIterator = rawModules.iterator();
        ArrayList<String> blacklist = new ArrayList<>(Arrays.asList("IS4010"));
        while (moduleIterator.hasNext()){
            String currentModuleCode = moduleIterator.next().ModuleCode.trim();

            // remove modules if they are blacklisted
            if (blacklist.contains(currentModuleCode)) {
                moduleIterator.remove();
            }

            // remove modules if they do not match any of the whitelisted module codes as defined in pattern.
            if (!whitelist.matcher(currentModuleCode).matches()){
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

        // if no prerequisites/corequisites/preclusions, empty string.
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

    private static String parsePrerequisites(NusmodsModule rawModule) {
        String prerequisites = "";
        if (rawModule.Prerequisite == null) {
            return prerequisites;
        }

        String rawPrerequisite = rawModule.Prerequisite;
        rawPrerequisite = rawPrerequisite.replace("For SoC students only.", "");
        if (rawPrerequisite.contains("Other students:")) {
            rawPrerequisite = rawPrerequisite.split("Other students:")[0];
            String[] tokens = rawPrerequisite.split(":");
            rawPrerequisite = tokens[1].trim();
        }
        rawPrerequisite = rawPrerequisite.trim();
        System.out.println("\nOriginal: " + rawPrerequisite);

        Pattern anyOneModule = patterns.get(PatternTypes.ANY_ONE_MODULE_GREEDY);
        if (anyOneModule.matcher(rawPrerequisite).matches()) {
            // a
            ArrayList<String> codes = extractModuleCodesFromOneModuleCode(rawPrerequisite);
            prerequisites = generateDependencyStringWithoutNesting(Operator.OR, codes);
        } else if (rawPrerequisite.contains("(") && !rawPrerequisite.contains("[")) {
            // (a and/or b) and/or (c and/or d)
            // (a and/or b) and/or c
            Pair<Operator, ArrayList<String>> topLevel = extractTopLevel(rawPrerequisite, "\\(", "\\)", ")");

            ArrayList<Operator> secondLevelOperators = new ArrayList<>();
            ArrayList<ArrayList<String>> allModuleCodes = new ArrayList<>();
            for (String token : topLevel.getValue()) {
                Pair<Operator, ArrayList<String>> secondLevel = extractSecondLevel(token);
                if (!secondLevel.getValue().isEmpty()) {
                    secondLevelOperators.add(secondLevel.getKey());
                    allModuleCodes.add(secondLevel.getValue());
                }
            }

            prerequisites = generateDependencyStringWithNesting(topLevel.getKey(), secondLevelOperators, allModuleCodes);
//            System.out.println("Processed: " + prerequisites + "\n");
        } else if (rawPrerequisite.contains("[")) {
            // triple level nesting
            if (rawModule.ModuleCode.contains("CP3106")) {
                prerequisites = "(CS2102 and CS2105 and CS3214) or (CS2102 and CS2105 and CS3215) or (CS2102S and CS2105 and CS3214) or (CS2102S and CS2105 and CS3215) or IS3102 or IS4102 or CS3201 or CS3281 or CS4201 or CS4203";
            }
        } else {
            Pair<Operator, ArrayList<String>> modules = extractSecondLevel(rawPrerequisite);
            prerequisites = generateDependencyStringWithoutNesting(modules.getKey(), modules.getValue());
//            System.out.println("Processed: " + prerequisites);
        }

        return prerequisites;
    }

    private static String generateDependencyStringWithNesting(Operator topLevelOperator, ArrayList<Operator>
            secondLevelOperators, ArrayList<ArrayList<String>> allModuleCodes) {
        StringBuilder prereqBuilder = new StringBuilder();
        Iterator<Operator> secondLevelOperatorIter = secondLevelOperators.iterator();
        for (int i = 0; i < allModuleCodes.size(); i++) {
            ArrayList<String> modules = allModuleCodes.get(i);

            String currentTopLevelOperatorString = "";
            if (topLevelOperator == Operator.AND) {
                currentTopLevelOperatorString = AND_WORD;
            } else if (topLevelOperator == Operator.OR) {
                currentTopLevelOperatorString = OR_WORD;
            }

            String internal = generateDependencyStringWithoutNesting(secondLevelOperatorIter.next(), modules);
            if (internal.isEmpty()) {
                continue;
            }

            if (allModuleCodes.size() > 1) {
                if (internal.contains(OR_WORD) || internal.contains(AND_WORD)) {
                    prereqBuilder.append(OPEN_BRACKET);
                    prereqBuilder.append(internal);
                    prereqBuilder.append(CLOSE_BRACKET);
                } else {
                    prereqBuilder.append(internal);
                }

                if (i != allModuleCodes.size()-1) {
                    prereqBuilder.append(currentTopLevelOperatorString);
                }
            } else {
                prereqBuilder.append(internal);
            }
        }

        return prereqBuilder.toString();
    }

    private static String generateDependencyStringWithoutNesting(Operator operator, ArrayList<String> modules) {
        modules.removeIf(module -> module.isEmpty());
        if (modules.isEmpty()) {
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();
        String operatorString = "";
        if (operator == Operator.AND) {
            operatorString = AND_WORD;
        } else if (operator == Operator.OR) {
            operatorString = OR_WORD;
        }

        for (int i = 0; i < modules.size() - 1; i++) {
            stringBuilder.append(modules.get(i));
            stringBuilder.append(operatorString);
        }
        stringBuilder.append(modules.get(modules.size()-1));

        return stringBuilder.toString();
    }

    // tokenize according to brackets then recognize operator between brackets
    private static Pair<Operator, ArrayList<String>> extractTopLevel(String rawInput, String regexDelimiterOpen, String
            regexDelimiterClose, String delimiterClose) {
        String[] rawTokens = rawInput.split(regexDelimiterOpen);
        ArrayList<String> tokens = new ArrayList<>(rawTokens.length);
        Operator operator = null;

        for (String token : rawTokens) {
            token = token.trim();
            if (token.isEmpty()) {
                // do nothing
            } else if (token.endsWith(ModulesParser.AND)) {
                // ModulesParser.AND_WORD is lowercase "and". Prerequisites don't contain camelCase or uppercase "and"s.
                operator = Operator.AND;
                tokens.add(token.substring(0, token.length() - ModulesParser.AND.length()).trim());
            } else if (token.endsWith(ModulesParser.OR)) {
                operator = Operator.OR;
                tokens.add(token.substring(0, token.length() - ModulesParser.OR.length()).trim());
            } else if (token.contains(delimiterClose)) {
                // last token
                // check if contains closing bracket to account for cases like "(a and/or b) and/or c"
                String[] smallerRawTokens = token.split(regexDelimiterClose, 2);
                tokens.add(smallerRawTokens[0].trim());
                String rest = smallerRawTokens[1].trim();
                if (rest.startsWith(ModulesParser.AND)) {
                    operator = Operator.AND;
                    String rightModule = rest.substring(ModulesParser.AND.length()).trim();
                    tokens.add(rightModule.trim());
                } else if (rest.startsWith(ModulesParser.OR)) {
                    operator = Operator.OR;
                    String rightModule = rest.substring(ModulesParser.OR.length()).trim();
                    tokens.add(rightModule.trim());
                } else {
                    // ???
                }
            } else {
                tokens.add(token);
            }
        }

        return new Pair<>(operator, tokens);
    }

    // if token contains >1 operator, the operators must be of the same type.
    // if token contains 0 operators, operator is returned as null.
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
                    Pattern anyTwoModules = patterns.get(PatternTypes.ANY_TWO_MODULES);
                    if (anyTwoModules.matcher(rawModuleToken).matches()) {
                        String[] codes = rawModuleToken.split("/");
                        for (String code : codes) {
                            moduleCodes.addAll(extractModuleCodesFromOneModuleCode(code));
                        }
                    } else {
                        System.out.println("DOESNT MATCH: " + rawModuleToken);
                    }
                }
            }
        } else if (token.contains(ModulesParser.AND_WORD)) {
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
        } else {
            if (anyOneModule.matcher(token).matches()) {
                ArrayList<String> codes = extractModuleCodesFromOneModuleCode(token);
                moduleCodes.addAll(codes);
            }
        }

        return new Pair<>(operator, moduleCodes);
    }

    private static ArrayList<String> extractModuleCodesFromOneModuleCode(String code) {
        String trimmedCode = code.split(" ")[0].replaceAll("[^/a-zA-Z0-9]", "");

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

    private static String parseCorequisites(NusmodsModule rawModule) {
        String corequisites = "";
        if (rawModule.Corequisite == null) {
            return corequisites;
        }

        String corequisite = rawModule.Corequisite.trim();

        Pattern anyOneModule = patterns.get(PatternTypes.ANY_ONE_MODULE_GREEDY);
        if (anyOneModule.matcher(corequisite).matches()) {
            ArrayList<String> codes = extractModuleCodesFromOneModuleCode(corequisite);
            corequisites = generateDependencyStringWithoutNesting(Operator.OR, codes);
        }

        // extract "co-read ..." from prerequisites
//        System.out.println("COREQUISITES: " + corequisites);
        return corequisites;
    }

    private static String parsePreclusions(NusmodsModule rawModule) {
        String preclusions = "";
        if (rawModule.Preclusion == null) {
            return preclusions;
        }

        String preclusion = rawModule.Preclusion.trim();

        Pattern anyOneModule = patterns.get(PatternTypes.ANY_ONE_MODULE_GREEDY);
        if (anyOneModule.matcher(preclusion).matches()){
            ArrayList<String> codes = extractModuleCodesFromOneModuleCode(preclusion);
            preclusions = generateDependencyStringWithoutNesting(Operator.OR, codes);
        }

//        System.out.println("PRECLUSIONS: " + preclusions);
        return preclusions;
    }

    private static Hashtable<PatternTypes, Pattern> generatePatterns() {
        Hashtable<PatternTypes, Pattern> patterns = new Hashtable<>();

        //eg CS1010, CS1231R, CS2103/T <module title>
        String regexAnyModuleGreedy = "(([a-zA-Z]){0,2}(\\d){4}([a-zA-Z]){0,2})[a-zA-Z_\\s_\\p{Punct}]*";
        Pattern anyOneGreedy = Pattern.compile(regexAnyModuleGreedy);
        patterns.put(PatternTypes.ANY_ONE_MODULE_GREEDY, anyOneGreedy);

        // eg CS1010
        String regexAnyModuleExact = "([a-zA-Z]){0,2}(\\d){4}([a-zA-Z]){0,2}";
        Pattern anyOneExact = Pattern.compile(regexAnyModuleExact);
        patterns.put(PatternTypes.ANY_ONE_MODULE_EXACT, anyOneExact);

        // eg CS2103/CS2103T <module title>
        String regexAnyTwoModules = regexAnyModuleExact + "/" + regexAnyModuleExact + "[a-zA-Z_\\s_\\p{Punct}]*";
        Pattern anyTwoModules = Pattern.compile(regexAnyTwoModules);
        patterns.put(PatternTypes.ANY_TWO_MODULES, anyTwoModules);

        return patterns;
    }

    private static ArrayList<Lesson> parseTimetable(NusmodsModule rawModule) {
        return new ArrayList<>();
    }
}
