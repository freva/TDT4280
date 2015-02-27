package Exercise3;

import Exercise3.containers.AuctionItem;
import Exercise3.containers.Item;
import com.sun.applet2.AppletParameters;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Exchange extends Agent {
    private ArrayList<AuctionItem> auctionQueue = new ArrayList<AuctionItem>();
    private static AID exchange;
    private static ArrayList<AID> traders = new ArrayList<AID>();
    private static final int amountResources = 1000;


    protected void setup() {
        exchange = this.getAID();
        getTraders();
        sendResourceDistribution();
        addBehaviour(new AuctionAdministrator());
    }

    private void sendResourceDistribution(){
        int nr = 3;
        ArrayList<ArrayList<Double>> ownedResourcesFractions = generateOwnedResources(3);
        ArrayList<ArrayList<Double>> wantedResourcesFractions = generateWantedResources(3);
        for(int i = 0; i< nr; i++){
            HashMap<Item, Integer> resourceDeficit = new HashMap<Item, Integer>();
            for(int j = 0; j < ownedResourcesFractions.size(); j++){
                resourceDeficit.put(Item.values()[j],(int)(amountResources*ownedResourcesFractions.get(j).get(i) - amountResources*wantedResourcesFractions.get(j).get(i)));
            }
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(traders.get(i));
            try {
                msg.setContentObject(resourceDeficit);
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.send(msg);
        }
    }

    private void getTraders(){
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Trader");
        template.addServices(sd);
        DFAgentDescription result[] = new DFAgentDescription[0];
        try {
            result = DFService.search(this, template);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        for (DFAgentDescription aResult : result) {
            traders.add(aResult.getName());
        }
    }

    private ArrayList<ArrayList<Double>> generateOwnedResources(int nrOfAgents){
        ArrayList<ArrayList<Double>> resourceFractions = new ArrayList<ArrayList<Double>>();
        for(int i = 0; i < Item.values().length; i++){
            ArrayList<Double> itemFraction = new ArrayList<Double>();
            double sum = 0;
            for(int j = 0; j < nrOfAgents; j++){
                double randomFloat = Math.random();
                sum += randomFloat;
                itemFraction.add(randomFloat);
            }
            double total = 0;
            for(int k = 0; k<itemFraction.size(); k++){
                total += itemFraction.get(k)/sum;
                itemFraction.set(k, itemFraction.get(k)/sum);
            }
            resourceFractions.add(itemFraction);
        }
        return resourceFractions;
    }

    private ArrayList<ArrayList<Double>> generateWantedResources(int nrOfAgents){
        ArrayList<ArrayList<Double>> resourceFractions = new ArrayList<ArrayList<Double>>();
        for(int i = 0; i < Item.values().length; i++){
            ArrayList<Double> itemFraction = new ArrayList<Double>();
            double sum = 0;
            for(int j = 0; j < nrOfAgents; j++){
                double randomFloat = Math.abs(new Random().nextGaussian());
                sum += randomFloat;
                itemFraction.add(randomFloat);
            }
            double total = 0;
            for(int k = 0; k<itemFraction.size(); k++){
                total += itemFraction.get(k)/sum;
                itemFraction.set(k, itemFraction.get(k)/sum);
            }
            resourceFractions.add(itemFraction);
        }
        return resourceFractions;
    }


    private void checkNextAuction() throws IOException {
        if(auctionQueue.size() == 0) return;

        AuctionItem ai = auctionQueue.remove((int) (auctionQueue.size()*Math.random()));
        ACLMessage msg = new ACLMessage(ACLMessage.QUERY_IF);
        msg.addReceiver(ai.getOwner());
        msg.setContentObject(ai);
        this.send(msg);
    }


    public static AID getExchange() {
        return exchange;
    }


    private class AuctionAdministrator extends CyclicBehaviour {

        @Override
        public void action() {
            ACLMessage msg = receive();
            ACLMessage response;

            try {
                if(msg == null) return;
                switch (msg.getPerformative()) {
                    case ACLMessage.INFORM:
                        ArrayList<AuctionItem> auctionItems = (ArrayList<AuctionItem>) msg.getContentObject();
                        auctionQueue.addAll(auctionItems);
                        break;

                    case ACLMessage.CONFIRM:
                        AuctionItem ai = (AuctionItem) msg.getContentObject();

                        response = new ACLMessage(ACLMessage.INFORM_REF);
                        response.setContentObject(ai);
                        response.addReceiver(ai.getOwner());
                        myAgent.send(response);
                        break;

                    case ACLMessage.DISCONFIRM:
                        checkNextAuction();
                        break;

                    case ACLMessage.AGREE:
                        checkNextAuction();
                        break;

                    case ACLMessage.CFP:
                        checkNextAuction();
                        break;

                }
            } catch (UnreadableException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
