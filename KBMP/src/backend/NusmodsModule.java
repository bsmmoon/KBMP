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
    public String ExamDate;
    public String ExamDuration;
    public boolean ExamOpenBook;
    public String ExamVenue;
    public NusmodsTimetable Timetable;
    public String[] LecturePeriods;

    public class NusmodsTimetable {
        public String ClassNo;
        public String LessonType;
        public String WeekText;
        public String DayText;
        public String StartTime;
        public String EndTime;
        public String Venue;
    }
}
