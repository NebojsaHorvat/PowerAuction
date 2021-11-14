package net.corda.samples.auction.client;

import net.corda.core.contracts.Amount;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.TransactionVerificationException;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.utilities.OpaqueBytes;
import net.corda.finance.contracts.asset.Cash;
import net.corda.finance.flows.CashIssueAndPaymentFlow;
import net.corda.samples.auction.flows.*;
import net.corda.samples.auction.states.PowerPromise;
import net.corda.samples.auction.states.AuctionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/auction/")
public class Controller {

    @Autowired
    private CordaRPCOps powerCompanyProxy;

    @Autowired
    private CordaRPCOps gridAuthorityProxy;

    @Autowired
    private CordaRPCOps prosumer1Proxy;

    @Autowired
    private CordaRPCOps prosumer2Proxy;

    @Autowired
    private CordaRPCOps prosumer3Proxy;

    @Autowired
    @Qualifier("prosumer1Proxy")
    private CordaRPCOps activeParty;

    @GetMapping("list")
    public APIResponse<List<StateAndRef<AuctionState>>> getAuctionList() {
        try{
            List<StateAndRef<AuctionState>> auctionList = activeParty.vaultQuery(AuctionState.class).getStates();
            return APIResponse.success(auctionList);
        }catch(Exception e){
            return APIResponse.error(e.getMessage());
        }
    }

    @GetMapping("asset/list")
    public APIResponse<List<StateAndRef<PowerPromise>>> getAssetList(){
        try{
            List<StateAndRef<PowerPromise>> assetList = activeParty.vaultQuery(PowerPromise.class).getStates();
            return APIResponse.success(assetList);
        }catch(Exception e){
            return APIResponse.error(e.getMessage());
        }
    }

