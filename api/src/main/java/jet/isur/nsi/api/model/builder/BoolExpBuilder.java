package jet.isur.nsi.api.model.builder;

import java.util.ArrayList;
import java.util.List;

import jet.isur.nsi.api.model.BoolExp;
import jet.isur.nsi.api.model.DictRowAttr;

public class BoolExpBuilder {

    private final BoolExpBuilder owner;
    private final boolean root;
    private BoolExp prototype;

    public BoolExpBuilder() {
        this(null,true);
    }

    BoolExpBuilder(BoolExpBuilder owner, boolean root) {
        this.owner = owner;
        this.root = root;
    }

    public BoolExp getPrototype() {
        if(prototype == null) {
            prototype = new BoolExp();
        }
        return prototype;
    }

    public List<BoolExp> getExpList() {
        if(getPrototype().getExpList() == null) {
            getPrototype().setExpList(new ArrayList<BoolExp>());
        }
        return getPrototype().getExpList();
    }

    public BoolExpBuilder key(String key) {
        getPrototype().setKey(key);
        return this;
    }

    public BoolExpBuilder func(String func) {
        getPrototype().setFunc(func);
        return this;
    }

    public BoolExpBuilder value(DictRowAttr value) {
        getPrototype().setValue(value);
        return this;
    }

    public BoolExpBuilder expList() {
        getPrototype().setExpList(new ArrayList<BoolExp>());
        return new BoolExpBuilder(this, false);
    }

    public BoolExpBuilder add() {
        owner.getPrototype().getExpList().add(getPrototype());
        prototype = null;
        return this;
    }

    public BoolExpBuilder end() {
        return owner;
    }

    public BoolExp build() {
        return getPrototype();
    }

    public BoolExpBuilder add(BoolExp filter) {
        owner.getPrototype().getExpList().add(filter);
        prototype = null;
        return this;
    }

}
