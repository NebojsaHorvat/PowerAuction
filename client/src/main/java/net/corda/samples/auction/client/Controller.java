package net.corda.samples.auction.client;

import net.corda.core.contracts.Amount;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.TransactionVerificationException;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.utilities.OpaqueBytes;
import net.corda.finance.contracts.asset.Cash;
import net.corda.finance.flows.CashIssueAndPaymentFlow;
import net.corda.samples.auction.client.DTO.AuctionDTO;
import net.corda.samples.auction.client.DTO.PowerPromiseDTO;
import net.corda.samples.auction.flows.*;
import net.corda.samples.auction.states.PowerPromise;
import net.corda.samples.auction.states.AuctionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auction/")
@CrossOrigin(origins="http://localhost:3000")
public class Controller {

    @Autowired
    private CordaRPCOps powerCompanyProxy;

    @Autowired
    private CordaRPCOps gridAuthorityProxy;

    @Autowired
    private CordaRPCOps prosumerProxy;

    @Autowired
    private CordaRPCOps customerProxy;

    @Autowired
    private CordaRPCOps producerProxy;

    @Autowired
    @Qualifier("prosumerProxy")
    private CordaRPCOps activeParty;

    @GetMapping("list")
    public APIResponse<List<AuctionDTO>> getAuctionList() {
        try{
            List<StateAndRef<AuctionState>> auctionList = activeParty.vaultQuery(AuctionState.class).getStates();

            // auctionList.stream().map(a -> new AuctionDTO(a.getState().getData())).forEach(a -> System.out.println(a));
            List<AuctionState> auctions = auctionList.stream().map(a -> a.getState().getData()).collect(Collectors.toList());

            //proci kroz sve aukcije dobaviti power promise i pretvoriti sve u DTO
            List<AuctionDTO> auctionDTOs = new ArrayList<>();
            for(AuctionState a : auctions){
                System.out.println(a.getAuctionItem().getPointer().getId().toString());
                PowerPromise p = getAssetById(a.getAuctionItem().getPointer().getId().toString());
                // System.out.println("title " + p.getTitle());
                if(p != null){
                    auctionDTOs.add(new AuctionDTO(a, p));
                }
                
            }

            return APIResponse.success(auctionDTOs);
        }catch(Exception e){
            return APIResponse.error(e.getMessage());
        }
    }

