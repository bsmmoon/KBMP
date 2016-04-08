package common;

import java.util.ArrayList;

/**
 * Created by bsmmo on 8/4/2016.
 */
public class FocusArea {
    private String name;
    private ArrayList<String> primaries;
    private ArrayList<String> electives;
    private ArrayList<String> unrestrictedElectives;

    public FocusArea() {
        this.primaries = new ArrayList<>();
        this.electives = new ArrayList<>();
        this.unrestrictedElectives = new ArrayList<>();
    }

    public void setName(String name) { this.name = name; }
    public void addPrimaries(String primary) { this.primaries.add(primary); }
    public void addElectives(String elective) { this.electives.add(elective); }
    public void addUnrestrictedElectives(String unrestrictedElective) { this.unrestrictedElectives.add(unrestrictedElective); }

    public String getName() { return name; }
    public ArrayList<String> getPrimaries() { return primaries; }
    public ArrayList<String> getElectives() { return electives; }
    public ArrayList<String> getUnrestrictedElectives() { return unrestrictedElectives; }
}
