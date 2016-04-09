package backend;

import java.util.ArrayList;
import java.util.ListIterator;

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
	
	public ArrayList<Module> getAvailableModules() {
		ArrayList<Module> modules = new ArrayList<Module>();
		
		MultifieldValue pv = (MultifieldValue) clips.eval(GET_ALL_AVAIABLE_MODULE);
		
		ListIterator<FactAddressValue> itr = pv.multifieldValue().listIterator();
		int len = pv.size();
		String code, name;
		while (len-- > 0 && itr.hasNext()) {
			FactAddressValue address = itr.next();
			try {
				if (address.getFactSlot("status").toString().equals("available")) {
					code = address.getFactSlot("code").toString().replace("\"", "");
					name = address.getFactSlot("name").toString().replace("\"", "");
					modules.add(new Module.Builder().setCode(code).setName(name).build());
				}
			} catch (Exception e) {
				System.out.println("Something went wrong!");
			}
		}
		
		return modules;
	}

	public void printFactsOnConsole() { clips.eval("(facts)"); }

	public void saveModules(ArrayList<Module> modules) {
		modules.forEach((module) -> clips.eval(ClipsParser.parseModuleIntoClips(module)));
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
