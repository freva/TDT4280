package Exercise3;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.util.HashMap;
import java.util.Random;

public class NegotiationAgent extends Agent {

    private HashMap<Item, Integer> ownedResources = new HashMap<Item, Integer>();
    private HashMap<Item, Integer> wantedResources = new HashMap<Item, Integer>();

    protected void setup(int id) {
        setResources(id);
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Trader");
        sd.setName(getLocalName());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    private void setResources(int id){
        Random random = new Random();
        long currentTime = System.currentTimeMillis();
        int bit = (int)currentTime&63;
        for(int i = 1; i<64; i<<=1){
            if((bit & i)>0){
                ownedResources.put(Item.values()[i-1],random.nextInt()%1000);
            }
        }
    }


}
