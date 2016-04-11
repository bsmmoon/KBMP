package backend;

import common.FocusArea;
import common.Module;

import java.util.ArrayList;

/*
 * Needs to support bidirectional parsing (Java <-> CLIPS)
 */
public class ClipsParser {
	public static ArrayList<String> parseModuleIntoClips(Module module) {
		ArrayList<String> result = new ArrayList<>();

		String out;
		out = "(assert (module " +
				"(code \"" + module.getCode() +"\")" +
				"(name \"" + module.getName() +"\")" +
				"(MC " + module.getCredits() + ")";
		String code = module.getCode();
		for (int i = 0; i < code.length(); i++) {
			int c = code.charAt(i);
			if (c >= 48 && c <= 57) {
				out += "(prefix \"" + code.substring(0, i) + "\")";
				out += "(level " + code.charAt(i) + ")";
				out += "(rest \"" + code.substring(i+1, code.length()) + "\")";
				break;
			}
		}

		ArrayList<String> prereqCombinations = parsePrereq(module.getPrerequisites());
		if (prereqCombinations.size() > 0) {
			String combination;
			for (String prereq : prereqCombinations) {
				combination = out;
				combination += prereq;
				combination += "))";
				result.add(combination);
			}
		} else {
			out += "))";
			result.add(out);
		}

//		result.forEach((line) -> System.out.println(line));

		return result;
	}

	public static String parseFocusAreaIntoClips(FocusArea focusArea) {
		String out;
		out = "(assert (focus " +
				"(name \"" + focusArea.getName() + "\")";

		out += "(primaries";
		ArrayList<String> primaries = focusArea.getPrimaries();
		if (primaries.isEmpty()) out += "\"\"";
		else for (String primary : primaries) out += " \"" + primary + "\"";
		out += ")";

		out += "(electives";
		ArrayList<String> electives = focusArea.getElectives();
		if (electives.isEmpty()) out += "\"\"";
		else for (String elective : electives) out += " \"" + elective + "\"";
		out += ")";

		out += "(unrestricted-electives";
		ArrayList<String> unrestrictedElectives = focusArea.getUnrestrictedElectives();
		if (unrestrictedElectives.isEmpty()) out += "\"\"";
		else for (String unrestrictedElective : unrestrictedElectives) out += " \"" + unrestrictedElective + "\"";
		out += ")";

		out += "))";

		return out;
	}

	public static ArrayList<String> parsePreclusions(ArrayList<Module> modules) {
		for (Module module : modules) {
			if (module.getPreclusions().isEmpty()) continue;
		}
		return new ArrayList<>();
	}

	private static ArrayList<String> parsePrereq(String prereq) {
		ArrayList<String> result = new ArrayList<>();
		prereq = prereq.trim();
		if (prereq.isEmpty()) {
			return result;
		}

		ArrayList<String[]> list = new ArrayList<>();
		int from, to;
		while (prereq.contains("(")) {
			from = prereq.indexOf("(");
			to = prereq.indexOf(")");
			list.add(prereq.substring(from+1, to).split(" or "));
			prereq = prereq.replace(prereq.substring(from, to+1), "");
		}
		String[] arr = prereq.split(" and ");
		if (arr.length == 1 && list.isEmpty()) {
			list.add(arr[0].split(" or "));
			arr = new String[0];
		}

		for (String str : arr) {
			if (str.isEmpty()) continue;
			list.add(new String[]{str});
		}

		result = combination(list);
		return result;
	}

	private static ArrayList<String> combination(ArrayList<String[]> list) {
		ArrayList<String> result = new ArrayList<>();
		combinationRecurr(list, 0, "", result);
		return result;
	}

	private static void combinationRecurr(ArrayList<String[]> list, int index, String command, ArrayList<String> result) {
		if (index == list.size()) {
			result.add("(prerequisites" + command + ")");
			return;
		}
		for (String str : list.get(index)) {
			combinationRecurr(list, index + 1, command + " \"" + str + "\"", result);
		}
	}
}
