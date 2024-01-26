package net.corda.samples.auction.client;

import java.io.Serializable;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

import net.corda.core.contracts.Amount;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.samples.auction.states.AuctionState;
import net.corda.samples.auction.states.PowerPromise;

public class DTO {
     public static class AuctionDTO implements Serializable{
        private UUID auctionId;
        private Amount<Currency> basePrice;
        private Amount<Currency> highestBid;
        private Party highestBidder;
        private Instant bidEndTime;
        private Boolean active;
        private Party auctioneer;
        private String power_promise_title;

        public AuctionDTO(){}


        public AuctionDTO(AuctionState a, PowerPromise p) {
            this.auctionId = a.getAuctionId();
            this.basePrice = a.getBasePrice();
            this.highestBid = a.getHighestBid();
            this.highestBidder = a.getHighestBidder();
            this.bidEndTime = a.getBidEndTime();
            this.active = a.getActive();
            this.auctioneer = a.getAuctioneer();
            this.power_promise_title = p.getTitle();
        }

        public UUID getAuctionId() {
            return auctionId;
        }

        public void setAuctionId(UUID auctionId) {
            this.auctionId = auctionId;
        }

        public Amount<Currency> getBasePrice() {
            return basePrice;
        }

        public void setBasePrice(Amount<Currency> basePrice) {
            this.basePrice = basePrice;
        }

        public Amount<Currency> getHighestBid() {
            return highestBid;
        }

        public void setHighestBid(Amount<Currency> highestBid) {
            this.highestBid = highestBid;
        }

        public Party getHighestBidder() {
            return highestBidder;
        }

        public void setHighestBidder(Party highestBidder) {
            this.highestBidder = highestBidder;
        }

        public Instant getBidEndTime() {
            return bidEndTime;
        }

        public void setBidEndTime(Instant bidEndTime) {
            this.bidEndTime = bidEndTime;
        }

        public Boolean getActive() {
            return active;
        }

        public void setActive(Boolean active) {
            this.active = active;
        }

        public Party getAuctioneer() {
            return auctioneer;
        }

        public void setAuctioneer(Party auctioneer) {
            this.auctioneer = auctioneer;
        }

        public String getPowerPromiseTitle(){
            return power_promise_title;
        }

        public void setPowerPromiseTitle(String title) {
            this.power_promise_title = title;
        }
    }


    public static class PowerPromiseDTO implements Serializable{
        private UUID promiseId;
        private String title;
        private Double powerSuppliedInKW;
        private Double powerSupplyDurationInMin;
        private Instant deliveryTime;
        private AbstractParty owner;

        public PowerPromiseDTO(){}


        public PowerPromiseDTO(PowerPromise p) {
            this.promiseId = p.getLinearId().getId();
            this.title = p.getTitle();
            this.powerSuppliedInKW = p.getPowerSuppliedInKW();
            this.powerSupplyDurationInMin  = p.getPowerSupplyDurationInMin();
            this.deliveryTime = p.getDeliveryTime();
            this.owner = p.getOwner();
        }

        public UUID getPromiseId() {
            return promiseId;
        }

        public void setPromiseId(UUID promiseId) {
            this.promiseId = promiseId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Double getPowerSuppliedInKW() {
            return powerSuppliedInKW;
        }

        public void setPowerSuppliedInKW(Double powerSuppliedInKW) {
            this.powerSuppliedInKW = powerSuppliedInKW;
        }

        public Double getPowerSupplyDurationInMin() {
            return powerSupplyDurationInMin;
        }

        public void setPowerSupplyDurationInMin(Double powerSupplyDurationInMin) {
            this.powerSupplyDurationInMin = powerSupplyDurationInMin;
        }

        public Instant getDeliveryTime() {
            return deliveryTime;
        }

        public void setDeliveryTime(Instant deliveryTime) {
            this.deliveryTime = deliveryTime;
        }

        public AbstractParty getOwner() {
            return owner;
        }

        public void setOwner(AbstractParty owner) {
            this.owner = owner;
        }
    }

    public static class BidDTO{

    }
}
