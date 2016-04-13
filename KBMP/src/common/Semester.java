package common;

import java.util.ArrayList;

/**
 * Created by bsmmo on 10/4/2016.
 */
public class Semester {
    private ArrayList<Module> modules;

    public Semester() {
        modules = new ArrayList<>();
    }

    public void addModule(Module module) {
        modules.add(module);
    }

    public ArrayList<Module> getModules() { return modules; }

    public Float[] getWorkloads() {
        Float[] result = new Float[]{0f, 0f, 0f, 0f, 0f};

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

    public String getSummary() {
        String out = "";
        out += "[";
        Float[] workloads = getWorkloads();
        for (int i = 0; i < workloads.length; i++) {
            out += workloads[i];
            if (i < workloads.length - 1) out += ", ";
        }
        out += "]";
        return out;
    }
}
