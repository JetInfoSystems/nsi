package jet.isur.nsi.api.test;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import jet.isur.nsi.api.model.Result;

import org.junit.Test;

public class TestModels {

    @Test
    public void testResult() throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(Result.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        Result v = new Result();
        v.setCode(1);
        v.setMessage("1");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        jaxbMarshaller.marshal(v, out);
        jaxbMarshaller.marshal(v, System.out);

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        Result r = (Result) jaxbUnmarshaller.unmarshal(new ByteArrayInputStream(out.toByteArray()));

        assertEquals(v.getCode(),r.getCode());
        assertEquals(v.getMessage(),r.getMessage());
    }

}
