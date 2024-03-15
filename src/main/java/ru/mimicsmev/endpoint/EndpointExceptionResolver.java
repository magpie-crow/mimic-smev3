package ru.mimicsmev.endpoint;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.AbstractEndpointExceptionResolver;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.SoapMessage;
import ru.mimicsmev.exception.SmevInvalidContentException;
import ru.mimicsmev.exception.SmevSignatureVerificationException;
import v1.InvalidContent;
import v1.ObjectFactory;
import v1.SignatureVerificationFault;

import javax.xml.transform.Result;
import java.util.Locale;

@Component
public class EndpointExceptionResolver extends AbstractEndpointExceptionResolver {
    private static final ObjectFactory FACTORY = new ObjectFactory();

    @Override
    @SneakyThrows
    protected boolean resolveExceptionInternal(MessageContext messageContext, Object o, Exception e) {

        final SoapMessage response = (SoapMessage) messageContext.getResponse();
        final SoapBody soapBody = response.getSoapBody();
        final SoapFault soapFault = soapBody.addClientOrSenderFault(e.getMessage(), Locale.ENGLISH);
        final SoapFaultDetail faultDetail = soapFault.addFaultDetail();
        final Result result = faultDetail.getResult();

        if (e instanceof SmevInvalidContentException) {
            return smevInvalidContentExceptionResolver((SmevInvalidContentException) e, result);
        }
        if (e instanceof SmevSignatureVerificationException) {
            return smevSignatureVerificationExceptionResolver((SmevSignatureVerificationException) e, result);
        }
        return false;
    }

    private boolean smevInvalidContentExceptionResolver(SmevInvalidContentException e, Result result) throws JAXBException {
        InvalidContent faultMessage = new InvalidContent();
        faultMessage.setCode("InvalidContent");
        faultMessage.setDescription(e.getMessage());
        JAXBContext jaxbContext = JAXBContext.newInstance(InvalidContent.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(FACTORY.createInvalidContent(faultMessage), result);
        return true;
    }

    private boolean smevSignatureVerificationExceptionResolver(SmevSignatureVerificationException e, Result result) throws JAXBException {
        SignatureVerificationFault faultMessage = new SignatureVerificationFault();
        faultMessage.setCode("SignatureVerificationFailed");
        faultMessage.setDescription(e.getCause().getMessage());
        JAXBContext jaxbContext = JAXBContext.newInstance(SignatureVerificationFault.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(FACTORY.createSignatureVerificationFault(faultMessage), result);
        return true;
    }
}
