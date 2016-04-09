package common;

import java.util.ArrayList;

/**
 * @description This class represents a timetable, grouped by semesters
 * @author Ruofan
 *
 */
public class ModulePlan {
	// Each semester contains a list of modules
	private ArrayList<Semester> semesters;
	
	public ModulePlan() {
		semesters = new ArrayList<Semester>();
	}
	
	public void setSemesters(int numberOfSemesters) {
		for (int i = 0; i < numberOfSemesters; i++) {
			semesters.add(new Semester());
		}
	}
	
	/**
	 * 
	 * @param module
	 * @param semester start from 1
	 */
	public void addNewModule(Module module, int semester) {
		semesters.get(semester-1).addModule(module);
	}
	
	public ArrayList<Semester> getSemesters() {
		return semesters;
	}
}
