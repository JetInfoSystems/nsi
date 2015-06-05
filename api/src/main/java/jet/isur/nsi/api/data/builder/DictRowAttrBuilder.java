package jet.isur.nsi.api.data.builder;

import java.util.ArrayList;
import java.util.List;

import jet.isur.nsi.api.model.DictRowAttr;

public class DictRowAttrBuilder {


    private final DictRowBuilder owner;
    private DictRowAttr prototype;

    DictRowAttrBuilder(DictRowBuilder owner) {
        this.owner = owner;
    }

    public DictRowAttrBuilder value(String value) {
        prototype.getValues().add(value);
        return this;
    }

    public DictRowBuilder add() {
        if(owner.getAttr(prototype.getAttrName()) == null) {
            owner.getPrototype().getAttrs().add(prototype);
        }
        return owner;
    }

    void setPrototype(DictRowAttr prototype) {
        this.prototype = prototype;
        // когда приступаем к редактированию атрибута нужно сбросить предыдущее значение
        prototype.getValues().clear();
    }

    public static DictRowAttr from(String name, String value) {
        DictRowAttr result = new DictRowAttr();
        List<String> values = new ArrayList<>(1);
        result.setValues(values);
        values.add(value);
        result.setAttrName(name);
        return result;
    }

    public static DictRowAttr from(String name, long value) {
        return from(name,Long.toString(value));
    }


    public static DictRowAttr build(String name, List<String> values) {
        DictRowAttr result = new DictRowAttr();
        result.setAttrName(name);
        result.setValues(values);
        return result;
    }

    public static DictRowAttr build(String name, String value) {
        ArrayList<String> values = new ArrayList<>(1);
        values.add(value);
        return build(name, values);
    }
}
