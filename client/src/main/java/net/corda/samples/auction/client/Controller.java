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
    public APIResponse<Void> createAsset(@RequestBody Forms.AssetForm assetForm){
        try{
            // TODO izmeni da se vreme uzima iz forme
            LocalDateTime expires = LocalDateTime.now().plusMinutes(5);
            activeParty.startFlowDynamic(CreateAssetFlow.class, assetForm.getTitle(), assetForm.getDescription(),
                    assetForm.getImageUrl(),expires).getReturnValue().get();
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
            LocalDateTime expires = LocalDateTime.now().plusMinutes(5);
            Double powerSuppliedInKW = 11.1;
            Double powerSupplyDurationInMin = 120.0;
            String title = powerSuppliedInKW*powerSupplyDurationInMin/60 + "KW/h on " +
                    DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss").format(expires);
            prosumer1Proxy.startFlowDynamic(CreateAssetFlow.class,
                    "10 KW/h 01.11.2021.",
                    "The most famous painting in the world, a masterpiece by Leonardo da Vinci, the mysterious woman with " +
                            "the enigmatic smile. The sitter in the painting is thought to be Lisa Gherardini, the wife of " +
                            "Florence merchant Francesco del Giocondo. It did represent an innovation in art -- the painting" +
                            " is the earliest known Italian portrait to focus so closely on the sitter in a half-length " +
                            "portrait.",
                    "img/power.jpg",expires,powerSuppliedInKW,powerSupplyDurationInMin);
            powerSuppliedInKW = 55.0;
            powerSupplyDurationInMin = 60.0;
            title = powerSuppliedInKW*powerSupplyDurationInMin/60 + "KW/h on " +
                    DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss").format(expires);
            prosumer1Proxy.startFlowDynamic(CreateAssetFlow.class,
                    "1000 KW/h 12.12.2021.",
                    "Yet another masterpiece by Leonardo da Vinci, painted in an era when religious imagery was still " +
                            "a dominant artistic theme, \"The Last Supper\" depicts the last time Jesus broke bread with " +
                            "his disciples before his crucifixion.",
                    "img/power.jpg",expires,powerSuppliedInKW,powerSupplyDurationInMin);

            powerSuppliedInKW = 999.0;
            powerSupplyDurationInMin = 60.0;
            title = powerSuppliedInKW*powerSupplyDurationInMin/60 + "KW/h on " +
                    DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss").format(expires);
            prosumer2Proxy.startFlowDynamic(CreateAssetFlow.class,
                    "111 KW/h 9.11.2021.",
                    "Painted by Vincent van Gogh, this comparatively abstract painting is the signature example of " +
                            "van Gogh's innovative and bold use of thick brushstrokes. The painting's striking blues and " +
                            "yellows and the dreamy, swirling atmosphere have intrigued art lovers for decades.",
                    "img/power.jpg",expires,powerSuppliedInKW,powerSupplyDurationInMin);

            powerSuppliedInKW = 10000.0;
            powerSupplyDurationInMin = 120.0;
            title = powerSuppliedInKW*powerSupplyDurationInMin/60 + "KW/h on " +
                    DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss").format(expires);
            prosumer3Proxy.startFlowDynamic(CreateAssetFlow.class,
                    title,
                    "First things first -- \"The Scream\" is not a single work of art. According to a British Museum's blog," +
                            " there are two paintings, two pastels and then an unspecified number of prints. Date back to " +
                            "the the year 1893, this masterpiece is a work of Edvard Munch",
                    "img/power.jpg",expires,powerSuppliedInKW,powerSupplyDurationInMin);

            activeParty = prosumer1Proxy;
        }catch (Exception e){
            return APIResponse.error(e.getMessage());
        }
        return APIResponse.success();
    }
}
