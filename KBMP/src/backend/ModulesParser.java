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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Joey on 28/3/16.
 */
public class ModulesParser {
    private static String ERROR_MESSAGE_MODULES_NOT_READABLE = "Path to module database is not readable";
    private static String ERROR_MESSAGE_WHITELIST_NOT_READABLE = "Path to whitelisted modules database is not readable";
    private static String ERROR_MESSAGE_BLACKLIST_NOT_READABLE = "Path to blacklisted modules database is not readable";
    private static String ERROR_MESSAGE_FOUNDATION_LIST_NOT_READABLE = "Path to foundation modules database is not " +
            "readable";
    private static String ERROR_MESSAGE_OTHER_REQUIRED_LIST_NOT_READABLE = "Path to other required modules database " +
            "is not readable";
    private static String WHITELIST_FILE_LOCATION = "data/Modules_Whitelist.txt";
    private static String BLACKLIST_FILE_LOCATION = "data/Modules_Blacklist.txt";
    private static String FOUNDATION_MODULES_FILE_LOCATION = "data/Foundation_Modules.txt";
    private static String OTHER_REQUIRED_MODULES_FILE_LOCATION = "data/Other_Required_Modules.txt";
    private enum PatternTypes {ANY_ONE_MODULE_GREEDY, ANY_ONE_MODULE_EXACT, ANY_TWO_MODULES,
        SENTENCE_FRAGMENT_CONTAINING_MODULES}
    private static Hashtable<PatternTypes, Pattern> patterns = generatePatterns();
    private static Hashtable<String, Module> existingModules = null;
    private static Module.Semester currentSemester;
    public static String AND_WORD = " and ";
    public static String AND = "and";
    public static String OR_WORD = " or ";
    public static String OR = "or";
    public static String OPEN_BRACKET = "(";
    public static String CLOSE_BRACKET = ")";
    public static String COMMA = ",";
    private static String CASE_INSENSITIVE_FLAG = "(?i)";
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
        applyTypes(modules);
        pairModules(modules);

        modules.putAll(existingModules);

        // reset instance-specific data.
        ModulesParser.existingModules = null;
        ModulesParser.currentSemester = null;

