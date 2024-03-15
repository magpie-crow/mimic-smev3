package ru.mimicsmev.dao.content;

import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;

import javax.xml.namespace.QName;

@XmlRegistry
public class ObjectFactory {
    public ObjectFactory() {
    }

    TestContentRequest createTestContentRequest() {
        return new TestContentRequest();
    }
}
