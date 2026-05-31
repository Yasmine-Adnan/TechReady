package com.example.quizapp_adnan.data.model;

import java.util.List;

public class ProfilingQuestion {
    private String id;
    private int order;
    private String parentQuestionId;
    private String parentAnswer;
    private String text;
    private List<String> options;
    private String fieldKey;
    private boolean multiSelect;

    // Constructeur vide requis par Firestore
    public ProfilingQuestion() {}

    public ProfilingQuestion(String id, int order, String parentQuestionId, String parentAnswer, String text, List<String> options, String fieldKey, boolean multiSelect) {
        this.id = id;
        this.order = order;
        this.parentQuestionId = parentQuestionId;
        this.parentAnswer = parentAnswer;
        this.text = text;
        this.options = options;
        this.fieldKey = fieldKey;
        this.multiSelect = multiSelect;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }

    public String getParentQuestionId() { return parentQuestionId; }
    public void setParentQuestionId(String parentQuestionId) { this.parentQuestionId = parentQuestionId; }

    public String getParentAnswer() { return parentAnswer; }
    public void setParentAnswer(String parentAnswer) { this.parentAnswer = parentAnswer; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public String getFieldKey() { return fieldKey; }
    public void setFieldKey(String fieldKey) { this.fieldKey = fieldKey; }

    public boolean isMultiSelect() { return multiSelect; }
    public void setMultiSelect(boolean multiSelect) { this.multiSelect = multiSelect; }
}
