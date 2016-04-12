package backend;

import java.util.ArrayList;
import java.util.ListIterator;

import common.AvailableModule;
import common.FocusArea;
import net.sf.clipsrules.jni.*;

import common.Module;

public class ClipsWrapper {
	private ClipsParser parser;
	
	private Environment clips;
	private boolean initialised;
	private ArrayList<String> modulesInFocusAreas;
	
	private static final String GET_ALL_AVAIABLE_MODULE = "(find-all-facts ((?f module)) TRUE)";
	private static final String GET_ALL_FOCUS_AREA = "(find-all-facts ((?f focus)) TRUE)";
	
	public ClipsWrapper() {
		this.initialised = false;
	}
	
	public void execute(String command) { clips.eval(command); }
	
	public ArrayList<AvailableModule> getAvailableModules() {
		ArrayList<AvailableModule> modules = new ArrayList<>();
		
		MultifieldValue pv = (MultifieldValue) clips.eval(GET_ALL_AVAIABLE_MODULE);
		
		ListIterator<FactAddressValue> itr = pv.multifieldValue().listIterator();
		int len = pv.size();
		String code, name, recommend;
		int weight;
		while (len-- > 0 && itr.hasNext()) {
			FactAddressValue address = itr.next();
			try {
				if (address.getFactSlot("status").toString().equals("available")) {
					code = address.getFactSlot("code").toString().replace("\"", "");
					weight = Integer.parseInt(address.getFactSlot("score").toString());
					recommend = address.getFactSlot("recommend").toString();
					modules.add(new AvailableModule(code, weight, recommend));
				}
			} catch (Exception e) {
				System.out.println("Something went wrong! " + e.getMessage());
			}
		}
		
		return modules;
	}

	public void printFactsOnConsole() { clips.eval("(facts)"); }

	public void saveModules(ArrayList<Module> modules) {
		for (Module module : modules) {
			if (getModuleLevel(module.getCode()) >= 4
					&& !modulesInFocusAreas.contains(module.getCode())) continue;
			ArrayList<String> entries = ClipsParser.parseModuleIntoClips(module);
			entries.forEach((entry) -> clips.eval(entry));
		}
		ArrayList<String> preclusions = ClipsParser.parsePreclusions(modules);
	}

	public void saveFocusAreas(ArrayList<FocusArea> focusAreas) {
		focusAreas.forEach((focusArea) -> clips.eval(ClipsParser.parseFocusAreaIntoClips(focusArea)));
	}

	public void saveModulesInFocusAreas(ArrayList<String> modulesInFocusAreas) { this.modulesInFocusAreas = modulesInFocusAreas; }

	public void reset() { clips.reset(); }

	public void run() { clips.run(); }
	
	public void init(String condition) {
		if (!initialised) {
			clips = new Environment();
			initialised = true;
		} else {
			clips.clear();
		}
		clips.loadFromString(condition);
	}

	private int getModuleLevel(String code) {
		for (int i = 0; i < code.length(); i++) {
			int c = code.charAt(i);
			if (!(c >= 48 && c <= 57)) continue;
			return Integer.parseInt(code.substring(i, i + 1));
		}
		return -1;
	}
}
