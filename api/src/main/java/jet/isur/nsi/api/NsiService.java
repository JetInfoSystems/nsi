package jet.isur.nsi.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import jet.isur.nsi.api.model.DictCountReq;
import jet.isur.nsi.api.model.DictCountRes;
import jet.isur.nsi.api.model.DictDeleteReq;
import jet.isur.nsi.api.model.DictDeleteRes;
import jet.isur.nsi.api.model.DictGetReq;
import jet.isur.nsi.api.model.DictGetRes;
import jet.isur.nsi.api.model.DictListReq;
import jet.isur.nsi.api.model.DictListRes;
import jet.isur.nsi.api.model.DictSaveReq;
import jet.isur.nsi.api.model.DictSaveRes;

@Path("/nsi")
public interface NsiService {

    /**
     * Получить количество записей справочника соответствующих заданному условию
     */
    @POST
    @Path("/nsi/dict/count")
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    DictCountRes dictCount(DictCountReq req);

    /**
     * Получить страницу списка записей справочника соответствующих заданному условию
     */
    @POST
    @Path("/dict/list")
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    DictListRes dictList(DictListReq req);

    /**
     * Получить полное состояние строки справочника, со всеми атрибутами
     */
    @POST
    @Path("/dict/get")
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    DictGetRes dictGet(DictGetReq req);

    /**
     * Сохранить состояние записи справочника, если ид атрибут задан то обновление, если нет то создание
     */
    @POST
    @Path("/dict/save")
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    DictSaveRes dictSave(DictSaveReq req);

    /**
     * Изменить отметку о удалении для заданной записи справочника
     */
    @POST
    @Path("/dict/delete")
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    DictDeleteRes dictDelete(DictDeleteReq req);

}
