package ru.mimicsmev.service;

import jakarta.xml.bind.JAXBException;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import ru.mimicsmev.dao.entity.ReqAttachment;
import ru.mimicsmev.dao.entity.ReqType;
import ru.mimicsmev.dao.entity.VsList;
import ru.mimicsmev.dao.mapper.AttachmentMapper;
import ru.mimicsmev.exception.AppException;
import ru.mimicsmev.exception.SmevInvalidContentException;
import v1.AttachmentContentType;
import v1.InteractionStatusType;
import v1.InteractionTypeType;
import v1.MessageMetadata;
import v1.MessageTypeType;
import v1.XMLDSigSignatureType;

import javax.xml.datatype.DatatypeFactory;
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
import java.io.StringWriter;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class AbstractSmevResponseCreator<T, S> {

    public abstract T createResponse(S request) throws SmevInvalidContentException;

    private final Jaxb2Marshaller jaxb2Marshaller;

    protected AbstractSmevResponseCreator(Jaxb2Marshaller jaxb2Marshaller) {
        this.jaxb2Marshaller = jaxb2Marshaller;
    }

    protected XMLDSigSignatureType sigXmlContent(Object content) throws ParserConfigurationException, IOException, SAXException {
        return SignatureService.sigContent(content);
    }

    public String marshalToString(Object object) throws JAXBException {
        StringWriter sw = new StringWriter();
        jaxb2Marshaller.createMarshaller().marshal(object, sw);
        return sw.getBuffer().toString();
    }

    protected MessageMetadata createMetadata(VsList senderMnemonic, VsList recipientMnemonic, String msgId) {
        MessageMetadata messageMetadata = new MessageMetadata();
        messageMetadata.setMessageId(msgId);
        messageMetadata.setMessageType(MessageTypeType.REQUEST);
        messageMetadata.setSendingTimestamp(DatatypeFactory.newDefaultInstance().newXMLGregorianCalendar(new GregorianCalendar()));
        messageMetadata.setDeliveryTimestamp(DatatypeFactory.newDefaultInstance().newXMLGregorianCalendar(new GregorianCalendar()));
        messageMetadata.setDestinationName("unknown");

        if (Objects.nonNull(senderMnemonic)) {
            MessageMetadata.Sender sender = new MessageMetadata.Sender();
            sender.setMnemonic(senderMnemonic.getMnemonic());
            sender.setHumanReadableName(senderMnemonic.getMnemonicDesc());
            messageMetadata.setSender(sender);
        }

        if (Objects.nonNull(recipientMnemonic)) {
            MessageMetadata.Recipient recipient = new MessageMetadata.Recipient();
            recipient.setMnemonic(recipientMnemonic.getMnemonic());
            recipient.setHumanReadableName(recipientMnemonic.getMnemonicDesc());
            messageMetadata.setRecipient(recipient);
        }

        MessageMetadata.SupplementaryData supplementaryData = new MessageMetadata.SupplementaryData();
        supplementaryData.setDetectedContentTypeName("not detected");
        supplementaryData.setInteractionType(InteractionTypeType.NOT_DETECTED);
        messageMetadata.setSupplementaryData(supplementaryData);
        messageMetadata.setStatus(InteractionStatusType.RESPONSE_IS_DELIVERED);

        return messageMetadata;
    }

    protected List<ReqAttachment> map(List<AttachmentContentType> attachmentContentTypeList, Long refId, ReqType reqType) {
        return attachmentContentTypeList.stream().map(
                attachmentContentType -> AttachmentMapper.INSTANCE.map(attachmentContentType, refId, reqType)
        ).collect(Collectors.toList());
    }

    protected AttachmentContentType map(ReqAttachment reqAttachment) {
        return AttachmentMapper.INSTANCE.map(reqAttachment);
    }

    protected Element createElementContent(String body) throws ParserConfigurationException, IOException, SAXException {
        Element element = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new ByteArrayInputStream(body.getBytes()))
                .getDocumentElement();
        element.normalize();
        return element;
    }

    protected static String nodeToString(Node node) throws AppException {
        StringWriter sw = new StringWriter();
        node.normalize();
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            transformer.transform(new DOMSource(node), new StreamResult(sw));
        } catch (TransformerException te) {
            throw new AppException("Failed convert to string");
        }
        return sw.toString();
    }

}
