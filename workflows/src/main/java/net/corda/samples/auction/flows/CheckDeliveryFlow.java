package net.corda.samples.auction.flows;

import co.paralleluniverse.fibers.Suspendable;
import kotlin.Pair;
import net.corda.core.contracts.Amount;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.StateRef;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.identity.PartyAndCertificate;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.finance.workflows.asset.CashUtils;
import net.corda.samples.auction.contracts.AuctionContract;
import net.corda.samples.auction.states.PowerPromise;

import java.math.BigDecimal;
import java.security.PublicKey;
import java.util.*;

import static net.corda.core.contracts.ContractsDSL.requireThat;

public class CheckDeliveryFlow {
    //Scheduled Flows must be annotated with @SchedulableFlow.
    @SchedulableFlow
    @InitiatingFlow
    public static class CheckDeliveryInitiator extends FlowLogic<SignedTransaction>{

        private final StateRef promiseRef;
        /**
         * @param promiseRef is the unique identifier of the promise which delivery is being checked .
         */
        public CheckDeliveryInitiator(StateRef promiseRef) {
            this.promiseRef = promiseRef;
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {

            // Query the vault to fetch a list of all AuctionState state, and filter the results based on the auctionId
            // to fetch the desired AuctionState state from the vault. This filtered state would be used as input to the
            // transaction.
    //            List<StateAndRef<Asset>> assetStateAndRefs = getServiceHub().getVaultService()
    //                    .queryBy(Asset.class).getStates();
    //            StateAndRef<Asset> inputStateAndRef = assetStateAndRefs.stream().filter(assetStateAndRef -> {
    //                Asset asset = assetStateAndRef.getState().getData();
    //                return asset.getLinearId().toString().equals(this.promiseId.toString());
    //            }).findAny().orElseThrow(() -> new IllegalArgumentException("Asset with id "+promiseId.toString()+" Not Found"));

            StateAndRef<PowerPromise> inputStateAndRef = getServiceHub().toStateAndRef(promiseRef);

            //get the notary from the input state.
            Party notary = inputStateAndRef.getState().getNotary();
            PowerPromise inputState = inputStateAndRef.getState().getData();

            // Check used to restrict the flow execution to be only done by the auctioneer.
            if (getOurIdentity().getName().toString().equals(inputState.getGridAuthority().nameOrNull().toString())) {

                // TODO podesiti da se negde proveri da li je delivered ili ne i vrati se true ili false
                Boolean delivered = true;
                PowerPromise outputState = new PowerPromise(inputState.getLinearId(),inputState.getTitle(),inputState.getDescription(),
                        inputState.getImageUrl(),inputState.getOwner(),inputState.getSupplier(), inputState.getDeliveryTime(), true, delivered,
                        inputState.getPowerSuppliedInKW(), inputState.getPowerSupplyDurationInMin(), inputState.getGridAuthority(), inputState.getLockedFunds());

                SignedTransaction selfSignedTransaction = null;
                if(delivered){
                    boolean isSpeculator = outputState.getOwner().nameOrNull().toString().toLowerCase().contains("speculator");
                    Amount<Currency> amountToSpend;
                    PartyAndCertificate amountSender;
                    AbstractParty amountReceiver;
                    List<PublicKey> signersList;
                    ArrayList<FlowSession> otherParticipants = new ArrayList<>();

                    if (isSpeculator) {
                        getLogger().info("SPECULATOR INFO: Speculator detected.");
                        // TODO Adjust the value of the fee acording to a chosen policy. Currently the same as in "else" branch.
                        amountToSpend = Amount.fromDecimal(new BigDecimal(
                                inputState.getLockedFunds().toDecimal().doubleValue()*0.9),
                                Currency.getInstance("USD"));

                        CordaX500Name legalName = inputState.getOwner().nameOrNull();
                        getLogger().info("SPECULATOR INFO: Speculator legal name: " + legalName.toString());
                        amountSender = getServiceHub()
                                .getNetworkMapCache()
                                .getNodeByLegalName(legalName)
                                .getLegalIdentitiesAndCerts().get(0);

                        amountReceiver = inputState.getGridAuthority();

                        // TODO Determine who should sign the transaction. Should i sign the transaction which takes my money?
                        signersList = Arrays.asList(
                                getOurIdentity().getOwningKey(),
                                inputState.getOwner().getOwningKey(),
                                inputState.getGridAuthority().getOwningKey());
                        otherParticipants.add(initiateFlow(inputState.getOwner()));
                        otherParticipants.add(initiateFlow(inputState.getGridAuthority()));
                    }
                    else {
                        // Ako struja nije uspesno isporucena onda se pare salju powerCompaniju i uzme se malo sebi za odrzavanje
                        amountToSpend = Amount.fromDecimal( new BigDecimal(
                                inputState.getLockedFunds().toDecimal().doubleValue()*0.9),
                                Currency.getInstance("USD"));
                        amountSender = getOurIdentityAndCert();
                        amountReceiver = inputState.getOwner();
                        signersList = Arrays.asList(
                                getOurIdentity().getOwningKey(),
                                inputState.getOwner().getOwningKey());
                        otherParticipants.add(initiateFlow(inputState.getOwner()));
                    }

                    TransactionBuilder transactionBuilder = new TransactionBuilder(notary);

                    Pair<TransactionBuilder, List<PublicKey>> txAndKeysPair =
                            CashUtils.generateSpend(getServiceHub(), transactionBuilder, amountToSpend, amountSender,
                                    amountReceiver, Collections.emptySet());
                    transactionBuilder = txAndKeysPair.getFirst();

                    transactionBuilder.addInputState(inputStateAndRef)
                            .addOutputState(outputState)
                            .addCommand(new AuctionContract.Commands.EndAuction(), signersList);

                    //Verify the transaction against the contract
                    transactionBuilder.verify(getServiceHub());

                    // Sign the transaction. The transaction should be sigend with the new keyPair generated for Cash spending
                    // and the node's key.
                    List<PublicKey> keysToSign = txAndKeysPair.getSecond();
                    keysToSign.add(getOurIdentity().getOwningKey());
                    selfSignedTransaction = getServiceHub().signInitialTransaction(transactionBuilder, keysToSign);


                    SignedTransaction signedTransaction = subFlow(new CollectSignaturesFlow(selfSignedTransaction, otherParticipants));
                    //Notarize and record the transaction in all participants ledger.
                    return subFlow(new FinalityFlow(signedTransaction, otherParticipants));

                }else{
                    final Party powerCompany = getServiceHub().getNetworkMapCache()
                            .getNodeByLegalName(CordaX500Name.parse("O=PowerCompany,L=Paris,C=FR")).getLegalIdentities().get(0);

                    TransactionBuilder transactionBuilder = new TransactionBuilder(notary);

                    // Ako struja nije uspesno isporucena onda se pare salju powerCompaniju i uzme se malo sebi za odrzavanje
                    Amount<Currency> payment =  Amount.fromDecimal( new BigDecimal(inputState.getLockedFunds().toDecimal().doubleValue()*0.9), Currency.getInstance("USD"));

                    Pair<TransactionBuilder, List<PublicKey>> txAndKeysPair =
                            CashUtils.generateSpend(getServiceHub(), transactionBuilder, payment, getOurIdentityAndCert(),
                                    powerCompany, Collections.emptySet());
                    transactionBuilder = txAndKeysPair.getFirst();

                    transactionBuilder.addInputState(inputStateAndRef)
                            .addOutputState(outputState)
                            .addCommand(new AuctionContract.Commands.EndAuction(), Arrays.asList(getOurIdentity().getOwningKey(), powerCompany.getOwningKey(), inputState.getOwner().getOwningKey()));

                    //Verify the transaction against the contract
                    transactionBuilder.verify(getServiceHub());

                    // Sign the transaction. The transaction should be sigend with the new keyPair generated for Cash spending
                    // and the node's key.
                    List<PublicKey> keysToSign = txAndKeysPair.getSecond();
                    keysToSign.add(getOurIdentity().getOwningKey());
                    selfSignedTransaction = getServiceHub().signInitialTransaction(transactionBuilder, keysToSign);

                    ArrayList<FlowSession> otherParticipant = new ArrayList<>();
                    otherParticipant.add(initiateFlow(inputState.getOwner()));
                    otherParticipant.add(initiateFlow(powerCompany ));

                    SignedTransaction signedTransaction = subFlow(new CollectSignaturesFlow(selfSignedTransaction, otherParticipant));
                    //Notarize and record the transaction in all participants ledger.
                    return subFlow(new FinalityFlow(signedTransaction, otherParticipant));
                }


            } else {
                return null;
            }
        }
    }


    @InitiatedBy(CheckDeliveryFlow.CheckDeliveryInitiator.class)
    public static class CheckDeliveryResponder extends FlowLogic<SignedTransaction> {

        private FlowSession counterpartySession;

        public CheckDeliveryResponder(FlowSession counterpartySession) {
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
