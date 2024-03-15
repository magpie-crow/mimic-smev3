package ru.mimicsmev.endpoint;

import jakarta.xml.bind.JAXBElement;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.Namespace;
import org.springframework.ws.server.endpoint.annotation.Namespaces;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import ru.mimicsmev.exception.SmevInvalidContentException;
import ru.mimicsmev.service.creator.GetRequestCreator;
import ru.mimicsmev.service.creator.GetResponseCreator;
import ru.mimicsmev.service.creator.SendRequestCreator;
import ru.mimicsmev.service.creator.SendResponseCreator;
import v1.AckRequest;
import v1.GetRequestRequest;
import v1.GetRequestResponse;
import v1.GetResponseRequest;
import v1.GetResponseResponse;
import v1.ObjectFactory;
import v1.SendRequestRequest;
import v1.SendRequestResponse;
import v1.SendResponseRequest;
import v1.SendResponseResponse;
import v1.Void;

@Endpoint
public class SmevEndpointService {

    public static final String NAMESPACE_URI_TYPES = "urn://x-artefacts-smev-gov-ru/services/message-exchange/types/1.1";
    public static final String NAMESPACE_URI_BASIC = "urn://x-artefacts-smev-gov-ru/services/message-exchange/types/basic/1.1";
    public static final String NAMESPACE_URI_FAULTS = "urn://x-artefacts-smev-gov-ru/services/message-exchange/types/faults/1.1";

    private final SendResponseCreator sendResponseCreator;
    private final GetResponseCreator getResponseCreator;
    private final GetRequestCreator getRequestCreator;
    private final SendRequestCreator sendRequestCreator;

    SmevEndpointService(SendResponseCreator sendResponseCreator, GetResponseCreator getResponseCreator, GetRequestCreator getRequestCreator, SendRequestCreator sendRequestCreator) {
        this.sendResponseCreator = sendResponseCreator;
        this.getResponseCreator = getResponseCreator;
        this.getRequestCreator = getRequestCreator;
        this.sendRequestCreator = sendRequestCreator;
    }

    @PayloadRoot(localPart = "SendRequestRequest", namespace = NAMESPACE_URI_TYPES)
    @Namespaces(value = {
            @Namespace(prefix = "ns", uri = NAMESPACE_URI_TYPES),
            @Namespace(prefix = "ns2", uri = NAMESPACE_URI_BASIC),
            @Namespace(prefix = "ns3", uri = NAMESPACE_URI_FAULTS)
    })
    @ResponsePayload
    public SendRequestResponse sendRequest(@RequestPayload SendRequestRequest parameters) throws SmevInvalidContentException {
        return sendRequestCreator.createResponse(parameters);
    }

    @PayloadRoot(localPart = "GetRequestRequest", namespace = NAMESPACE_URI_TYPES)
    @Namespaces(value = {
            @Namespace(prefix = "ns", uri = NAMESPACE_URI_TYPES),
            @Namespace(prefix = "ns2", uri = NAMESPACE_URI_BASIC),
            @Namespace(prefix = "ns3", uri = NAMESPACE_URI_FAULTS)
    })
    @ResponsePayload
    public GetRequestResponse getRequestResponse(@RequestPayload GetRequestRequest getRequestRequest) throws SmevInvalidContentException {
        return getRequestCreator.createResponse(getRequestRequest);

    }

    @PayloadRoot(localPart = "GetResponseRequest", namespace = NAMESPACE_URI_TYPES)
    @Namespaces(value = {
            @Namespace(prefix = "ns", uri = NAMESPACE_URI_TYPES),
            @Namespace(prefix = "ns2", uri = NAMESPACE_URI_BASIC),
            @Namespace(prefix = "ns3", uri = NAMESPACE_URI_FAULTS)
    })
    @ResponsePayload
    public GetResponseResponse getResponseResponse(@RequestPayload GetResponseRequest getResponseRequest) throws SmevInvalidContentException {
        return getResponseCreator.createResponse(getResponseRequest);
    }


    @PayloadRoot(localPart = "AckRequest", namespace = NAMESPACE_URI_TYPES)
    @Namespaces(value = {
            @Namespace(prefix = "ns", uri = NAMESPACE_URI_TYPES),
            @Namespace(prefix = "ns2", uri = NAMESPACE_URI_BASIC),
            @Namespace(prefix = "ns3", uri = NAMESPACE_URI_FAULTS)
    })
    @ResponsePayload
    public JAXBElement ack(@RequestPayload AckRequest ackRequest) {
        ObjectFactory o = new ObjectFactory();
        return o.createAckResponse(new Void());
    }

    @PayloadRoot(localPart = "SendResponseRequest", namespace = NAMESPACE_URI_TYPES)
    @Namespaces(value = {
            @Namespace(prefix = "ns", uri = NAMESPACE_URI_TYPES),
            @Namespace(prefix = "ns2", uri = NAMESPACE_URI_BASIC),
            @Namespace(prefix = "ns3", uri = NAMESPACE_URI_FAULTS)
    })
    @ResponsePayload
    public SendResponseResponse sendResponseResponse(@RequestPayload SendResponseRequest sendResponseRequestre) throws SmevInvalidContentException {
        return sendResponseCreator.createResponse(sendResponseRequestre);
    }
}
