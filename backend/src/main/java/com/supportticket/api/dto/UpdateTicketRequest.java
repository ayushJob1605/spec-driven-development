package com.supportticket.api.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.supportticket.domain.Priority;

public class UpdateTicketRequest {

    private String title;
    private boolean titlePresent;

    private String description;
    private boolean descriptionPresent;

    private Priority priority;
    private boolean priorityPresent;

    private String assignee;
    private boolean assigneePresent;

    public String getTitle() {
        return title;
    }

    @JsonSetter("title")
    public void setTitle(String title) {
        this.title = title;
        this.titlePresent = true;
    }

    public boolean isTitlePresent() {
        return titlePresent;
    }

    public String getDescription() {
        return description;
    }

    @JsonSetter("description")
    public void setDescription(String description) {
        this.description = description;
        this.descriptionPresent = true;
    }

    public boolean isDescriptionPresent() {
        return descriptionPresent;
    }

    public Priority getPriority() {
        return priority;
    }

    @JsonSetter("priority")
    public void setPriority(Priority priority) {
        this.priority = priority;
        this.priorityPresent = true;
    }

    public boolean isPriorityPresent() {
        return priorityPresent;
    }

    public String getAssignee() {
        return assignee;
    }

    @JsonSetter("assignee")
    public void setAssignee(String assignee) {
        this.assignee = assignee;
        this.assigneePresent = true;
    }

    public boolean isAssigneePresent() {
        return assigneePresent;
    }

    public boolean hasAnyField() {
        return titlePresent || descriptionPresent || priorityPresent || assigneePresent;
    }
}
