package backend;

/**
 * Created by Joey on 3/4/16.
 */
public class NusmodsModule {
    public String ModuleCode;
    public String ModuleTitle;
    public String Department;
    public String ModuleDescription;
    public int ModuleCredit;
    public String Workload;
    public String Prerequisite;
    public String Corequisite;
    public String Preclusion;
    public String ExamDate;
    public String[] Types; // module types, e.g. UEM
    public String ExamDuration;
    public boolean ExamOpenBook;
    public String ExamVenue;
    public NusmodsTimetable[] Timetable;
    public String[] LecturePeriods;

    public static class NusmodsTimetable {
        public String ClassNo;
        public String LessonType;
        public String WeekText;
        public String DayText;
        public String StartTime;
        public String EndTime;
        public String Venue;

        @Override
        public String toString() {
            return String.join(" ", this.ClassNo, this.LessonType, this.WeekText, this.DayText, this.StartTime, this
                    .EndTime, this.Venue);
        }
    }

    @Override
    public String toString() {
        return this.ModuleCode + " " + this.ModuleTitle + " by " + this.Department + "\n" + this.ModuleDescription +
                "\nModule credit: " + this.ModuleCredit + "\nWorkload: " + this.Workload + "\nPrequisite: " + this
                .Prerequisite + "\nCorequisite: " + this.Corequisite + "\nPreclusion: " + this.Preclusion + "\nExam " +
                "date: " + this.ExamDate + "\nExam duration: " + this.ExamDuration + "\nExam is open book: " + this
                .ExamOpenBook + "\nExam venue: " + this.ExamVenue + "\n\nTimetable: " + this.Timetable +
                "\n\nLecture periods: " + this.LecturePeriods;
    }
}
