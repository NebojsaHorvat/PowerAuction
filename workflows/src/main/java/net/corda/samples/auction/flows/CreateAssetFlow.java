package net.corda.samples.auction.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.samples.auction.contracts.PowerPromiseContract;
import net.corda.samples.auction.states.PowerPromise;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;


/**
 * This flows is used to build a transaction to issue an asset on the Corda Ledger, which can later be put on auction.
 * It creates a self issues transaction, the states is only issued on the ledger of the party who executes the flows.
 */
@StartableByRPC
public class CreateAssetFlow extends FlowLogic<SignedTransaction> {

    private final String title;
    private final String description;
    private final String imageURL;
    private final LocalDateTime expires;
    private final Double powerSuppliedInKW;
    private final Double powerSupplyDurationInMin;

    /**
     * Constructor to initialise flows parameters received from rpc.
     *
     * @param title of the asset to be issued on ledger
     * @param description of the asset to be issued in ledger
     * @param imageURL is a url of an image of the asset
     */
    // TODO izbaci title i descirption posto su nepotrebni i onda moras menjati i kontroler i fronend
    public CreateAssetFlow(String title, String description, String imageURL, LocalDateTime expires,
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
        final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0); // METHOD 1
        // final Party notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB")); // METHOD 2


//        LocalDateTime expires = LocalDateTime.now().plusMinutes(5);
        // Create the output states
        String title = powerSuppliedInKW*powerSupplyDurationInMin/60 + "KW/h on " +
                DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss").format(expires);
        PowerPromise output = new PowerPromise(new UniqueIdentifier(),title, description, imageURL,
                getOurIdentity(),getOurIdentity(), expires.atZone(ZoneId.systemDefault()).toInstant(),false, false,
                powerSuppliedInKW,powerSupplyDurationInMin);

        // Build the transaction, add the output states and the command to the transaction.
        TransactionBuilder transactionBuilder = new TransactionBuilder(notary)
                .addOutputState(output)
                .addCommand(new PowerPromiseContract.Commands.CreatePowerPromise(),
                        Arrays.asList(getOurIdentity().getOwningKey())); // Required Signers

        // Verify the transaction
        transactionBuilder.verify(getServiceHub());

        // Sign the transaction
        SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);

        // Notarise the transaction and record the states in the ledger.
        return subFlow(new FinalityFlow(signedTransaction, Collections.emptyList()));
    }
}
