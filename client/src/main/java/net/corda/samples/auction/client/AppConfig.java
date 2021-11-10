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

    @Value("${prosumer1.host}")
    private String prosumer1HostAndPort;

    @Value("${prosumer2.host}")
    private String prosumer2HostAndPort;

    @Value("${prosumer3.host}")
    private String prosumer3HostAndPort;

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
    public CordaRPCOps prosumer1Proxy(){
        CordaRPCClient prosumer1Client = new CordaRPCClient(NetworkHostAndPort.parse(prosumer1HostAndPort));
        return prosumer1Client.start("user1", "test").getProxy();
    }

    @Bean(destroyMethod = "")
    public CordaRPCOps prosumer2Proxy(){
        CordaRPCClient prosumer2Client = new CordaRPCClient(NetworkHostAndPort.parse(prosumer2HostAndPort));
        return prosumer2Client.start("user1", "test").getProxy();
    }

    @Bean(destroyMethod = "")
    public CordaRPCOps prosumer3Proxy(){
        CordaRPCClient prosumer3Client = new CordaRPCClient(NetworkHostAndPort.parse(prosumer3HostAndPort));
        return prosumer3Client.start("user1", "test").getProxy();
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
