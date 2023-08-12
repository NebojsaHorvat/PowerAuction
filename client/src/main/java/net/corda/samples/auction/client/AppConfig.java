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

    @Value("${producer1.host}")
    private String producer1HostAndPort;

    @Value("${customer1.host}")
    private String customer1HostAndPort;

    @Value("${producer2.host}")
    private String producer2HostAndPort;

    @Value("${customer2.host}")
    private String customer2HostAndPort;

    @Value("${prosumer1.host}")
    private String prosumer1HostAndPort;

    @Value("${prosumer2.host}")
    private String prosumer2HostAndPort;

    @Value("${prosumer3.host}")
    private String prosumer3HostAndPort;

    @Value("${prosumer4.host}")
    private String prosumer4HostAndPort;

    @Value("${prosumer5.host}")
    private String prosumer5HostAndPort;

    @Value("${customer3.host}")
    private String customer3HostAndPort;

    @Value("${customer4.host}")
    private String customer4HostAndPort;

    @Value("${customer5.host}")
    private String customer5HostAndPort;

    @Value("${customer6.host}")
    private String customer6HostAndPort;

    @Value("${customer7.host}")
    private String customer7HostAndPort;

    @Value("${verificationAgency.host}")
    private String verificationAgencyHostAndPort;

    @Value("${speculator.host}")
    private String speculatorHostAndPort;

    @Value("${energyStorageProvider.host}")
    private String energyStorageProviderHostAndPort;

    @Bean(destroyMethod = "") // Avoids node shutdown on rpc disconnect
    public CordaRPCOps powerCompanyProxy() {
        CordaRPCClient powerCompanyClient = new CordaRPCClient(NetworkHostAndPort.parse(powerCompanyHostAndPort));
        return powerCompanyClient.start("user1", "test").getProxy();
    }

    @Bean(destroyMethod = "") // Avoids node shutdown on rpc disconnect
    public CordaRPCOps gridAuthorityProxy() {
        CordaRPCClient gridAuthorityClient = new CordaRPCClient(NetworkHostAndPort.parse(gridAuthorityHostAndPort));
        return gridAuthorityClient.start("user1", "test").getProxy();
    }

    @Bean(destroyMethod = "")
    public CordaRPCOps prosumerProxy() {
        CordaRPCClient prosumerClient = new CordaRPCClient(NetworkHostAndPort.parse(prosumerHostAndPort));
        return prosumerClient.start("user1", "test").getProxy();
    }

    @Bean(destroyMethod = "")
    public CordaRPCOps customerProxy() {
        CordaRPCClient customerClient = new CordaRPCClient(NetworkHostAndPort.parse(customerHostAndPort));
        return customerClient.start("user1", "test").getProxy();
    }

    @Bean(destroyMethod = "")
    public CordaRPCOps producerProxy() {
        CordaRPCClient producerClient = new CordaRPCClient(NetworkHostAndPort.parse(producerHostAndPort));
        return producerClient.start("user1", "test").getProxy();
    }

    @Bean(destroyMethod = "")
    public CordaRPCOps producer1Proxy() {
        CordaRPCClient producer1Client = new CordaRPCClient(NetworkHostAndPort.parse(producer1HostAndPort));
        return producer1Client.start("user1", "test").getProxy();
    }

    @Bean(destroyMethod = "")
    public CordaRPCOps customer1Proxy() {
        CordaRPCClient customer1Client = new CordaRPCClient(NetworkHostAndPort.parse(customer1HostAndPort));
        return customer1Client.start("user1", "test").getProxy();
    }

    @Bean(destroyMethod = "")
    public CordaRPCOps producer2Proxy() {
        CordaRPCClient producer2Client = new CordaRPCClient(NetworkHostAndPort.parse(producer2HostAndPort));
        return producer2Client.start("user1", "test").getProxy();
    }

    @Bean(destroyMethod = "")
    public CordaRPCOps customer2Proxy() {
        CordaRPCClient customer2Client = new CordaRPCClient(NetworkHostAndPort.parse(customer2HostAndPort));
        return customer2Client.start("user1", "test").getProxy();
    }

    @Bean(destroyMethod = "")
    public CordaRPCOps prosumer1Proxy() {
        CordaRPCClient prosumer1Client = new CordaRPCClient(NetworkHostAndPort.parse(prosumer1HostAndPort));
        return prosumer1Client.start("user1", "test").getProxy();
    }

    @Bean(destroyMethod = "")
    public CordaRPCOps prosumer2Proxy() {
        CordaRPCClient prosumer2Client = new CordaRPCClient(NetworkHostAndPort.parse(prosumer2HostAndPort));
        return prosumer2Client.start("user1", "test").getProxy();
    }

    @Bean(destroyMethod = "")
    public CordaRPCOps prosumer3Proxy() {
        CordaRPCClient prosumer3Client = new CordaRPCClient(NetworkHostAndPort.parse(prosumer3HostAndPort));
        return prosumer3Client.start("user1", "test").getProxy();
    }

    @Bean(destroyMethod = "")
    public CordaRPCOps prosumer4Proxy() {
        CordaRPCClient prosumer4Client = new CordaRPCClient(NetworkHostAndPort.parse(prosumer4HostAndPort));
        return prosumer4Client.start("user1", "test").getProxy();
    }

    @Bean(destroyMethod = "")
    public CordaRPCOps prosumer5Proxy() {
        CordaRPCClient prosumer5Client = new CordaRPCClient(NetworkHostAndPort.parse(prosumer5HostAndPort));
        return prosumer5Client.start("user1", "test").getProxy();
    }

    @Bean(destroyMethod = "")
    public CordaRPCOps customer3Proxy() {
        CordaRPCClient customer3Client = new CordaRPCClient(NetworkHostAndPort.parse(customer3HostAndPort));
        return customer3Client.start("user1", "test").getProxy();
    }

    @Bean(destroyMethod = "")
    public CordaRPCOps customer4Proxy() {
        CordaRPCClient customer4Client = new CordaRPCClient(NetworkHostAndPort.parse(customer4HostAndPort));
        return customer4Client.start("user1", "test").getProxy();
    }

    @Bean(destroyMethod = "")
    public CordaRPCOps customer5Proxy() {
        CordaRPCClient customer5Client = new CordaRPCClient(NetworkHostAndPort.parse(customer5HostAndPort));
        return customer5Client.start("user1", "test").getProxy();
    }

    @Bean(destroyMethod = "")
    public CordaRPCOps customer6Proxy() {
        CordaRPCClient customer6Client = new CordaRPCClient(NetworkHostAndPort.parse(customer6HostAndPort));
        return customer6Client.start("user1", "test").getProxy();
    }

    @Bean(destroyMethod = "")
    public CordaRPCOps customer7Proxy() {
        CordaRPCClient customer7Client = new CordaRPCClient(NetworkHostAndPort.parse(customer7HostAndPort));
        return customer7Client.start("user1", "test").getProxy();
    }

    @Bean(destroyMethod = "")
    public CordaRPCOps verificationAgencyProxy(){
        CordaRPCClient verificationAgencyClient = new CordaRPCClient(NetworkHostAndPort.parse(verificationAgencyHostAndPort));
        return verificationAgencyClient.start("user1", "test").getProxy();
    }

    @Bean(destroyMethod = "")
    public CordaRPCOps speculatorProxy() {
        CordaRPCClient speculatorClient = new CordaRPCClient(NetworkHostAndPort.parse(speculatorHostAndPort));
        return speculatorClient.start("user1", "test").getProxy();
    }

    @Bean(destroyMethod = "")
    public CordaRPCOps energyStorageProviderProxy() {
        CordaRPCClient energyStorageProviderClient = new CordaRPCClient(NetworkHostAndPort.parse(energyStorageProviderHostAndPort));
        return energyStorageProviderClient.start("user1", "test").getProxy();
    }

    /**
     * Corda Jackson Support, to convert corda objects to json
     */
    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        ObjectMapper mapper = JacksonSupport.createDefaultMapper(powerCompanyProxy());
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(mapper);
        return converter;
    }
}