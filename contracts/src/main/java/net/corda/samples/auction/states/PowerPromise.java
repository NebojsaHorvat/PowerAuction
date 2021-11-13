package net.corda.samples.auction.states;

import net.corda.core.contracts.*;
import net.corda.core.flows.FlowLogicRef;
import net.corda.core.flows.FlowLogicRefFactory;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.samples.auction.contracts.PowerPromiseContract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;

/**
 * An ownable states to represent an asset that could be put on auction.
 */
@BelongsToContract(PowerPromiseContract.class)
public class PowerPromise implements OwnableState, LinearState, SchedulableState {
//public class Asset implements OwnableState, LinearState {


    private final UniqueIdentifier linearId;
    private final String title;
    private final String description;
    private final String imageUrl;
    private final AbstractParty owner;
    private final Party supplier;
    private final Party gridAuthority;
    private final Amount<Currency> lockedFunds;


    private final Instant deliveryTime;
    private final Boolean expired;
    private final Boolean delivered;
    private final Double powerSuppliedInKW;
    private final Double powerSupplyDurationInMin;
    private final Double powerProducedInKWh;

    public PowerPromise(UniqueIdentifier linearId, String title, String description, String imageUrl,
                        AbstractParty owner, Party supplier, Instant deliveryTime, Boolean expired, Boolean delivered,
                        Double powerSuppliedInKW, Double powerSupplyDurationInMin, Party gridAuthority, Amount<Currency> lockedFunds) {
        this.linearId = linearId;
        this.description = description;
        this.imageUrl = imageUrl;
        this.owner = owner;
        this.supplier = supplier;
        this.deliveryTime = deliveryTime;
        this.expired = expired;
        this.delivered = delivered;
        this.powerSuppliedInKW = powerSuppliedInKW;
        this.powerSupplyDurationInMin = powerSupplyDurationInMin;
        this.powerProducedInKWh = powerSuppliedInKW*powerSupplyDurationInMin/60;
        this.title = title;
        this.gridAuthority = gridAuthority;
        this.lockedFunds = lockedFunds;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(owner, gridAuthority);
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
        return new CommandAndState(new PowerPromiseContract.Commands.TransferPowerPromise(),
                new PowerPromise(this.getLinearId(), this.getTitle() , this.getDescription(),
                        this.getImageUrl(), newOwner, this.supplier , this.deliveryTime, this.expired,this.delivered, this.powerSuppliedInKW,
                        this.powerSupplyDurationInMin, this.gridAuthority,this.lockedFunds));
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

    public Double getPowerSuppliedInKW() {
        return powerSuppliedInKW;
    }

    public Double getPowerSupplyDurationInMin() {
        return powerSupplyDurationInMin;
    }

    public Double getPowerProducedInKWh() {
        return powerProducedInKWh;
    }

    public Party getSupplier() {
        return supplier;
    }

    public Party getGridAuthority() {
        return gridAuthority;
    }

    public Amount<Currency> getLockedFunds() {
        return lockedFunds;
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return linearId;
    }

    @Nullable
    @Override
    public ScheduledActivity nextScheduledActivity(@NotNull StateRef thisStateRef, @NotNull FlowLogicRefFactory flowLogicRefFactory) {
        if(expired)
            return null;

        FlowLogicRef flowLogicRef = flowLogicRefFactory.create(
                "net.corda.samples.auction.flows.CheckDeliveryFlow$CheckDeliveryInitiator", thisStateRef);
//                "net.corda.samples.auction.flows.CheckDeliveryFlow", thisStateRef);
        return new ScheduledActivity(flowLogicRef, deliveryTime);
    }
}
