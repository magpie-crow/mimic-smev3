package ru.mimicsmev.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.config.annotation.WsConfigurationSupport;
import org.springframework.ws.server.endpoint.adapter.DefaultMethodEndpointAdapter;
import org.springframework.ws.server.endpoint.adapter.method.MarshallingPayloadMethodProcessor;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.XsdSchemaCollection;
import org.springframework.xml.xsd.commons.CommonsXsdSchemaCollection;

import java.util.Collections;

@Configuration
public class EndpointConfiguration extends WsConfigurationSupport {
    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatherServlet_v11(ApplicationContext applicationContext) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<MessageDispatcherServlet>(servlet, "/ws/*");
    }


    /*
    if need two dispatcher service and need another version
    @Bean(name = "service_v13")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchemaCollection schemaV3) {
        DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
        wsdl11Definition.setPortTypeName("service_v13");
        wsdl11Definition.setLocationUri("/ws");
        wsdl11Definition.setTargetNamespace("urn://x-artefacts-smev-gov-ru/services/message-exchange/types/1.3");
        wsdl11Definition.setSchemaCollection(schemaV3);
        return wsdl11Definition;
    }

    @Bean(name = "marshallerV3")
    public Jaxb2Marshaller marshallerVersion3() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("v3");
        marshaller.setMtomEnabled(true);
        return marshaller;
    }

    @Bean(name = "schemaV3")
    public XsdSchemaCollection schemaVersion3() {
        CommonsXsdSchemaCollection commonsXsdSchemaCollection = new CommonsXsdSchemaCollection(
                new ClassPathResource("schema/v3/smev-message-exchange-basic-1.3.xsd"),
                new ClassPathResource("schema/v3/smev-message-exchange-directive-1.3.xsd"),
                new ClassPathResource("schema/v3/smev-message-exchange-faults-1.3.xsd"),
                new ClassPathResource("schema/v3/smev-message-exchange-routing-1.3.xsd"),
                new ClassPathResource("schema/v3/smev-message-exchange-types-1.3.xsd")

        );
        commonsXsdSchemaCollection.setInline(true);
        return commonsXsdSchemaCollection;
    }

    @Bean(name = "methodProcessorV3")
    public MarshallingPayloadMethodProcessor methodProcessorVersion3(Jaxb2Marshaller marshallerV3) {
        return new MarshallingPayloadMethodProcessor(marshallerV3);
    }

    @Bean(name = "endpointAdapterV3")
    DefaultMethodEndpointAdapter endpointAdapterVersion3(MarshallingPayloadMethodProcessor methodProcessorV3) {
        DefaultMethodEndpointAdapter adapter = new DefaultMethodEndpointAdapter();
        adapter.setMethodArgumentResolvers(Collections.singletonList(methodProcessorV3));
        adapter.setMethodReturnValueHandlers(Collections.singletonList(methodProcessorV3));
        return adapter;
    }*/

    @Bean(name = "service")
    public DefaultWsdl11Definition defaultWsdl11Definition2(XsdSchemaCollection schemaV1) {
        DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
        wsdl11Definition.setPortTypeName("service");
        wsdl11Definition.setLocationUri("/ws");
        wsdl11Definition.setTargetNamespace("urn://x-artefacts-smev-gov-ru/services/message-exchange/types/1.1");
        wsdl11Definition.setSchemaCollection(schemaV1);
        return wsdl11Definition;
    }

    @Bean(name = "schema")
    public XsdSchemaCollection schemaVersion1() {
        CommonsXsdSchemaCollection commonsXsdSchemaCollection = new CommonsXsdSchemaCollection(
                new ClassPathResource("schema/v1/smev-message-exchange-basic-1.1.xsd"),
                new ClassPathResource("schema/v1/smev-message-exchange-faults-1.1.xsd"),
                new ClassPathResource("schema/v1/smev-message-exchange-types-1.1.xsd"));
        commonsXsdSchemaCollection.setInline(true);
        return commonsXsdSchemaCollection;
    }


    @Bean(name = "methodProcessor")
    public MarshallingPayloadMethodProcessor methodProcessorVersion1(Jaxb2Marshaller marshallerV1) {
        return new MarshallingPayloadMethodProcessor(marshallerV1);
    }
    @Bean(name = "marshaller")
    public Jaxb2Marshaller marshallerVersion1() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("v1");
        marshaller.setMtomEnabled(true);
        return marshaller;
    }

    @Bean(name = "endpointAdapter")
    DefaultMethodEndpointAdapter endpointAdapterVersion1(MarshallingPayloadMethodProcessor methodProcessorV1) {
        DefaultMethodEndpointAdapter adapter = new DefaultMethodEndpointAdapter();
        adapter.setMethodArgumentResolvers(Collections.singletonList(methodProcessorV1));
        adapter.setMethodReturnValueHandlers(Collections.singletonList(methodProcessorV1));
        return adapter;
    }

}
