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

//    public int[] getWorkload() {
//        int[] workload = new int[5];
//        int index;
//        for (Module module : modules) {
//            index = 0;
//            for (Module.WorkloadTypes type : Module.WorkloadTypes.values()) {
//                workload[index] += module.getWorkload().get(type);
//            }
//        }
//
//        return workload;
//    }
}
