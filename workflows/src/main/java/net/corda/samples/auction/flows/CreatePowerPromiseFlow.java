package net.corda.samples.auction.flows;

import co.paralleluniverse.fibers.Suspendable;
import kotlin.Pair;
import net.corda.core.contracts.Amount;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.node.NodeInfo;
import net.corda.core.node.StatesToRecord;
import net.corda.core.transactions.CoreTransaction;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.finance.workflows.asset.CashUtils;
import net.corda.samples.auction.contracts.PowerPromiseContract;
import net.corda.samples.auction.states.PowerPromise;
import org.w3c.dom.Node;

import java.math.BigDecimal;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


import static net.corda.core.contracts.ContractsDSL.requireThat;


/**
 * This flows is used to build a transaction to issue an asset on the Corda Ledger, which can later be put on auction.
 * It creates a self issues transaction, the states is only issued on the ledger of the party who executes the flows.
 */
public class CreatePowerPromiseFlow {

    @InitiatingFlow
    @StartableByRPC
    public static class CreatePowerPromiseFlowInitiator extends FlowLogic<SignedTransaction> {

        private final String title;
        private final String description;
        private final String imageURL;
        private final String productionManner;
        private final boolean isFromRenewableSource;
        private final LocalDateTime expires;
        private final Double powerSuppliedInKW;
        private final Double powerSupplyDurationInMin;
        private final Double lockedFundsDouble;

        /**
         * Constructor to initialise flows parameters received from rpc.
         *
         * @param title       of the asset to be issued on ledger
         * @param description of the asset to be issued in ledger
         * @param imageURL    is a url of an image of the asset
         */
        public CreatePowerPromiseFlowInitiator(String title, String description, String imageURL,
                                               String productionManner, boolean isFromRenewableSource,
                                               LocalDateTime expires, Double powerSuppliedInKW,
                                               Double powerSupplyDurationInMin, Double lockedFundsDouble) {
            this.title = title;
            this.description = description;
            this.imageURL = imageURL;
            this.productionManner = productionManner;
            this.isFromRenewableSource = isFromRenewableSource;
            this.expires = expires;
            this.powerSupplyDurationInMin = powerSupplyDurationInMin;
            this.powerSuppliedInKW = powerSuppliedInKW;
            this.lockedFundsDouble = lockedFundsDouble;
        }


        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {
            // Obtain a reference to a notary we wish to use.
            /** METHOD 1: Take first notary on network, WARNING: use for test, non-prod environments, and single-notary networks only!*
             *  METHOD 2: Explicit selection of notary by CordaX500Name - argument can by coded in flows or parsed from config (Preferred)
             *
             *  * - For production you always want to use Method 2 as it guarantees the expected notary is returned.
             */
            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
            final Party gridAuthority = getServiceHub().getNetworkMapCache().getNodeByLegalName(CordaX500Name.parse("O=GridAuthority,L=Paris,C=FR")).getLegalIdentities().get(0);

            if (getOurIdentity().getName().toString().toLowerCase().contains("customer") ){
                getLogger().warn("MY WARNING: Customer can not create Power Promise");
                throw new FlowException("Customer can not create Power Promise");
            }
            // Pare se stavljaju u depozit cim se napravi powerPromise
            TransactionBuilder transactionBuilder = new TransactionBuilder(notary);

            Amount<Currency> payment =  Amount.fromDecimal( new BigDecimal(lockedFundsDouble), Currency.getInstance("USD"));
            Pair<TransactionBuilder, List<PublicKey>> txAndKeysPair =
                    CashUtils.generateSpend(getServiceHub(), transactionBuilder, payment, getOurIdentityAndCert(),
                            gridAuthority, Collections.emptySet());
            transactionBuilder = txAndKeysPair.getFirst();


            // Create the output states
            String title = powerSuppliedInKW * powerSupplyDurationInMin / 60 + "KW/h on " +
                    DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss").format(expires);
            Amount<Currency> lockedFunds =  Amount.fromDecimal( new BigDecimal(lockedFundsDouble), Currency.getInstance("USD"));
            PowerPromise output = new PowerPromise(new UniqueIdentifier(), title, description, imageURL, productionManner,
                    isFromRenewableSource, false, getOurIdentity(), getOurIdentity(),
                    expires.atZone(ZoneId.systemDefault()).toInstant(), false, false,
                    powerSuppliedInKW, powerSupplyDurationInMin, gridAuthority, lockedFunds );


            List<NodeInfo> verificationNodeInfos = getServiceHub().getNetworkMapCache().getAllNodes().stream()
                    .filter(nodeInfo -> nodeInfo.getLegalIdentities().get(0).getName().toString().toLowerCase().contains("verification"))
                    .collect(Collectors.toList());

            List<Party> verificationNodes = verificationNodeInfos.stream()
                    .map(nodeInfo -> nodeInfo.getLegalIdentities().get(0))
                    .collect(Collectors.toList());

            List<PublicKey> signersList = new ArrayList<>();
            signersList.add(output.getOwner().getOwningKey());
            signersList.add(output.getGridAuthority().getOwningKey());

            ArrayList<FlowSession> otherParticipants = new ArrayList<>();

            for(Party verificationNode : verificationNodes) {
                otherParticipants.add(initiateFlow(verificationNode));
                signersList.add(verificationNode.getOwningKey());
            }

            // Build the transaction, add the output states and the command to the transaction.
            transactionBuilder.addOutputState(output)
                    .addCommand(new PowerPromiseContract.Commands.CreatePowerPromise(), signersList); // Required Signers

            // Verify the transaction
            transactionBuilder.verify(getServiceHub());


            // Sign the transaction
            List<PublicKey> keysToSign = txAndKeysPair.getSecond();
            keysToSign.add(getOurIdentity().getOwningKey());
            SignedTransaction selfSignedTransaction = getServiceHub().signInitialTransaction(transactionBuilder,keysToSign);


            otherParticipants.add(initiateFlow(gridAuthority));
            SignedTransaction signedTransaction = subFlow(new CollectSignaturesFlow(selfSignedTransaction, otherParticipants));

            return subFlow(new FinalityFlow(signedTransaction, otherParticipants));
        }
    }

