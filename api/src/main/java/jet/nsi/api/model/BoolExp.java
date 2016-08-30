package jet.nsi.api.model;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class BoolExp implements Serializable {
    
    private static final long serialVersionUID = 1L;
    /**
     * Название параметрa, соответствует атрибуту справочника.
     */
    private String key;
    /**
     * Функция предикат: =, <=, >=, <, > или логичекое условие: and, or, notAnd, notOr.
     */
    private String func;
    /**
     * Значение параметра, используется для обычных предикаторв.
     */
    private DictRowAttr value;
    @XmlElement(name="expList")
    private List<BoolExp> expList;
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public String getFunc() {
        return func;
    }
    public void setFunc(String func) {
        this.func = func;
    }
    public DictRowAttr getValue() {
        return value;
    }
    public void setValue(DictRowAttr value) {
        this.value = value;
    }
    public List<BoolExp> getExpList() {
        return expList;
    }
    public void setExpList(List<BoolExp> expList) {
        this.expList = expList;
    }

    @Override
    public String toString() {
        return "BoolExp [key=" + key + ", func=" + func + ", value=" + value
                + ", expList=" + expList + "]";
    }
}