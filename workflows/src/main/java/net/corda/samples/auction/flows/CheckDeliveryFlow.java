package net.corda.samples.auction.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.StateRef;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.samples.auction.contracts.AuctionContract;
import net.corda.samples.auction.states.PowerPromise;

import java.util.Collections;


//Scheduled Flows must be annotated with @SchedulableFlow.
@SchedulableFlow
public class CheckDeliveryFlow extends FlowLogic<SignedTransaction>{
    private final StateRef promiseRef;

    /**
     * @param promiseRef is the unique identifier of the promise which delivery is being checked .
     */
    public CheckDeliveryFlow(StateRef promiseRef) {
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
        // TODO promeniti da ovo moze da radi samo onaj ko odrzava mrezu

//        if (getOurIdentity().getName().toString().equals(inputState.getOwner().nameOrNull().toString())) {

            // Create the output state, mark tge auction as inactive
            // TODO stavi da ovo delievered bude random true/false

            PowerPromise outputState = new PowerPromise(inputState.getLinearId(),inputState.getTitle(),inputState.getDescription(),
                    inputState.getImageUrl(),inputState.getOwner(),inputState.getSupplier(), inputState.getDeliveryTime(), true, true,
                    inputState.getPowerSuppliedInKW(), inputState.getPowerSupplyDurationInMin(), inputState.getPowerCompany());


            // Build the transaction.
            TransactionBuilder transactionBuilder = new TransactionBuilder(notary)
                    .addInputState(inputStateAndRef)
                    .addOutputState(outputState)
                    .addCommand(new AuctionContract.Commands.EndAuction(), getOurIdentity().getOwningKey());

            //Verify the transaction against the contract
            transactionBuilder.verify(getServiceHub());

            //Sign the transaction.
            SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);

            //Notarize and record the transaction in all participants ledger.
            return subFlow(new FinalityFlow(signedTransaction, Collections.emptyList()));

//        } else {
//            return null;
//        }

    }
}


//    @InitiatedBy(CheckDeliveryFlow.CheckDeliveryInitiator.class)
//    public static class CheckDeliveryResponder extends FlowLogic<SignedTransaction> {
//
//        private FlowSession counterpartySession;
//
//        public CheckDeliveryResponder(FlowSession counterpartySession) {
//            this.counterpartySession = counterpartySession;
//        }
//
//        @Override
//        @Suspendable
//        public SignedTransaction call() throws FlowException {
//            return subFlow(new ReceiveFinalityFlow(counterpartySession));
//        }
//    }
//}