        return modules;
    }

    private static void applyTypes(Hashtable<String, Module> modules) throws IOException {
        ArrayList<String> foundationModules;
        ArrayList<String> otherRequiredModules;
        try {
            foundationModules = getFoundationModules(Paths.get(ModulesParser.FOUNDATION_MODULES_FILE_LOCATION));
        } catch (IOException ioe) {
            throw new IOException(ERROR_MESSAGE_FOUNDATION_LIST_NOT_READABLE);
        }

        try {
            otherRequiredModules = getOtherRequiredModules(Paths.get(ModulesParser
                    .OTHER_REQUIRED_MODULES_FILE_LOCATION));
        } catch (IOException ioe) {
            throw new IOException(ERROR_MESSAGE_OTHER_REQUIRED_LIST_NOT_READABLE);
        }

        for (String foundationModule : foundationModules) {
            Module module = modules.get(foundationModule);
            if (module == null) {
                continue;
            }
            module.setType(Module.Type.FOUNDATION);
        }

        for (String otherRequiredModule : otherRequiredModules) {
            Module module = modules.get(otherRequiredModule);
            if (module == null) {
                continue;
            }
            module.setType(Module.Type.OTHER_REQUIRED);
        }

        for (Module module : modules.values()){
            if (module.getType() == null) {
                String moduleCode = module.getCode();
                if (moduleCode.matches(CASE_INSENSITIVE_FLAG + "CS320[12]")) {
                    module.setType(Module.Type.SOFTWARE_ENG_PROJECT);
                } else if (moduleCode.matches(CASE_INSENSITIVE_FLAG + "CS328[12]")) {
                    module.setType(Module.Type.THEMATIC_SYSTEMS_PROJECT);
                } else if (moduleCode.matches(CASE_INSENSITIVE_FLAG + "CS32[17]")) {
                    module.setType(Module.Type.SOFTWARE_ENG_1617_PROJECT);
                } else if (moduleCode.matches(CASE_INSENSITIVE_FLAG + "CS328[34]")) {
                    module.setType(Module.Type.MEDIA_TECH_PROJECT);
                } else if (moduleCode.matches(CASE_INSENSITIVE_FLAG + "CP320[02]")) {
                    module.setType(Module.Type.THREE_MONTHS_INTERNSHIP);
                } else if (moduleCode.equalsIgnoreCase("CP3880")) {
                    module.setType(Module.Type.SIX_MONTHS_INTERNSHIP);
                } else if (moduleCode.equalsIgnoreCase("CP4101")) {
                    module.setType(Module.Type.FINAL_YEAR_PROJECT);
                } else if (moduleCode.startsWith("CS")) {
                    module.setType(Module.Type.BREADTH_AND_DEPTH);
                } else {
                    module.setType(Module.Type.OTHER);
                }
            }
        }
    }

    private static void pairModules(Hashtable<String, Module> modules) {
        Module module = modules.get("CS3201");
        if (module != null) {
            module.setPairedWith("CS3202");
        }

        module = modules.get("CS3202");
        if (module != null) {
            module.setPairedWith("CS3201");
        }

        module = modules.get("CS3281");
        if (module != null) {
            module.setPairedWith("CS3282");
        }

        module = modules.get("CS3282");
        if (module != null) {
            module.setPairedWith("CS3281");
        }
    }

    private static ArrayList<String> getFoundationModules(Path pathToFile) throws IOException {
        return new ArrayList<>(Files.readAllLines(pathToFile));
    }

    private static ArrayList<String> getOtherRequiredModules(Path pathToFile) throws IOException {
        return new ArrayList<>(Files.readAllLines(pathToFile));
    }

    private static ArrayList<NusmodsModule> getRawModules(Path pathToFile) throws IOException {
        File moduleJson = pathToFile.toFile();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ArrayList<NusmodsModule> rawModules = objectMapper.readValue(moduleJson, new
                TypeReference<ArrayList<NusmodsModule>>(){});
        return rawModules;
    }

    private static ArrayList<NusmodsModule> filterByModuleCode(ArrayList<NusmodsModule> rawModules, Pattern
            whitelist) throws IOException {
        Iterator<NusmodsModule> moduleIterator = rawModules.iterator();

        ArrayList<String> blacklist;
        try {
            Path pathToBlacklist = Paths.get(ModulesParser.BLACKLIST_FILE_LOCATION);
            blacklist = new ArrayList<>(Files
                    .readAllLines(pathToBlacklist));
        } catch (IOException ioe) {
            throw new IOException(ModulesParser.ERROR_MESSAGE_BLACKLIST_NOT_READABLE);
        }

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
        Path pathToWhitelistFile = Paths.get(ModulesParser.WHITELIST_FILE_LOCATION);
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
            String[] restOfTokens = rest.split(COMMA);
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

        if (rawModule.ModuleDescription != null) {
            moduleBuilder.setDescription(rawModule.ModuleDescription.trim());
        }

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

        String moduleCode = rawModule.ModuleCode.trim();
        if (moduleCode.equalsIgnoreCase("CS4350")) {
            return "CS3247";
        } else if (moduleCode.equalsIgnoreCase("CS4243")) {
            return "CS1020 and (MA1101R or MA1506) and (MA1102R or MA1505C or MA1505 or MA1521) and (ST1232 " +
                    "or ST2131 or ST2334)";
        } else if (moduleCode.equalsIgnoreCase("CS3242")) {
            return "CS3241 and PC1221 and MA1521 and MA1101R";
        } else if (moduleCode.equalsIgnoreCase("CS2220")) {
            return "CS1020";
        }

//        System.out.println("\nModule: " + rawModule.ModuleCode);

        rawPrerequisite = rawPrerequisite.replace("For SoC students only.", "");
        if (rawPrerequisite.contains("Other students:")) {
            rawPrerequisite = rawPrerequisite.split("Other students:")[0];
            String[] tokens = rawPrerequisite.split(":");
            rawPrerequisite = tokens[1].trim();
        }
        rawPrerequisite = rawPrerequisite.trim();
        if (rawPrerequisite.endsWith(".")) {
            rawPrerequisite = rawPrerequisite.substring(0, rawPrerequisite.length()-1);
        }
//        System.out.println("Original: " + rawPrerequisite);

        if (rawPrerequisite.contains(".")) {
            String[] sentences = rawPrerequisite.split("\\.");
            ArrayList<String> parsedSentences = new ArrayList<>();
            for (String sentence : sentences) {
                String parsedSentence = "";
                if (moduleCode.endsWith("R")) {
                    if (sentence.contains("pass host module in previous")) {
                        parsedSentence = moduleCode.substring(0, moduleCode.length() - 1);
                    } else if (sentence.contains("Co-read host module")) {
                        parsedSentence = moduleCode.substring(0, moduleCode.length() - 1);
                    }
                } else {
                    parsedSentence = parsePrerequisiteFromSentence(sentence, rawModule.ModuleCode);
                }
//                System.out.println("Original fragment: " + sentence);
//                System.out.println("Parsed fragment: " + parsedSentence);
                if (!parsedSentence.isEmpty()) {
                     parsedSentences.add(parsedSentence);
                }
            }
            prerequisites = generateDependencyStringWithoutNesting(Operator.AND, parsedSentences);
        } else {
            prerequisites = parsePrerequisiteFromSentence(rawPrerequisite, rawModule.ModuleCode);
        }
//        System.out.println("Processed: " + prerequisites);
        return prerequisites;
    }

    private static String parsePrerequisiteFromSentence(String rawPrerequisite, String moduleCode) {
        String prerequisites;

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
            for (String token : topLevel.getSecond()) {
                Pair<Operator, ArrayList<String>> secondLevel = extractSecondLevel(token);
                if (!secondLevel.getSecond().isEmpty()) {
                    secondLevelOperators.add(secondLevel.getFirst());
                    allModuleCodes.add(secondLevel.getSecond());
                }
            }

            prerequisites = generateDependencyStringWithNesting(topLevel.getFirst(), secondLevelOperators, allModuleCodes);
//            System.out.println("Processed: " + prerequisites + "\n");
        } else {
            Pair<Operator, ArrayList<String>> modules = extractSecondLevel(rawPrerequisite);
            prerequisites = generateDependencyStringWithoutNesting(modules.getFirst(), modules.getSecond());
//            System.out.println("Processed: " + prerequisites);
        }
        return prerequisites;
    }

    private static String generateDependencyStringWithNesting(Operator topLevelOperator, ArrayList<Operator>
            secondLevelOperators, ArrayList<ArrayList<String>> allModuleCodes) {
        StringBuilder dependencyStringBuilder = new StringBuilder();
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
                if (internal.toLowerCase().contains(OR_WORD) || internal.toLowerCase().contains(AND_WORD)) {
                    dependencyStringBuilder.append(OPEN_BRACKET);
                    dependencyStringBuilder.append(internal);
                    dependencyStringBuilder.append(CLOSE_BRACKET);
                } else {
                    dependencyStringBuilder.append(internal);
                }

                if (i != allModuleCodes.size()-1) {
                    dependencyStringBuilder.append(currentTopLevelOperatorString);
                }
            } else {
                dependencyStringBuilder.append(internal);
            }
        }

        return dependencyStringBuilder.toString();
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
        if (token.toLowerCase().contains(ModulesParser.OR_WORD)) {
            String[] rawModuleTokens;
            operator = Operator.OR;

            if (token.contains(",")) {
                String[] tokenizedByOr = token.split(CASE_INSENSITIVE_FLAG + ModulesParser.OR_WORD);
                ArrayList<String> tokens = new ArrayList<>();
                for (String rawModuleToken : tokenizedByOr) {
                    if (rawModuleToken.contains(",")) {
                        String[] smallerRawTokens = rawModuleToken.split(",");
                        // can just add because it's OR
                        tokens.addAll(Arrays.asList(smallerRawTokens));
                    } else {
                        tokens.add(rawModuleToken);
                    }
                }
                rawModuleTokens = tokens.toArray(new String[0]);
            } else {
                rawModuleTokens = token.split(CASE_INSENSITIVE_FLAG + ModulesParser.OR_WORD);
            }

            ArrayList<String> newTokens = splitModulesIfNecessary(rawModuleTokens);

            for (String rawModuleToken : newTokens) {
                rawModuleToken = rawModuleToken.trim();
                if (anyOneModule.matcher(rawModuleToken).matches()) {
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
                        Matcher matcher = anyOneModule.matcher(rawModuleToken);
                        if (matcher.find()) {
                            moduleCodes.add(matcher.group());
//                        } else {
//                        System.out.println("DOESNT MATCH: " + rawModuleToken);
                        }
                    }
                }
            }
        } else if (token.toLowerCase().contains(ModulesParser.AND_WORD)) {
            String[] rawModuleTokens = token.split(CASE_INSENSITIVE_FLAG + ModulesParser.AND_WORD);
            ArrayList<String> newTokens = splitModulesIfNecessary(rawModuleTokens);
            int count = 0;
            for (String rawModuleToken : newTokens) {
                if (anyOneModule.matcher(rawModuleToken).matches()) {
                    count++;
                    ArrayList<String> codes = extractModuleCodesFromOneModuleCode(rawModuleToken);
                    moduleCodes.addAll(codes);
                } else {
//                    System.out.println("DOESNT MATCH: " + rawModuleToken);
                }
            }
            if (count > 0) { // "and" isn't present only within a module's title
                operator = Operator.AND;
            }
        } else if (token.contains(",")) {
            // no "and", no "or". just commas -> assume OR because it's preclusions
            String[] rawModuleTokens;
            operator = Operator.OR;

            String[] tokens = token.split(COMMA);

            for (String rawModuleToken : tokens) {
                rawModuleToken = rawModuleToken.trim();
                if (anyOneModule.matcher(rawModuleToken).matches()) {
                    ArrayList<String> codes = extractModuleCodesFromOneModuleCode(rawModuleToken);
                    moduleCodes.addAll(codes);
                } else {
//                    System.out.println("DOESNT MATCH: " + rawModuleToken);
                }
            }
        } else if (anyOneModule.matcher(token).matches()) {
                ArrayList<String> codes = extractModuleCodesFromOneModuleCode(token);
                moduleCodes.addAll(codes);
        } else {

        }

        return new Pair<>(operator, moduleCodes);
    }

    // written for a particular case
    // assumes that any operators found in is not important
    private static ArrayList<String> splitModulesIfNecessary(String[] tokens) {
        ArrayList<String> newTokens = new ArrayList<>();
        for (String token : tokens) {
            if (token.toLowerCase().contains(AND_WORD)) {
                String[] smallerTokens = token.split(CASE_INSENSITIVE_FLAG + AND_WORD);
                for (String smallerToken : smallerTokens) {
                    newTokens.add(smallerToken.trim());
                }
            } else if (token.toLowerCase().contains(OR_WORD)) {
                String[] smallerTokens = token.split(CASE_INSENSITIVE_FLAG + OR_WORD);
                for (String smallerToken : smallerTokens) {
                    newTokens.add(smallerToken.trim());
                }
            } else {
                newTokens.add(token);
            }
        }

        return newTokens;
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

//        System.out.println(rawModule.ModuleCode);

        if (rawModule.ModuleCode.contains("CS3281")) {
            return "CS3282";
        } else if (rawModule.ModuleCode.contains("CS3282")) {
            return "CS3281";
        }

        Pattern anyOneModule = patterns.get(PatternTypes.ANY_ONE_MODULE_GREEDY);
        if (anyOneModule.matcher(corequisite).matches()) {
            ArrayList<String> codes = extractModuleCodesFromOneModuleCode(corequisite);
            corequisites = generateDependencyStringWithoutNesting(Operator.OR, codes);
        } else {
            // for CS2101 and CS2103T
            Pattern exact = patterns.get(PatternTypes.ANY_ONE_MODULE_EXACT);
            Matcher matcher = exact.matcher(corequisite);
            if (matcher.find()) {
                corequisites = matcher.group();
            }

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

//        System.out.print(rawModule.ModuleCode + " ");

        if (rawModule.ModuleCode.contains("MA1301X")) {
            return "MA1301 or MA1301FC";
        }

        String rawPreclusion = rawModule.Preclusion.trim();
//        System.out.println("\nModule: " + rawModule.ModuleCode);
//        System.out.println("Original: " + rawPreclusion);

        if (rawPreclusion.contains(".")) {
            String[] sentences = rawPreclusion.split("\\.");
            ArrayList<String> parsedSentences = new ArrayList<>();
            for (String sentence : sentences) {
                String parsedSentence = parsePreclusionFromSentence(sentence);
                if (!parsedSentence.isEmpty()) {
                    parsedSentences.add(parsedSentence);
                }
            }

            preclusions = generateDependencyStringWithoutNesting(Operator.OR, parsedSentences);
        } else {
            preclusions = parsePreclusionFromSentence(rawPreclusion);
        }

//        System.out.println(preclusions);
        return preclusions;
    }

    private static String parsePreclusionFromSentence(String rawPreclusion) {
        String preclusions;
        Pattern anyOneModule = patterns.get(PatternTypes.ANY_ONE_MODULE_GREEDY);
//        System.out.println("Sentence: " + rawPreclusion);

        if (anyOneModule.matcher(rawPreclusion).matches()){
            ArrayList<String> codes = extractModuleCodesFromOneModuleCode(rawPreclusion);
            preclusions = generateDependencyStringWithoutNesting(Operator.OR, codes);
        } else if (rawPreclusion.contains("(")) {
            // (a and/or b) and/or (c and/or d)
            // (a and/or b) and/or c
            Pair<Operator, ArrayList<String>> topLevel = extractTopLevel(rawPreclusion, "\\(", "\\)", ")");

            ArrayList<Operator> secondLevelOperators = new ArrayList<>();
            ArrayList<ArrayList<String>> allModuleCodes = new ArrayList<>();
            for (String token : topLevel.getSecond()) {
                Pair<Operator, ArrayList<String>> secondLevel = extractSecondLevel(token);
                if (!secondLevel.getSecond().isEmpty()) {
                    secondLevelOperators.add(secondLevel.getFirst());
                    allModuleCodes.add(secondLevel.getSecond());
                }
            }

            preclusions = generateDependencyStringWithNesting(topLevel.getFirst(), secondLevelOperators, allModuleCodes);
        } else {
            Pair<Operator, ArrayList<String>> modules = extractSecondLevel(rawPreclusion);
            preclusions = generateDependencyStringWithoutNesting(modules.getFirst(), modules.getSecond());
        }
        return preclusions;
    }

    private static Hashtable<PatternTypes, Pattern> generatePatterns() {
        Hashtable<PatternTypes, Pattern> patterns = new Hashtable<>();

        // eg "CS1010"
        String regexAnyModuleExact = "([a-zA-Z]){2,3}(\\d){4}([a-zA-Z]){0,2}";
        Pattern anyOneExact = Pattern.compile(regexAnyModuleExact);
        patterns.put(PatternTypes.ANY_ONE_MODULE_EXACT, anyOneExact);

        //eg "CS1010", "CS1231R", "CS2103/T <module title>"
        String regexAnyModuleGreedy = "(" + regexAnyModuleExact + ")[a-zA-Z_\\s_\\p{Punct}]*";
        Pattern anyOneGreedy = Pattern.compile(regexAnyModuleGreedy);
        patterns.put(PatternTypes.ANY_ONE_MODULE_GREEDY, anyOneGreedy);

        // eg "CS2103/CS2103T <module title>"
        String regexAnyTwoModules = regexAnyModuleExact + "/" + regexAnyModuleExact + "[a-zA-Z_\\s_\\p{Punct}]*";
        Pattern anyTwoModules = Pattern.compile(regexAnyTwoModules);
        patterns.put(PatternTypes.ANY_TWO_MODULES, anyTwoModules);

        // eg "CS1101C, CS1101S. Engineering"
        String regexModuleInSentenceFragment = ".*?" + regexAnyModuleExact + ".*?\\.";
        String regexModuleAfterSentenceFragment = ".*?\\." + ".*?" + regexAnyModuleExact + ".*?";
        Pattern sentenceFragmentContainingModules = Pattern.compile(regexModuleInSentenceFragment + "|" + regexModuleAfterSentenceFragment);
        patterns.put(PatternTypes.SENTENCE_FRAGMENT_CONTAINING_MODULES, sentenceFragmentContainingModules);

        return patterns;
    }

    private static ArrayList<Lesson> parseTimetable(NusmodsModule rawModule) {
        return new ArrayList<>();
    }
}
