package common;

import java.io.Serializable;
import java.sql.Time;

/**
 * Created by Joey on 28/3/16.
 */
public class Lesson implements Serializable {
    public enum Day {MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY}
    public enum Type {LECTURE, TUTORIAL, LABORATORY, SECTIONAL, RECITATION}
    public enum Frequency {EVERY, ODD, EVEN}
    private String name;
    private Type type;
    private Frequency frequency;
    private Time start;
    private Day day;

    public static class Builder{
        private String name;
        private Type type;
        private Frequency frequency;
        private Time start;
        private Day day;

        public Lesson build(){
            return new Lesson(this);
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setType(Type type) {
            this.type = type;
            return this;
        }

        public Builder setFrequency(Frequency frequency) {
            this.frequency = frequency;
            return this;
        }

        public Builder setStart(Time start) {
            this.start = start;
            return this;
        }

        public Builder setDay(Day day) {
            this.day = day;
            return this;
        }
    }

    public static Builder builder(){
        return new Builder();
    }

    public Lesson(Builder builder){
        this.name = builder.name;
        this.type = builder.type;
        this.frequency = builder.frequency;
        this.start = builder.start;
        this.day = builder.day;
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public Time getStart() {
        return start;
    }

    public Day getDay() {
        return day;
    }
}
