package jet.isur.nsi.api.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="result")
public class Result implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * Код ошибки.
     */
    private Integer code;
    /**
     * Сообщение о ошибке.
     */
    private String message;
    public Integer getCode() {
        return code;
    }
    public void setCode(Integer code) {
        this.code = code;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

}
