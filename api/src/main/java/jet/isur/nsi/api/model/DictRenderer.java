package jet.isur.nsi.api.model;

import jet.isur.nsi.api.NsiServiceException;
import jet.isur.nsi.api.data.NsiConfigDict;

import java.util.HashMap;
import java.util.Map;

/**
 * Интерфейс рендерера объектов НСИ в их текстовое представление
 * @see jet.isur.nsi.api.model.DictRef
 */
public abstract class DictRenderer {

    private static final Map<String, DictRenderer> renderers = new HashMap<>();
    private static DictRenderer defaultRenderer;

    protected abstract String[] getDictNames();

    public abstract DictRef render(DictRow row, NsiConfigDict dictCfg);

    /**
     * Годится ли рендерер, чтобы быть дефолтным. Дефолтным станет последний инициализированный дефолтопригодный рендерер.
     * По умолчанию не годится.
     */
    protected boolean isDefault() {
        return false;
    }

    protected DictRenderer() {
        // регистрируем рендерер для каждого справочника, который он умеет рендерить
        for(String dictName: getDictNames()) {
            renderers.put(dictName, this);
        }
        if(isDefault()) {
            defaultRenderer = this;
        }
    }

    /**
     * Выдает инстанс рендерера для заданного справочника НСИ
     */
    public static DictRenderer getInstance(String dictName) throws NsiServiceException {
        DictRenderer renderer = renderers.get(dictName);
        if(renderer != null) {
            return renderer;
        } else if(defaultRenderer != null) {
            return defaultRenderer;
        } else {
            throw new NsiServiceException("no renderer for dictionary '%s' found", dictName);
        }
    }
}
