package net.corda.samples.auction.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.samples.auction.contracts.PowerPromiseContract;
import net.corda.samples.auction.states.PowerPromise;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

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
        private final LocalDateTime expires;
        private final Double powerSuppliedInKW;
        private final Double powerSupplyDurationInMin;

        /**
         * Constructor to initialise flows parameters received from rpc.
         *
         * @param title       of the asset to be issued on ledger
         * @param description of the asset to be issued in ledger
         * @param imageURL    is a url of an image of the asset
         */
        // TODO izbaci title posto su nepotrebni i onda moras menjati i kontroler i front
        public CreatePowerPromiseFlowInitiator(String title, String description, String imageURL, LocalDateTime expires,
                               Double powerSuppliedInKW, Double powerSupplyDurationInMin) {
            this.title = title;
            this.description = description;
            this.imageURL = imageURL;
            this.expires = expires;
            this.powerSupplyDurationInMin = powerSupplyDurationInMin;
            this.powerSuppliedInKW = powerSuppliedInKW;
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
            final Party powerCompany = getServiceHub().getNetworkMapCache().getNodeByLegalName(CordaX500Name.parse("O=PowerCompany,L=Paris,C=FR")).getLegalIdentities().get(0);


            //        LocalDateTime expires = LocalDateTime.now().plusMinutes(5);
            // Create the output states
            String title = powerSuppliedInKW * powerSupplyDurationInMin / 60 + "KW/h on " +
                    DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss").format(expires);
            PowerPromise output = new PowerPromise(new UniqueIdentifier(), title, description, imageURL,
                    getOurIdentity(), getOurIdentity(), expires.atZone(ZoneId.systemDefault()).toInstant(), false, false,
                    powerSuppliedInKW, powerSupplyDurationInMin, powerCompany);

            // Build the transaction, add the output states and the command to the transaction.
            TransactionBuilder transactionBuilder = new TransactionBuilder(notary)
                    .addOutputState(output)
                    .addCommand(new PowerPromiseContract.Commands.CreatePowerPromise(),
                            Arrays.asList(output.getOwner().getOwningKey(), output.getPowerCompany().getOwningKey())); // Required Signers

            // Verify the transaction
            transactionBuilder.verify(getServiceHub());

            // Sign the transaction
            SignedTransaction selfSignedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);

            // Notarise the transaction and record the states in the ledger.
            ArrayList<FlowSession> otherParticipant = new ArrayList<>();
            otherParticipant.add(initiateFlow(powerCompany));

            SignedTransaction signedTransaction = subFlow(new CollectSignaturesFlow(selfSignedTransaction, otherParticipant));

            return subFlow(new FinalityFlow(signedTransaction, otherParticipant));
        }
    }

    @InitiatedBy(CreatePowerPromiseFlow.CreatePowerPromiseFlowInitiator.class)
    public static class CreatePowerPromiseFlowResponder extends FlowLogic<SignedTransaction> {

        private FlowSession counterpartySession;

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
                    requireThat(require -> {
//                        ContractState output = stx.getTx().getOutputs().get(0).getData();
//                        require.using("This must be an SplitTransaction.", output instanceof SplitTransactionState);
//                        SplitTransactionState spState = (SplitTransactionState) output;
//                        require.using("I won't accept SplitTransactions with a value over 100.", spState.getValue() <= 100);
                        return null;
                    });
                }
            }
            final SignTxFlow signTxFlow = new SignTxFlow(counterpartySession);
            final SecureHash txId = subFlow(signTxFlow).getId();

            return subFlow(new ReceiveFinalityFlow(counterpartySession));
        }
    }
}