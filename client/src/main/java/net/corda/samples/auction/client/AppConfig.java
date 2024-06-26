package net.corda.samples.auction.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.corda.client.jackson.JacksonSupport;
import net.corda.client.rpc.CordaRPCClient;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.utilities.NetworkHostAndPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppConfig implements WebMvcConfigurer {

    @Value("${powerCompany.host}")
    private String powerCompanyHostAndPort;

    @Value("${gridAuthority.host}")
    private String gridAuthorityHostAndPort;

    @Value("${prosumer.host}")
    private String prosumerHostAndPort;

    @Value("${customer.host}")
    private String customerHostAndPort;

    @Value("${producer.host}")
    private String producerHostAndPort;

    @Bean(destroyMethod = "")  // Avoids node shutdown on rpc disconnect
    public CordaRPCOps powerCompanyProxy(){
        CordaRPCClient powerCompanyClient = new CordaRPCClient(NetworkHostAndPort.parse(powerCompanyHostAndPort));
        return powerCompanyClient.start("user1", "test").getProxy();
    }

    @Bean(destroyMethod = "")  // Avoids node shutdown on rpc disconnect
    public CordaRPCOps gridAuthorityProxy(){
        CordaRPCClient gridAuthorityClient = new CordaRPCClient(NetworkHostAndPort.parse(gridAuthorityHostAndPort));
        return gridAuthorityClient.start("user1", "test").getProxy();
    }

    @Bean(destroyMethod = "")
    public CordaRPCOps prosumerProxy(){
        CordaRPCClient prosumerClient = new CordaRPCClient(NetworkHostAndPort.parse(prosumerHostAndPort));
        return prosumerClient.start("user1", "test").getProxy();
    }

    @Bean(destroyMethod = "")
    public CordaRPCOps customerProxy(){
        CordaRPCClient customerClient = new CordaRPCClient(NetworkHostAndPort.parse(customerHostAndPort));
        return customerClient.start("user1", "test").getProxy();
    }

    @Bean(destroyMethod = "")
    public CordaRPCOps producerProxy(){
        CordaRPCClient producerClient = new CordaRPCClient(NetworkHostAndPort.parse(producerHostAndPort));
        return producerClient.start("user1", "test").getProxy();
    }

    /**
     * Corda Jackson Support, to convert corda objects to json
     */
    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(){
        ObjectMapper mapper =  JacksonSupport.createDefaultMapper(powerCompanyProxy());
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(mapper);
        return converter;
    }
}
