package net.corda.samples.auction.states;

import net.corda.core.contracts.*;
import net.corda.core.flows.FlowLogicRef;
import net.corda.core.flows.FlowLogicRefFactory;
import net.corda.core.identity.AbstractParty;
import net.corda.samples.auction.contracts.AssetContract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * An ownable states to represent an asset that could be put on auction.
 */
@BelongsToContract(AssetContract.class)
//public class Asset implements OwnableState, LinearState, SchedulableState {
public class Asset implements OwnableState, LinearState {


    private final UniqueIdentifier linearId;
    private final String title;
    private final String description;
    private final String imageUrl;
    private final AbstractParty owner;

    private final Instant deliveryTime;
    private final Boolean expired;
    private final Boolean delivered;

    public Asset(UniqueIdentifier linearId, String title, String description, String imageUrl,
                 AbstractParty owner, Instant deliveryTime, Boolean expired, Boolean delivered) {
        this.linearId = linearId;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.owner = owner;
        this.deliveryTime = deliveryTime;
        this.expired = expired;
        this.delivered = delivered;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(owner);
    }

    @NotNull
    @Override
    public AbstractParty getOwner() {
        return owner;
    }

    /**
     * This method should be called to retrieve an ownership transfer command and the updated states with the new owner
     * passed as a parameter to the method.
     *
     * @param newOwner of the asset
     * @return A CommandAndState object encapsulating the command and the new states with the changed owner, to be used
     * in the ownership transfer transaction.
     */
    @NotNull
    @Override
    public CommandAndState withNewOwner(@NotNull AbstractParty newOwner) {
        return new CommandAndState(new AssetContract.Commands.TransferAsset(),
                new Asset(this.getLinearId(), this.getTitle(), this.getDescription(),
                        this.getImageUrl(), newOwner, this.deliveryTime, this.expired,this.delivered ));
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Instant getDeliveryTime() {
        return deliveryTime;
    }

    public Boolean getExpired() {
        return expired;
    }

    public Boolean getDelivered() {
        return delivered;
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return linearId;
    }

//    @Nullable
//    @Override
//    public ScheduledActivity nextScheduledActivity(@NotNull StateRef thisStateRef, @NotNull FlowLogicRefFactory flowLogicRefFactory) {
//        if(expired)
//            return null;
//
//        FlowLogicRef flowLogicRef = flowLogicRefFactory.create(
//                "net.corda.samples.auction.flows.CheckDeliveryFlow.CheckDeliveryInitiator", linearId);
//        return new ScheduledActivity(flowLogicRef, deliveryTime);
//    }
}
