package common;

import java.util.ArrayList;

/**
 * @description This class represents a timetable, grouped by semesters
 * @author Ruofan
 *
 */
public class ModulePlan {
	// Each semester contains a list of modules
	private ArrayList<ArrayList<Module>> semesters;
	
	public ModulePlan() {
		semesters = new ArrayList<ArrayList<Module>>();
	}
	
	public void createNewSemester() {
		semesters.add(new ArrayList<Module> ());
	}
	
	/**
	 * 
	 * @param module
	 * @param semester start from 1
	 */
	public void addNewModule(Module module, int semester) {
		try {
			semesters.get(semester-1).add(module);
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Semester " + semester + " has not been added!");
			e.printStackTrace();
		}
	}
	
	public ArrayList<ArrayList<Module>> getModulePlan() {
		return semesters;
	}
}
