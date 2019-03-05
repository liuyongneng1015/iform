package tech.ascs.icity.iform.api.model;

import io.swagger.annotations.ApiModel;
import tech.ascs.icity.model.NameEntity;

import java.util.ArrayList;
import java.util.List;

@ApiModel("应用关联的表单和列表")
public class AppListForm {
    private List<NameEntity> forms = new ArrayList<>();
    private List<NameEntity> lists = new ArrayList<>();
    public AppListForm() { }

    public List<NameEntity> getForms() {
        return forms;
    }

    public void setForms(List<NameEntity> forms) {
        this.forms = forms;
    }

    public List<NameEntity> getLists() {
        return lists;
    }

    public void setLists(List<NameEntity> lists) {
        this.lists = lists;
    }
}
