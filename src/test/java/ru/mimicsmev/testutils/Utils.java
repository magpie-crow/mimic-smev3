package ru.mimicsmev.testutils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import ru.mimicsmev.dao.content.TestContentRequest;
import ru.mimicsmev.exception.AppException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public class Utils {
    public static String marshalToString(Object object) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance("ru.mimicsmev.dao.content");
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        StringWriter sw = new StringWriter();
        m.marshal(object, sw);
        return sw.toString();
    }

    public static TestContentRequest unmarshal(String xml) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(TestContentRequest.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return (TestContentRequest) jaxbUnmarshaller.unmarshal(new StringReader(xml));
    }

    public static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    public static String nodeToString(Node node) throws AppException {
        StringWriter sw = new StringWriter();
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(node), new StreamResult(sw));
        } catch (TransformerException te) {
            throw new AppException("Failed convert to string");
        }
        return sw.toString();
    }

    public static Element createElementContent(String body) throws ParserConfigurationException, IOException, SAXException {
        Element element = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new ByteArrayInputStream(body.getBytes()))
                .getDocumentElement();
        element.normalize();
        return element;
    }
}
