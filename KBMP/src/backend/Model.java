package backend;/*
 * Facet
 */

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;

import common.Module;
import common.ModulePlan;

public class Model {
	private ClipsWrapper clips;
	private ArrayList<Module> availableModules;
	private ModulePlan plan;
	private String MODULES_FILE_LOCATION = "data/availableModules.txt";
	private String RAW_MODULES_SEM1_FILE_LOCATION = "data/AY1516_S1_modules.json";
	private String RAW_MODULES_SEM2_FILE_LOCATION = "data/AY1516_S2_modules.json";
	private int numberOfSemesterLeft;
	private int semester;

	public Model(ClipsWrapper clips) {
		this.clips = clips;
		this.plan = new ModulePlan();
		this.plan.createNewSemester();
		this.semester = 1;

		try {
			this.populateModules();
		} catch (Exception e) {
			e.printStackTrace();
			availableModules = new ArrayList<>();
		}
	}

	public ModulePlan getModulePlan() { return plan; }

	public ArrayList<Module> getAvailableModules() { return availableModules; }

	public int getSemester() { return semester; }

	public void setNumberOfSemesterLeft(int semester) { this.numberOfSemesterLeft = semester; }

	public void update() { availableModules = clips.getAvailableModules(); }

	private void populateModules() throws IOException {
		try {
			// if file containing collated modules exists, just populate from file
			this.availableModules = readModulesFromFile();
			System.out.println(this.availableModules.size() + " modules successfully read from file.");
		} catch (Exception e) {
			// else if raw files exist, generate collated modules
			Hashtable<String, Module> allModules;
			Path sem1ModulesJson = Paths.get(RAW_MODULES_SEM1_FILE_LOCATION);
			Path sem2ModulesJson = Paths.get(RAW_MODULES_SEM2_FILE_LOCATION);
			try {
				allModules = ModulesParser.updateModulesFromPath(sem1ModulesJson, new Hashtable<>(),
						Module.Semester.ONE);
				allModules = ModulesParser.updateModulesFromPath(sem2ModulesJson, allModules,
						Module.Semester.TWO);
				this.availableModules = new ArrayList<>(allModules.values());
			} catch (IOException ioe) {
				ioe.printStackTrace();
				throw new FileNotFoundException("Neither collated nor raw modules' files found.");
			}

			try {
				writeModulesToFile();
				System.out.println(this.availableModules.size() + " collated modules successfully written to file.");
			} catch (IOException ioe) {
				ioe.printStackTrace();
				throw new IOException("Collated modules file could not be written.");
			}
		}

	}

	private void writeModulesToFile() throws IOException {
		FileOutputStream fos = new FileOutputStream(this.MODULES_FILE_LOCATION);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(this.availableModules);
	}

	private ArrayList<Module> readModulesFromFile() throws IOException, ClassNotFoundException {
		FileInputStream fis = new FileInputStream(this.MODULES_FILE_LOCATION);
		ObjectInputStream ois = new ObjectInputStream(fis);
		return (ArrayList<Module>) ois.readObject();
	}

	public void updatePlan(ArrayList<Module> modules) {
		modules.forEach((module) -> plan.addNewModule(module, semester));
	}

	public boolean isDone() {
		return semester == numberOfSemesterLeft;
	}
}
