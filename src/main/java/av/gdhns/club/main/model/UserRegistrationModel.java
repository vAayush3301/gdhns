package av.gdhns.club.main.model;

public class UserRegistrationModel {
    private String fullName;
    private String classId;
    private String section;
    private String admNo;
    private String idea, description;
    private String projectType;
    private String[] teamMembers;

    public UserRegistrationModel(String projectType, String description, String idea, String admNo, String section, String classId, String fullName) {
        this.projectType = projectType;
        this.description = description;
        this.idea = idea;
        this.admNo = admNo;
        this.section = section;
        this.classId = classId;
        this.fullName = fullName;
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
}
