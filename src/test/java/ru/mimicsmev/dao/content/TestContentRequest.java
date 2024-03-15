package ru.mimicsmev.dao.content;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@XmlRootElement(name = "TestContentRequest")
public class TestContentRequest {
    @XmlElement
    private long id;
    @XmlElement
    private String senderID;

    public TestContentRequest() {

    }

    public TestContentRequest(long id, String senderID) {
        this.id = id;
        this.senderID = senderID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestContentRequest that = (TestContentRequest) o;

        if (id != that.id) return false;
        return Objects.equals(senderID, that.senderID);
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (senderID != null ? senderID.hashCode() : 0);
        return result;
    }
}
