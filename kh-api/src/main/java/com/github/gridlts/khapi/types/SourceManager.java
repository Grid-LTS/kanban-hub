package com.github.gridlts.khapi.types;

public enum SourceManager {

    GOOGLE_TASKS("google_tasks"),

    TASKWARRIOR("taskwarrior");

    private String managerType;

    SourceManager(String managerType){
        this.managerType = managerType;
    }

    @Override
    public String toString() {
        return this.managerType;
    }
}