    @InitiatedBy(CreatePowerPromiseFlow.CreatePowerPromiseFlowInitiator.class)
    public static class CreatePowerPromiseFlowResponder extends FlowLogic<SignedTransaction> {

        private final FlowSession counterpartySession;

        public CreatePowerPromiseFlowResponder (FlowSession counterpartySession) {
            this.counterpartySession = counterpartySession;
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {

            class SignTxFlow extends SignTransactionFlow {
                private SignTxFlow(FlowSession otherPartyFlow) {
                    super(otherPartyFlow);
                }

                @Override
                protected void checkTransaction(SignedTransaction stx) {
                    if (getOurIdentity().getName().toString().toLowerCase().contains("verification")) {
                        getLogger().info("VERIFICATION INFO: Verification agency entered responder flow");
                        requireThat(require -> {
                            getLogger().info("VERIFICATION INFO: data before casting: " + stx.getCoreTransaction().getOutputs());
                            CoreTransaction coreTransaction = stx.getCoreTransaction();
                            int outputsSize = coreTransaction.getOutputs().size();
                            PowerPromise output = (PowerPromise) stx.getCoreTransaction().getOutputs().get(outputsSize-1).getData();
                            getLogger().info("VERIFICATION INFO: data after casting: " + output);
                            String productionManner = output.getProductionManner().toLowerCase();
                            getLogger().info("VERIFICATION INFO: Production manner is: " + productionManner);
                            if (output.getIsFromRenewableSource()) {
                                getLogger().info("VERIFICATION INFO: Producer of Power Promise claims that it is from renewable source");
                            } else {
                                getLogger().info("VERIFICATION INFO: Producer of Power Promise claims that it is not from renewable source");
                            }


                            // TODO ono sto je oputpu nije powerPromise nego verovatno kombinacija powerpromisa i kes transfera, proveri kako to da rascivijas
//                        require.using("This must be an PowerPromise.", output instanceof PowerPromise);
//                        PowerPromise pwPromise = (PowerPromise) output;
//                        require.using("I won't accept price amounts lower then 1.", pwPromise.getPowerSuppliedInKW() > 1);
//                        getLogger().info("NODE +"+getOurIdentity().getName().toString()+" IS SIGNING POWER_TRANSACTION WITH TITLE: "+pwPromise.getTitle() );
                            require.using("Failed to verify power promise",
                                    (productionManner.contains("wind") || productionManner.contains("solar")) ||
                                            !output.getIsFromRenewableSource());
                            getLogger().info("VERIFICATION INFO: Verification agency approved the production manner");
                            return null;
                        });
                    }
                }
            }


            final SignTxFlow signTxFlow = new SignTxFlow(counterpartySession);
            final SecureHash txId = subFlow(signTxFlow).getId();
//            return subFlow(new ReceiveFinalityFlow(counterpartySession));
            return subFlow(new ReceiveTransactionFlow(counterpartySession, true, StatesToRecord.ALL_VISIBLE) );
        }
    }
}