    @PostMapping("asset/create")
    public APIResponse<Void> createPowerPromise(@RequestBody Forms.PowerForm powerForm){
        try{
            LocalDateTime expires = LocalDateTime.parse(powerForm.getDeliveryTime(),
                    DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss a"));
            String title = powerForm.getPowerSuppliedInKW()*powerForm.getPowerSupplyDurationInMin()/60 + "KW/h on " +
                    DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss").format(expires);
            // TODO locked funds su uvek 10
            activeParty.startFlowDynamic(CreatePowerPromiseFlow.CreatePowerPromiseFlowInitiator.class,
                    title,
                    "",
                    "img/power.png",expires,powerForm.getPowerSuppliedInKW(),powerForm.getPowerSupplyDurationInMin(),10.0);

            return APIResponse.success();
        }catch(Exception e){
            return APIResponse.error(e.getMessage());
        }
    }

    @PostMapping("create")
    public APIResponse<Void> createAuction(@RequestBody Forms.CreateAuctionForm auctionForm){
        try {
            activeParty.startFlowDynamic(CreateAuctionFlow.CreateAuctionInitiator.class,
                    Amount.parseCurrency(auctionForm.getBasePrice() + " USD"),
                    UUID.fromString(auctionForm.getAssetId()),
                    LocalDateTime.parse(auctionForm.getDeadline(),
                            DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss a"))).getReturnValue().get();
            return APIResponse.success();
        }catch (ExecutionException e){
            if(e.getCause() != null && e.getCause().getClass().equals(TransactionVerificationException.ContractRejection.class)){
                return APIResponse.error(e.getCause().getMessage());
            }else{
                return APIResponse.error(e.getMessage());
            }
        }catch (Exception e){
            return APIResponse.error(e.getMessage());
        }
    }

    @PostMapping("delete/{auctionId}")
    public APIResponse<Void> deleteAuction(@PathVariable String auctionId){
        try {
            activeParty.startFlowDynamic(AuctionExitFlow.AuctionExitInitiator.class, UUID.fromString(auctionId)).getReturnValue().get();
            return APIResponse.success();
        }catch (ExecutionException e){
            if(e.getCause() != null && e.getCause().getClass().equals(TransactionVerificationException.ContractRejection.class)){
                return APIResponse.error(e.getCause().getMessage());
            }else{
                return APIResponse.error(e.getMessage());
            }
        }catch (Exception e){
            return APIResponse.error(e.getMessage());
        }
    }

    @PostMapping("placeBid")
    public APIResponse<Void> placeBid(@RequestBody Forms.BidForm bidForm){
        try{
            activeParty.startFlowDynamic(BidFlow.BidInitiator.class,
                    Amount.parseCurrency(bidForm.getAmount() + " USD"),
                    UUID.fromString(bidForm.getAuctionId()))
                    .getReturnValue().get();
            return APIResponse.success();
        }catch (ExecutionException e){
            if(e.getCause() != null && e.getCause().getClass().equals(TransactionVerificationException.ContractRejection.class)){
                return APIResponse.error(e.getCause().getMessage());
            }else{
                return APIResponse.error(e.getMessage());
            }
        }catch (Exception e){
            return APIResponse.error(e.getMessage());
        }
    }

    @PostMapping("payAndSettle")
    public APIResponse<Void> payAndSettle(@RequestBody Forms.SettlementForm settlementForm){
        try {
            activeParty.startFlowDynamic(AuctionSettlementFlow.class,
                    UUID.fromString(settlementForm.getAuctionId()),
                    Amount.parseCurrency(settlementForm.getAmount()))
                    .getReturnValue().get();
            return APIResponse.success();
        }
        catch (ExecutionException e){
            if(e.getCause() != null && e.getCause().getClass().equals(TransactionVerificationException.ContractRejection.class)){
                return APIResponse.error(e.getCause().getMessage());
            }else{
                return APIResponse.error(e.getMessage());
            }
        }catch (Exception e){
            return APIResponse.error(e.getMessage());
        }
    }

    @PostMapping("issueCash")
    public APIResponse<Void> issueCash(@RequestBody Forms.IssueCashForm issueCashForm){
        try{
            activeParty.startFlowDynamic(CashIssueAndPaymentFlow.class,
                    Amount.parseCurrency(issueCashForm.getAmount() + " USD"),
                    OpaqueBytes.of("PartyA".getBytes()),
                    activeParty.partiesFromName(issueCashForm.getParty(), false).iterator().next(),
                    false,
                    activeParty.notaryIdentities().get(0))
                    .getReturnValue().get();
            return APIResponse.success();
        }catch (ExecutionException e){
            if(e.getCause() != null && e.getCause().getClass().equals(TransactionVerificationException.ContractRejection.class)){
                return APIResponse.error(e.getCause().getMessage());
            }else{
                return APIResponse.error(e.getMessage());
            }
        }catch (Exception e){
            return APIResponse.error(e.getMessage());
        }
    }


    @GetMapping("getCashBalance")
    public APIResponse<Long> getCashBalance(){
        try {
            List<StateAndRef<Cash.State>> cashStateList = activeParty.vaultQuery(Cash.State.class).getStates();
            Long amount = 0L;
            if(cashStateList.size()>0) {
                amount = cashStateList.stream().map(stateStateAndRef ->
                        stateStateAndRef.getState().getData().getAmount().getQuantity()).reduce(Long::sum).get();
                if (amount >= 100) {
                    amount = amount / 100;
                } else {
                    amount = 0L;
                }
            }
            return APIResponse.success(amount);
        }catch (Exception e){
            return APIResponse.error(e.getMessage());
        }
    }

    @PostMapping(value = "switch-party/{party}")
    public APIResponse<Long> switchParty(@PathVariable String party){
        if(party.equals("powerCompany")){
            activeParty = powerCompanyProxy;
        }else if(party.equals("gridAuthority")){
            activeParty = gridAuthorityProxy;
        }else if(party.equals("prosumer1")){
            activeParty = prosumer1Proxy;
        }else if(party.equals("prosumer2")){
            activeParty = prosumer2Proxy;
        }else if(party.equals("prosumer3")){
            activeParty = prosumer3Proxy;
        }else{
            return APIResponse.error("Unrecognised Party");
        }
        return getCashBalance();
    }

    /**
     * Create some initial data to play with.
     * @return
     */
    @PostMapping("setup")
    public APIResponse<Void> setupDemoData(){
        try {
            // Issuing cache to all participants
            Double amountOfCacheIssuedToAll = 30.0;

            prosumer1Proxy.startFlowDynamic(CashIssueAndPaymentFlow.class,
                    Amount.parseCurrency(amountOfCacheIssuedToAll+ " USD"),
                    OpaqueBytes.of("PartyA".getBytes()),
                    activeParty.partiesFromName("prosumer1", false).iterator().next(),
                    false, activeParty.notaryIdentities().get(0))
                            .getReturnValue().get();
            prosumer2Proxy.startFlowDynamic(CashIssueAndPaymentFlow.class,
                    Amount.parseCurrency(amountOfCacheIssuedToAll+ " USD"),
                    OpaqueBytes.of("PartyA".getBytes()),
                    activeParty.partiesFromName("prosumer2", false).iterator().next(),
                    false, activeParty.notaryIdentities().get(0))
                    .getReturnValue().get();
            prosumer3Proxy.startFlowDynamic(CashIssueAndPaymentFlow.class,
                    Amount.parseCurrency(amountOfCacheIssuedToAll+ " USD"),
                    OpaqueBytes.of("PartyA".getBytes()),
                    activeParty.partiesFromName("prosumer3", false).iterator().next(),
                    false, activeParty.notaryIdentities().get(0))
                    .getReturnValue().get();


            Double lockedFunds = 10.0;
            LocalDateTime expires = LocalDateTime.now().plusMinutes(2);
            Double powerSuppliedInKW = 11.1;
            Double powerSupplyDurationInMin = 120.0;
            // TODO nema potrebe da ovde daljem title kad ga vec izgenerisem u CreatePowerPromiseFlow
            String title = powerSuppliedInKW*powerSupplyDurationInMin/60 + "KW/h on " +
                    DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss").format(expires);
            prosumer1Proxy.startFlowDynamic(CreatePowerPromiseFlow.CreatePowerPromiseFlowInitiator.class,
                    title,
                    "",
                    "img/power.png",expires,powerSuppliedInKW,powerSupplyDurationInMin,lockedFunds);

            powerSuppliedInKW = 55.0;
            powerSupplyDurationInMin = 60.0;
            title = powerSuppliedInKW*powerSupplyDurationInMin/60 + "KW/h on " +
                    DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss").format(expires);
            prosumer1Proxy.startFlowDynamic(CreatePowerPromiseFlow.CreatePowerPromiseFlowInitiator.class,
                    title,
                    "",
                    "img/power.png",expires,powerSuppliedInKW,powerSupplyDurationInMin,lockedFunds);

            powerSuppliedInKW = 999.0;
            powerSupplyDurationInMin = 60.0;
            title = powerSuppliedInKW*powerSupplyDurationInMin/60 + "KW/h on " +
                    DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss").format(expires);
            prosumer2Proxy.startFlowDynamic(CreatePowerPromiseFlow.CreatePowerPromiseFlowInitiator.class,
                    title,
                    "",
                    "img/power.png",expires,powerSuppliedInKW,powerSupplyDurationInMin,lockedFunds);

            powerSuppliedInKW = 10000.0;
            powerSupplyDurationInMin = 120.0;
            title = powerSuppliedInKW*powerSupplyDurationInMin/60 + "KW/h on " +
                    DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss").format(expires);
            prosumer3Proxy.startFlowDynamic(CreatePowerPromiseFlow.CreatePowerPromiseFlowInitiator.class,
                    title,
                    "",
                    "img/power.png",expires,powerSuppliedInKW,powerSupplyDurationInMin,lockedFunds);

        }catch (Exception e){
            return APIResponse.error(e.getMessage());
        }
        return APIResponse.success();
    }
}
