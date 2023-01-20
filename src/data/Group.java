package data;

import java.util.Calendar;

public class Group implements java.io.Serializable{
    private String number;
    private String teacher;
    private Calendar date;

    public Group(String number, String teacher){
        this.number = number;
        this.teacher = teacher;
        this.date = Calendar.getInstance();
    }

    public String getNumber() {
        return number;
    }

    public String getTeacher() {
        return teacher;
    }

    public Calendar getDate() {
        return date;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object obj){
        if (obj == null){
            return false;
        }

        if (obj instanceof String){
            return obj.equals(this.getNumber());
        }

        if (!(obj instanceof Group)){
            return false;
        }

        Group compare = (Group) obj;
        return compare.getNumber().equals(this.getNumber());
    }
}