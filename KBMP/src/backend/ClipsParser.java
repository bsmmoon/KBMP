package backend;

import common.FocusArea;
import common.Module;

import java.util.ArrayList;

/*
 * Needs to support bidirectional parsing (Java <-> CLIPS)
 */
public class ClipsParser {
	public static String parseModuleIntoClips(Module module) {
		String out;
		out = "(assert (module " +
				"(code \"" + module.getCode() +"\")" +
				"(name \"" + module.getName() +"\")" +
				"(MC " + module.getCredits() + ")";
		if (module.getPrerequisites().size() > 0) {
			out += "(prerequisites";
			for (String prerequisite : module.getPrerequisites()) {
				out += " \"" + prerequisite + "\"";
			}
			out += ")";
		}

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

		out += "))";
		return out;
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
}
