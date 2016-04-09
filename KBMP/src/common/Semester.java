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
}
