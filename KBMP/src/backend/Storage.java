package backend;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;

import common.Module;
import common.FocusArea;

public class Storage {
	private String CONDITION_DIR = "clips/planner.clp";
	private String MODULES_FILE_LOCATION = "data/availableModules.txt";
	private String RAW_MODULES_SEM1_FILE_LOCATION = "data/AY1516_S1_modules.json";
	private String RAW_MODULES_SEM2_FILE_LOCATION = "data/AY1516_S2_modules.json";
	private String RAW_FOCUS_AREA_LOCATION = "data/FocusArea.json";


	public String readCondition() {
		String condition = "";
		try {
			condition = new String(Files.readAllBytes(Paths.get(CONDITION_DIR)));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return condition;
	}
	
	public ArrayList<Module> readModules() throws IOException {
		ArrayList<Module> modules;
		try {
			// if file containing collated modules exists, just populate from file
			modules = readModulesFromFile();
			System.out.println(modules.size() + " modules successfully read from file.");
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
				modules = new ArrayList<>(allModules.values());
			} catch (IOException ioe) {
				ioe.printStackTrace();
				throw new FileNotFoundException("Neither collated nor raw modules' files found.");
			}

			try {
				writeModulesToFile(modules);
				System.out.println(modules.size() + " collated modules successfully written to file.");
			} catch (IOException ioe) {
				ioe.printStackTrace();
				throw new IOException("Collated modules file could not be written.");
			}
		}

		return modules;
	}

	// pseudo json parser to save time :p
	public ArrayList<FocusArea> readFocusArea() throws IOException {
		String json = new String(Files.readAllBytes(Paths.get(RAW_FOCUS_AREA_LOCATION)));;
		String[] list = new String[]{"{", ":", "[", "]", "}", ","};
		for (String delimeter : list) json = json.replace(delimeter, ";");
		json = json.replace("\"", "");
		json = json.replace(";;Primaries;;", ";!;");
		json = json.replace(";;Electives;;", ";!;");
		json = json.replace(";;Unrestricted Electives;;", ";!;");
		while (json.contains(";;;")) json = json.replace(";;;", ";;");
		json = json.replace(";;", ";!;");

		String[] arr = json.split(";");
		int phase = 0;

		ArrayList<FocusArea> focusArea = new ArrayList<>();
		FocusArea focus = new FocusArea();
		for (String line : arr) {
			if (line.isEmpty()) continue;
			switch (phase) {
				case 0: // Focus Area
					if (line.equals("!")) phase = 1;
					else focus.setName(line);
					break;
				case 1: // Primaries
					if (line.equals("!")) phase = 2;
					else focus.addPrimaries(line);
					break;
				case 2: // Electives
					if (line.equals("!")) phase = 3;
					else focus.addElectives(line);
					break;
				case 3: // Unrestricted Electives
					if (line.equals("!")) {
						phase = 0;
						focusArea.add(focus);
						focus = new FocusArea();
					}
					else focus.addUnrestrictedElectives(line);
					break;
			}
		}

		return focusArea;
	}

	private void writeModulesToFile(ArrayList<Module> modules) throws IOException {
		FileOutputStream fos = new FileOutputStream(this.MODULES_FILE_LOCATION);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(modules);
	}

	private ArrayList<Module> readModulesFromFile() throws IOException, ClassNotFoundException {
		FileInputStream fis = new FileInputStream(this.MODULES_FILE_LOCATION);
		ObjectInputStream ois = new ObjectInputStream(fis);
		return (ArrayList<Module>) ois.readObject();
	}

}
