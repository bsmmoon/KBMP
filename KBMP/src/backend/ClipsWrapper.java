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
	
	private static final String GET_ALL_AVAIABLE_MODULE = "(find-all-facts ((?f module)) TRUE)";
	private static final String GET_ALL_FOCUS_AREA = "(find-all-facts ((?f focus)) TRUE)";
	
	public ClipsWrapper() {
		this.initialised = false;
	}
	
	public void execute(String command) {
		System.out.println(command);
		clips.eval(command);
	}
	
	public ArrayList<AvailableModule> getAvailableModules() {
		ArrayList<AvailableModule> modules = new ArrayList<>();
		
		MultifieldValue pv = (MultifieldValue) clips.eval(GET_ALL_AVAIABLE_MODULE);
		
		ListIterator<FactAddressValue> itr = pv.multifieldValue().listIterator();
		int len = pv.size();
		String code, name;
		int weight;
		while (len-- > 0 && itr.hasNext()) {
			FactAddressValue address = itr.next();
			try {
				if (address.getFactSlot("status").toString().equals("available")) {
					code = address.getFactSlot("code").toString().replace("\"", "");
					weight = Integer.parseInt(address.getFactSlot("score").toString());
					modules.add(new AvailableModule(code, weight));
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
			ArrayList<String> entries = ClipsParser.parseModuleIntoClips(module);
			entries.forEach((entry) -> clips.eval(entry));
		}
		ArrayList<String> preclusions = ClipsParser.parsePreclusions(modules);
	}

	public void saveFocusAreas(ArrayList<FocusArea> focusAreas) {
		focusAreas.forEach((focusArea) -> clips.eval(ClipsParser.parseFocusAreaIntoClips(focusArea)));
	}

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
}
