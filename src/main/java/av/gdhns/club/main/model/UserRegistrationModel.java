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
    private String photoLink, videoLink;
    private String email;

    public UserRegistrationModel() {
        this.createdAt = getCurrentTimestamp();
    }

    public UserRegistrationModel(String fullName, String phoneNumber, String email, String classId, String section, String admNo, String idea, String description, String projectType, String photoLink, String videoLink) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.classId = classId;
        this.section = section;
        this.admNo = admNo;
        this.idea = idea;
        this.description = description;
        this.projectType = projectType;
        this.photoLink = photoLink;
        this.videoLink = videoLink;
    }

    public UserRegistrationModel(String projectName, String phoneNumber, String email, String classId, String section, String admNo, String idea, String description, String projectType, String[] teamMembers, String photoLink, String videoLink) {
        this.fullName = projectName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.classId = classId;
        this.section = section;
        this.admNo = admNo;
        this.idea = idea;
        this.description = description;
        this.projectType = projectType;
        this.teamMembers = teamMembers;
        this.photoLink = photoLink;
        this.videoLink = videoLink;

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

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getPhotoLink() {
        return photoLink;
    }

    public void setPhotoLink(String photoLink) {
        this.photoLink = photoLink;
    }

    public String getVideoLink() {
        return videoLink;
    }

    public void setVideoLink(String videoLink) {
        this.videoLink = videoLink;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
