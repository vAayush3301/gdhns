package av.gdhns.club.main.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UserRegistrationModel {
    private String fullName;
    private String phoneNumber;
    private String classId;
    private String section;
    private String admNo;
    private String idea, description;
    private String projectType;
    private String[] teamMembers;
    private String createdAt;

    public UserRegistrationModel() {
    }

    public UserRegistrationModel(String fullName, String phoneNumber, String classId, String section, String admNo, String idea, String description, String projectType) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.classId = classId;
        this.section = section;
        this.admNo = admNo;
        this.idea = idea;
        this.description = description;
        this.projectType = projectType;
    }

    public UserRegistrationModel(String fullName, String classId, String section, String admNo, String idea, String description, String projectType, String[] teamMembers) {
        this.fullName = fullName;
        this.classId = classId;
        this.section = section;
        this.admNo = admNo;
        this.idea = idea;
        this.description = description;
        this.projectType = projectType;
        this.teamMembers = teamMembers;

        this.createdAt = getCurrentTimestamp();
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getAdmNo() {
        return admNo;
    }

    public void setAdmNo(String admNo) {
        this.admNo = admNo;
    }

    public String getIdea() {
        return idea;
    }

    public void setIdea(String idea) {
        this.idea = idea;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public String[] getTeamMembers() {
        return teamMembers;
    }

    public void setTeamMembers(String[] teamMembers) {
        this.teamMembers = teamMembers;
    }

    @Override
    public String toString() {
        return fullName;
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
