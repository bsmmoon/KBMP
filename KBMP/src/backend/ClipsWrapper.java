package backend;

import java.util.ArrayList;
import java.util.ListIterator;

import net.sf.clipsrules.jni.*;

import common.Module;

public class ClipsWrapper {
	private ClipsParser parser;
	
	private Environment clips;
	private boolean initialised;
	
	private static final String GET_ALL_AVAIABLE_MODULE = "(find-all-facts ((?f module)) TRUE)";
	
	public ClipsWrapper() {
		this.initialised = false;
		this.parser = new ClipsParser();
	}
	
	public void execute(String command) {
		command = parser.parseStringIntoClips(command);
		clips.eval(command);
	}
	
	public ArrayList<Module> getAvailableModules() {
		ArrayList<Module> modules = new ArrayList<Module>();
		
		clips.eval("(assert (flag (type return)))");
		MultifieldValue pv = (MultifieldValue) clips.eval(GET_ALL_AVAIABLE_MODULE);
		
		ListIterator<FactAddressValue> itr = pv.multifieldValue().listIterator();
		int len = pv.size();
		String code, name;
		while (len-- > 0 && itr.hasNext()) {
			FactAddressValue address = itr.next();
			try {
				if (address.getFactSlot("status").toString().equals("available")) {
					code = address.getFactSlot("code").toString();
					name = address.getFactSlot("name").toString();
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
		modules.forEach((module) -> clips.eval(parseModuleIntoClips(module)));
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

	private String parseModuleIntoClips(Module module) {
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
		out += "))";

		System.out.println(out);
		return out;
	}
}