    @GetMapping("asset/list")
    public APIResponse<List<PowerPromiseDTO>> getAssetList(){
        try{
    
            List<StateAndRef<PowerPromise>> assetList = activeParty.vaultQuery(PowerPromise.class).getStates();
            List<PowerPromiseDTO> assets = assetList.stream().map(a -> new PowerPromiseDTO(a.getState().getData())).collect(Collectors.toList());
            return APIResponse.success(assets);
        }catch(Exception e){
            return APIResponse.error(e.getMessage());
        }
    }


    
    public PowerPromise getAssetById(String assetId){
        try{
            List<StateAndRef<PowerPromise>> assetList = activeParty.vaultQuery(PowerPromise.class).getStates();
            List<StateAndRef<PowerPromise>> assets = assetList.stream().filter(a -> a.getState().getData().getLinearId().getId().toString().equals(assetId)).collect(Collectors.toList());

            PowerPromise p = assets.get(0).getState().getData();
            System.out.println(p);
            return p;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    @PostMapping("asset/create")
    public APIResponse<Void> createPowerPromise(@RequestBody Forms.PowerForm powerForm){
        System.out.println("Create power promise");
        try{
            if(activeParty.nodeInfo().getLegalIdentities().get(0).getName().toString().toLowerCase().contains("customer")){
                return APIResponse.error("Customers can not create Power Promise!");
            }
            LocalDateTime expires = LocalDateTime.parse(powerForm.getDeliveryTime(),
                    DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss a"));
            String title = powerForm.getPowerSuppliedInKW()*powerForm.getPowerSupplyDurationInMin()/60 + "KW/h on " +
                    DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss").format(expires);
            // TODO locked funds su uvek 10
            System.out.println("Before calling create power promise flow");
            System.out.println(powerForm);
//            dodati proveru da li korisnik ima novca za kreiranje promis-a
            activeParty.startFlowDynamic(CreatePowerPromiseFlow.CreatePowerPromiseFlowInitiator.class,
                    title,
                    "",
                    "img/power.png",expires,powerForm.getPowerSuppliedInKW(),powerForm.getPowerSupplyDurationInMin(),10.0);

            System.out.println("After calling create power promise flow");

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
            if(activeParty.nodeInfo().getLegalIdentities().get(0).getName().toString().toLowerCase().contains("producer")){
                return APIResponse.error("Producer can not place bid in order to buy Power Promise!");
            }

            System.out.println(activeParty.startFlowDynamic(BidFlow.BidInitiator.class,
                    Amount.parseCurrency(bidForm.getAmount() + " USD"),
                    UUID.fromString(bidForm.getAuctionId()))
                    .getReturnValue().get());


            // ovde bih trebala da pokrenem socket koji ce da obavesti tracker app da je bidovano i da sacuva bid u bazu
            // moram imati kako izgleda bit, tj koliko je bidovano, koja je aukcija, ko je bidovao i vreme

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
        }else if(party.equals("prosumer")){
            activeParty = prosumerProxy;
        }else if(party.equals("customer")){
            activeParty = customerProxy;
        }else if(party.equals("producer")){
            activeParty = producerProxy;
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

            prosumerProxy.startFlowDynamic(CashIssueAndPaymentFlow.class,
                    Amount.parseCurrency(amountOfCacheIssuedToAll+ " USD"),
                    OpaqueBytes.of("PartyA".getBytes()),
                    activeParty.partiesFromName("prosumer", false).iterator().next(),
                    false, activeParty.notaryIdentities().get(0))
                            .getReturnValue().get();
            customerProxy.startFlowDynamic(CashIssueAndPaymentFlow.class,
                    Amount.parseCurrency(amountOfCacheIssuedToAll+ " USD"),
                    OpaqueBytes.of("PartyA".getBytes()),
                    activeParty.partiesFromName("customer", false).iterator().next(),
                    false, activeParty.notaryIdentities().get(0))
                    .getReturnValue().get();
            producerProxy.startFlowDynamic(CashIssueAndPaymentFlow.class,
                    Amount.parseCurrency(amountOfCacheIssuedToAll+ " USD"),
                    OpaqueBytes.of("PartyA".getBytes()),
                    activeParty.partiesFromName("producer", false).iterator().next(),
                    false, activeParty.notaryIdentities().get(0))
                    .getReturnValue().get();


            Double lockedFunds = 10.0;
            LocalDateTime expires = LocalDateTime.now().plusMinutes(2);
            Double powerSuppliedInKW = 11.1;
            Double powerSupplyDurationInMin = 120.0;
            String title = powerSuppliedInKW*powerSupplyDurationInMin/60 + "KW/h on " +
                    DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss").format(expires);
            prosumerProxy.startFlowDynamic(CreatePowerPromiseFlow.CreatePowerPromiseFlowInitiator.class,
                    title,
                    "",
                    "img/power.png",expires,powerSuppliedInKW,powerSupplyDurationInMin,lockedFunds);

            powerSuppliedInKW = 55.0;
            powerSupplyDurationInMin = 60.0;
            title = powerSuppliedInKW*powerSupplyDurationInMin/60 + "KW/h on " +
                    DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss").format(expires);
            prosumerProxy.startFlowDynamic(CreatePowerPromiseFlow.CreatePowerPromiseFlowInitiator.class,
                    title,
                    "",
                    "img/power.png",expires,powerSuppliedInKW,powerSupplyDurationInMin,lockedFunds);

            // Customer ne moze da ima powerPromise ciji je on vlasnik
//            powerSuppliedInKW = 999.0;
//            powerSupplyDurationInMin = 60.0;
//            title = powerSuppliedInKW*powerSupplyDurationInMin/60 + "KW/h on " +
//                    DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss").format(expires);
//            customerProxy.startFlowDynamic(CreatePowerPromiseFlow.CreatePowerPromiseFlowInitiator.class,
//                    title,
//                    "",
//                    "img/power.png",expires,powerSuppliedInKW,powerSupplyDurationInMin,lockedFunds);

            powerSuppliedInKW = 10000.0;
            powerSupplyDurationInMin = 120.0;
            title = powerSuppliedInKW*powerSupplyDurationInMin/60 + "KW/h on " +
                    DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss").format(expires);
            producerProxy.startFlowDynamic(CreatePowerPromiseFlow.CreatePowerPromiseFlowInitiator.class,
                    title,
                    "",
                    "img/power.png",expires,powerSuppliedInKW,powerSupplyDurationInMin,lockedFunds);

        }catch (Exception e){
            return APIResponse.error(e.getMessage());
        }
        return APIResponse.success();
    }
}
