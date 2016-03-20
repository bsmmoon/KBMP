package common;

import java.util.ArrayList;

/**
 * @description This class represents a timetable, grouped by semesters
 * @author Ruofan
 *
 */
public class ModulePlan {
	// Each semester contains a list of modules
	ArrayList<ArrayList<Module>> semesters;
	
	public ModulePlan() {
		semesters = new ArrayList<ArrayList<Module>>();
	}
	
	public void createNewSemester() {
		semesters.add(new ArrayList<Module> ());
	}
	
	public void addNewModule(Module module, int semester) {
		try {
			
		} catch (ArrayIndexOutOfBoundsException e) {
			
		}
	}
}
