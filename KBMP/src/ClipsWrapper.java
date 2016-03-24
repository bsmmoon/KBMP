import java.util.ArrayList;
import java.util.ListIterator;

import net.sf.clipsrules.jni.*;

import common.Module;

public class ClipsWrapper {
	private ClipsParser parser;
	
	private Environment clips;
	private boolean initialised = false;
	
	private static final String GET_ALL_AVAIABLE_MODULE = "(find-all-facts ((?f module)) TRUE)";
	
	public ClipsWrapper() {
		this.parser = new ClipsParser();
	}
	
	public void run(String command) {
		command = this.parser.parseStringIntoClips(command);
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
					modules.add(new Module(code, name));
				}
			} catch (Exception e) {
				System.out.println("Something went wrong!");
			}
		}
		
		return modules;
	}
	
	public void reset(String condition) {
		init();
		clips.loadFromString(condition);
		clips.run();
		clips.eval("(reset)");
	}
	
	public void init() {
		if (!initialised) clips = new Environment();
		else clips.reset();
	}
}
