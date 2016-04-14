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
	private int startingSemester;
	
	public ModulePlan() {
		semesters = new ArrayList<Semester>();
	}
	
	public void setSemesters(int totalSemesters, int startingSemester) {
		this.startingSemester = startingSemester;
		for (int i = 0; i < totalSemesters - startingSemester + 1; i++) {
			semesters.add(new Semester());
		}
	}
	
	/**
	 * 
	 * @param module
	 * @param semester start from 1
	 */
	public void addNewModule(Module module, int semester) {
		semesters.get(semester - startingSemester).addModule(module);
	}
	
	public ArrayList<Semester> getSemesters() {
		return semesters;
	}

	public Semester getSemester(int semester) { return semesters.get(semester - startingSemester); }

	public Float[] getWorkloads(int semester) { return semesters.get(semester - startingSemester).getWorkloads(); }
}
