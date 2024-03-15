package ru.mimicsmev.service;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import v1.XMLDSigSignatureType;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public interface SignatureService {
    static XMLDSigSignatureType sigContent(Object xml) throws ParserConfigurationException, IOException, SAXException {
        String smevSig = "<ds:Signature xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\"><ds:SignedInfo><ds:CanonicalizationMethod Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"/><ds:SignatureMethod Algorithm=\"urn:ietf:params:xml:ns:cpxmlsec:algorithms:gostr34102012-gostr34112012-256\"/><ds:Reference URI=\"#SIGNED_BY_SMEV\"><ds:Transforms><ds:Transform Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"/><ds:Transform Algorithm=\"urn://smev-gov-ru/xmldsig/transform\"/></ds:Transforms><ds:DigestMethod Algorithm=\"urn:ietf:params:xml:ns:cpxmlsec:algorithms:gostr34112012-256\"/><ds:DigestValue>YBG0NmMgu</ds:DigestValue></ds:Reference></ds:SignedInfo><ds:SignatureValue>UfCvhthhjRHXLS0A==</ds:SignatureValue><ds:KeyInfo><ds:X509Data><ds:X509Certificate>Ymxhbms=</ds:X509Certificate></ds:X509Data></ds:KeyInfo></ds:Signature>";
        XMLDSigSignatureType xmldSigSignatureType = new XMLDSigSignatureType();
        Element element = DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(new ByteArrayInputStream(smevSig.getBytes()))
                .getDocumentElement();

        xmldSigSignatureType.setAny(element);
        return xmldSigSignatureType;
    }

    static byte[] sigAttach(byte[] attach) {
        String sig = "signature file is constant";
        return sig.getBytes();
    }

}
