package common;

import java.util.ArrayList;

/**
 * Created by bsmmo on 11/4/2016.
 */
public class AvailableModule implements Comparable {
    private Module module;
    private String code;
    private int score;
    private ArrayList<String> reasonings;

    public AvailableModule(String code, int score) {
        this.code = code;
        this.score = score;
    }

    public void setModule(Module module) { this.module = module; }

    public Module getModule() { return module; }
    public String getCode() { return code; }
    public int getScore() { return score; }
    public ArrayList<String> getReasonings() { return reasonings; }

    @Override
    public int compareTo(Object o) {
        AvailableModule other = (AvailableModule) o;
        if (score != other.score) return other.score - score;
        else return code.compareTo(other.code);
    }
}
