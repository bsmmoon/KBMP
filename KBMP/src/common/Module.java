package common;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;

/**
 * @description This class contains all the information about a module
 * @author Joey
 *
 */
public class Module {
	private String code;
	private String name;
	private int credits = -1;
	private String department = "";
	private Hashtable<String, Integer> workload = new Hashtable<String, Integer>();
	private ArrayList<String> prerequisites = new ArrayList<String>();
	private ArrayList<String> corequisites = new ArrayList<String>();
	private ArrayList<String> exclusions = new ArrayList<String>();
	private ArrayList<Lesson> timetable = new ArrayList<Lesson>();
	private Calendar exam;

	public static class Builder {
		private String code;
		private String name;
		private int credits = -1;
		private String department = "";
		private Hashtable<String, Integer> workload = new Hashtable<String, Integer>();
		private ArrayList<String> prerequisites = new ArrayList<String>();
		private ArrayList<String> corequisites = new ArrayList<String>();
		private ArrayList<String> exclusions = new ArrayList<String>();
		private ArrayList<Lesson> timetable = new ArrayList<Lesson>();
		private Calendar exam;

		public Module build(){
			return new Module(this);
		}

		public Builder setCode(String code){
			this.code = code;
			return this;
		}

		public Builder setName(String name) {
			this.name = name;
			return this;
		}

		public Builder setCredits(int credits) {
			this.credits = credits;
			return this;
		}

		public Builder setDepartment(String department) {
			this.department = department;
			return this;
		}

		public Builder setWorkload(Hashtable<String, Integer> workload) {
			this.workload = workload;
			return this;
		}

		public Builder setPrerequisites(ArrayList<String> prerequisites) {
			this.prerequisites = prerequisites;
			return this;
		}

		public Builder setCorequisites(ArrayList<String> corequisites) {
			this.corequisites = corequisites;
			return this;
		}

		public Builder setExclusions(ArrayList<String> exclusions) {
			this.exclusions = exclusions;
			return this;
		}

		public Builder setTimetable(ArrayList<Lesson> timetable) {
			this.timetable = timetable;
			return this;
		}

		public Builder setExam(Calendar exam) {
			this.exam = exam;
			return this;
		}
	}

	public static Builder builder(){
		return new Builder();
	}

	private Module(Builder builder) {
		this.code = builder.code;
		this.name = builder.name;
		this.credits = builder.credits;
		this.department = builder.department;
		this.workload = builder.workload;
		this.prerequisites = builder.prerequisites;
		this.corequisites = builder.corequisites;
		this.exclusions = builder.exclusions;
		this.timetable = builder.timetable;
		this.exam = builder.exam;
	}

	public Module(String code, String name){
		this.code = code;
		this.name = name;
	}

	public String getCode() {
		return code;
	}
	
	public String getName() {
		return name;
	}

	public String getDepartment() {
		return department;
	}

	public int getCredits() {
		return credits;
	}

	public Hashtable<String, Integer> getWorkload() {
		return workload;
	}

	public Calendar getExam() {
		return exam;
	}

	public ArrayList<String> getPrerequisites() {
		return prerequisites;
	}

	public ArrayList<String> getCorequisites() {
		return corequisites;
	}

	public ArrayList<String> getExclusions() {
		return exclusions;
	}

	public ArrayList<Lesson> getTimetable() {
		return timetable;
	}

	public String toString() {
		return "[" + this.code + ", " + this.name + "]";
	}
}
