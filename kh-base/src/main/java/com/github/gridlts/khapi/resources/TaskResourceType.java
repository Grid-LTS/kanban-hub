package com.github.gridlts.khapi.resources;

public enum TaskResourceType {

    GOOGLE_TASKS("google_tasks"),

    TASKWARRIOR("taskwarrior");

    private String resourceType;

    TaskResourceType(String resourceType){
        this.resourceType = resourceType;
    }

    @Override
    public String toString() {
        return this.resourceType;
    }
}
