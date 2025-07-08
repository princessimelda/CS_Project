package NotificationSystem;

public class ReminderPreferences {
    private String studentId;
    private String preferredTime; // Stored in "HH:mm" format
    private int frequencyHours;
    private int hoursBeforePriority;

    public ReminderPreferences(String studentId, String preferredTime, int frequencyHours, int hoursBeforePriority) {
        this.studentId = studentId;
        this.preferredTime = preferredTime;
        this.frequencyHours = frequencyHours;
        this.hoursBeforePriority = hoursBeforePriority;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getPreferredTime() {
        return preferredTime;
    }

    public void setPreferredTime(String preferredTime) {
        this.preferredTime = preferredTime;
    }

    public int getFrequencyHours() {
        return frequencyHours;
    }

    public void setFrequencyHours(int frequencyHours) {
        this.frequencyHours = frequencyHours;
    }

    public int getHoursBeforePriority() {
        return hoursBeforePriority;
    }

    public void setHoursBeforePriority(int hoursBeforePriority) {
        this.hoursBeforePriority = hoursBeforePriority;
    }
}

