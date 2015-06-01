package jet.isur.nsi.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import jet.isur.nsi.api.model.MetaDictGetReq;
import jet.isur.nsi.api.model.MetaDictGetRes;
import jet.isur.nsi.api.model.MetaDictListReq;
import jet.isur.nsi.api.model.MetaDictListRes;

@Path("/nsi/meta")
public interface NsiMetaService {

    /**
     * Получить список коротких описаний справочников
     */
    @POST
    @Path("/dict/list")
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    MetaDictListRes metaDictList(MetaDictListReq req);

    /**
     * Получить полное описание справочника
     */
    @POST
    @Path("/dict/get")
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    MetaDictGetRes metaDictGet(MetaDictGetReq req);

}
