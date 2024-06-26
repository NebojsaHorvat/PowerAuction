package net.corda.samples.auction;

import com.google.common.collect.ImmutableList;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.Amount;
import net.corda.core.node.NetworkParameters;
import net.corda.core.transactions.SignedTransaction;
import net.corda.samples.auction.flows.CreateAuctionFlow;
import net.corda.samples.auction.flows.CreatePowerPromiseFlow;
import net.corda.samples.auction.states.PowerPromise;
import net.corda.samples.auction.states.AuctionState;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.MockNetworkParameters;
import net.corda.testing.node.StartedMockNode;
import net.corda.testing.node.TestCordapp;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;

import static org.junit.Assert.*;

public class FlowTests {
    private  MockNetwork network;
    private  StartedMockNode a;
    private  StartedMockNode b;

    @Before
    public void setup() {
        network = new MockNetwork(
                new MockNetworkParameters(
                        ImmutableList.of(
                                TestCordapp.findCordapp("net.corda.samples.auction.flows"),
                                TestCordapp.findCordapp("net.corda.samples.auction.contracts")
                        )
                ).withNetworkParameters(new NetworkParameters(4, Collections.emptyList(),
                        10485760, 10485760 * 50, Instant.now(), 1,
                        Collections.emptyMap()))
        );
        a = network.createPartyNode(null);
        b = network.createPartyNode(null);
        network.runNetwork();
    }

    @After
    public void tearDown() {
        network.stopNodes();
    }

//    @Test
//    public void testCreateAssetFlow() throws Exception {
//        LocalDateTime expires = LocalDateTime.now().plusMinutes(5);
//        CreatePowerPromiseFlow.CreatePowerPromiseFlowInitiator flow = new CreatePowerPromiseFlowInitiator("Test Asset", "Dummy Asset", "http://abc.com/dummy.png",expires,11.1,11.1);
//        CordaFuture<SignedTransaction> future = a.startFlow(flow);
//        network.runNetwork();
//        SignedTransaction signedTransaction = future.get();
//        PowerPromise powerPromise = (PowerPromise) signedTransaction.getTx().getOutput(0);
//        assertNotNull(powerPromise);
//    }
//
//    @Test
//    public void testCreateAuctionFlow() throws Exception {
//        LocalDateTime expires = LocalDateTime.now().plusMinutes(5);
//        CreatePowerPromiseFlow assetflow = new CreatePowerPromiseFlow("Test Asset", "Dummy Asset", "http://abc.com/dummy.png",expires,11.1,11.1);
//        CordaFuture<SignedTransaction> future = a.startFlow(assetflow);
//        network.runNetwork();
//        SignedTransaction signedTransaction = future.get();
//        PowerPromise powerPromise = (PowerPromise) signedTransaction.getTx().getOutput(0);
//        CreateAuctionFlow.CreateAuctionInitiator auctionFlow = new CreateAuctionFlow.CreateAuctionInitiator(Amount.parseCurrency("1000 USD"),
//                powerPromise.getLinearId().getId(), LocalDateTime.ofInstant(Instant.now().plusMillis(30000), ZoneId.systemDefault()));
//        CordaFuture<SignedTransaction> future1 = a.startFlow(auctionFlow);
//        network.runNetwork();
//        SignedTransaction transaction = future1.get();
//        AuctionState auctionState = (AuctionState) transaction.getTx().getOutput(0);
//        assertNotNull(auctionState);
//    }
}