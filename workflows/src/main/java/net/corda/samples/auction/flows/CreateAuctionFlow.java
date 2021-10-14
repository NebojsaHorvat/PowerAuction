package net.corda.samples.auction.flows;


import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.Amount;
import net.corda.core.contracts.LinearPointer;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import net.corda.samples.auction.contracts.AuctionContract;
import net.corda.samples.auction.states.AuctionState;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static net.corda.core.contracts.ContractsDSL.requireThat;

/**
 * This flow is used to create a auction for an asset.
 */
public class CreateAuctionFlow {

    @InitiatingFlow
    @StartableByRPC
    public static class CreateAuctionInitiator extends FlowLogic<SignedTransaction> {

        private final ProgressTracker progressTracker = new ProgressTracker();

        private final Amount<Currency> basePrice;
        private final UUID auctionItem;
        private final LocalDateTime bidDeadLine;

        /**
         * Constructor to initialise flow parameters received from rpc.
         *
         * @param basePrice of the asset to be put of auction.
         * @param auctionItem is the uuid of the asset to be put on auction
         * @param bidDeadLine is the time till when the auction will be active
         */
        public CreateAuctionInitiator(Amount<Currency> basePrice, UUID auctionItem, LocalDateTime bidDeadLine) {
            this.basePrice = basePrice;
            this.auctionItem = auctionItem;
            this.bidDeadLine = bidDeadLine;
        }

        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            // Obtain a reference to a notary we wish to use.
            /** METHOD 1: Take first notary on network, WARNING: use for test, non-prod environments, and single-notary networks only!*
             *  METHOD 2: Explicit selection of notary by CordaX500Name - argument can by coded in flow or parsed from config (Preferred)
             *
             *  * - For production you always want to use Method 2 as it guarantees the expected notary is returned.
             */
            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0); // METHOD 1
            // final Party notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB")); // METHOD 2
            final Party powerCompany = getServiceHub().getNetworkMapCache().getNodeByLegalName(CordaX500Name.parse("O=PowerCompany,L=Paris,C=FR")).getLegalIdentities().get(0);
            Party auctioneer = getOurIdentity();

            // Fetch all parties from the network map and remove the auctioneer and notary. All the parties are added as
            // participants to the auction state so that its visible to all the parties in the network.
            List<Party> bidders = getServiceHub().getNetworkMapCache().getAllNodes().stream()
                    .map(nodeInfo -> nodeInfo.getLegalIdentities().get(0))
                    .collect(Collectors.toList());
            bidders.remove(auctioneer);
            bidders.remove(notary);

            // Create the output state. Use a linear pointer to point to the asset on auction. The asset would be added
            // as a reference state to the transaction and hence we won't spend it.
            AuctionState auctionState = new AuctionState(
                    new LinearPointer<>(new UniqueIdentifier(null, auctionItem), LinearState.class),
                    UUID.randomUUID(), basePrice, null, null,
                    bidDeadLine.atZone(ZoneId.systemDefault()).toInstant(), null, true, auctioneer,
                    bidders, null);

            // Build the transaction
            TransactionBuilder builder = new TransactionBuilder(notary)
                    .addOutputState(auctionState)
                    .addCommand(new AuctionContract.Commands.CreateAuction(), Arrays.asList(auctioneer.getOwningKey(),powerCompany.getOwningKey() ));

            // Verify the transaction
            builder.verify(getServiceHub());

            // Sign the transaction
            SignedTransaction selfSignedTransaction = getServiceHub().signInitialTransaction(builder);

            // Call finality Flow to notarise the transaction and record it in all participants ledger.
            List<FlowSession> bidderSessions = new ArrayList<>();
            FlowSession powerCompanySession = null;
            for(Party bidder: bidders)
            {
                FlowSession session = initiateFlow(bidder);

                if (bidder.getName().toString().contains("PowerCompany")){
                    powerCompanySession = session;
                }
                bidderSessions.add(session);
            }
            SignedTransaction signedTransaction = subFlow(new CollectSignaturesFlow(selfSignedTransaction, Arrays.asList(powerCompanySession)));

            return subFlow(new FinalityFlow(signedTransaction, bidderSessions));
        }
    }

    @InitiatedBy(CreateAuctionInitiator.class)
    public static class CreateAuctionResponder extends FlowLogic<SignedTransaction> {

        private FlowSession counterpartySession;

        public CreateAuctionResponder(FlowSession counterpartySession) {
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
