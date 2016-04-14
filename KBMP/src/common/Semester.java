package common;

import java.util.ArrayList;

/**
 * Created by bsmmo on 10/4/2016.
 */
public class Semester {
    private ArrayList<Module> modules;
    int semester;

    public Semester(int semester) {
        this.semester = semester;
        modules = new ArrayList<>();
    }

    public void addModule(Module module) {
        modules.add(module);
    }

    public ArrayList<Module> getModules() { return modules; }

    public int[] getWorkloads() {
        int[] result = new int[]{0, 0, 0, 0, 0};

        int index;
        for (Module module : modules) {
            index = 0;
            for (Module.WorkloadTypes type : Module.WorkloadTypes.values()) {
                if (module.getWorkload().isEmpty()) break;
                result[index] += module.getWorkload().get(type);
                index++;
            }
        }
        return result;
    }

    public int getCredits() {
        int total = 0;

        for (Module module : modules) {
            total += module.getCredits();
        }

        return total;
    }

    public String getName() {
        // later handle special semester name here as well
        return "Year " + getYear() + " Semester " + getSemester() + "\n";
    }

    public String getSummary() {
        String out = "<html>";
        out += "Summary";
        out += "<br>Workloads: [";
        int[] workloads = getWorkloads();
        for (int i = 0; i < workloads.length; i++) {
            out += workloads[i];
            if (i < workloads.length - 1) out += ", ";
        }
        out += "]";
        return out;
    }

    private int getYear() { return semester % 2 == 0 ? Math.floorDiv(semester, 2) : Math.floorDiv(semester, 2) + 1; }
    private int getSemester() { return semester % 2 == 0 ? 2 : 1; }
}
