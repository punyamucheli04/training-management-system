package com.trainingmanagement.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * A row of the <b>department</b> table, plus two values computed with GROUP BY.
 *
 * JSON property names are exactly the MySQL column names (e.g. "Dept_ID"),
 * so the database, the REST API and the React code all use the same names.
 */
public class Department {

    @JsonProperty("Dept_ID")
    private Integer deptId;

    @JsonProperty("Dept_Name")
    private String deptName;

    @JsonProperty("HOD_Name")
    private String hodName;

    /** COUNT(student) - read only */
    @JsonProperty("Student_Count")
    private Integer studentCount;

    /** AVG(CGPA) - read only */
    @JsonProperty("Avg_CGPA")
    private BigDecimal avgCgpa;

    public Department() {
    }

    public Integer getDeptId() {
        return deptId;
    }

    public void setDeptId(Integer deptId) {
        this.deptId = deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getHodName() {
        return hodName;
    }

    public void setHodName(String hodName) {
        this.hodName = hodName;
    }

    public Integer getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
    }

    public BigDecimal getAvgCgpa() {
        return avgCgpa;
    }

    public void setAvgCgpa(BigDecimal avgCgpa) {
        this.avgCgpa = avgCgpa;
    }
}
