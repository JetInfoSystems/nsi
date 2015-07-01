package jet.isur.nsi.api.model;

/**
 * DTO, использующийся вместо ссылки. Когда на фротненд возвращается, например, список каких-то объектов, которые при
 * этом ссылаются на другие объекты, данный DTO может использоваться в качестве ссылки вместо ID, чтобы фронтенд не
 * запрашивал у бэкенда каждый объект, а мог сразу показать его текстовое представление, используя поле DictRef.text.
 * Рендеринг текстового представления объектов в DictRef.text должен происходить на бэкенде. Например, для сотрудников
 * это может быть конкатенация их ФИО.
 * @see DictRenderer
 */
public class DictRef {

    private String id;
    private String text;

    public DictRef() {
    }

    public DictRef(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
