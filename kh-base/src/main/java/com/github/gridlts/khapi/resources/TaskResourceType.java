package com.github.gridlts.khapi.resources;

import org.apache.commons.collections4.map.HashedMap;

import java.util.Map;

public enum TaskResourceType {

    GOOGLE_TASKS("google_tasks"),

    TASKWARRIOR("taskwarrior");

    private String resourceType;
    public static Map<String, TaskResourceType> mapping = new HashedMap<>();

    static {
        mapping.put(GOOGLE_TASKS.toString(), GOOGLE_TASKS);
        mapping.put(TASKWARRIOR.toString(), TASKWARRIOR);

    }

    TaskResourceType(String resourceType){
        this.resourceType = resourceType;
    }

    public static TaskResourceType getResourceType(String resource) {
        if (mapping.get(resource) == null) {
            throw new RuntimeException(String.format("There is no resource mapping with name (%s)"));
        }
        return mapping.get(resource);
    }

    @Override
    public String toString() {
        return this.resourceType;
    }
}
