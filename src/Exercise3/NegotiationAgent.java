package Exercise3;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.util.HashMap;

public class NegotiationAgent extends Agent {
    private HashMap<Item, Integer> ownedResources = new HashMap<Item, Integer>();
    private HashMap<Item, Integer> wantedResources = new HashMap<Item, Integer>();
    private int coins = 1000;

    protected void setup() {
        int id = Integer.parseInt((String) getArguments()[0]);
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
        HashMap<Item, Integer> test = new HashMap<Item, Integer>();
        for(Item item: Item.values()) {
            ownedResources.put(item, (int) (200 * Math.random() + 100));
            wantedResources.put(item, (int) (Main.numAgents*100/Math.pow(2, (id + item.getId())%Main.numAgents)*Math.random())+60);
            test.put(item, ownedResources.get(item) - wantedResources.get(item));
        }

        System.out.println(getLocalName() + " wants resources: " + test.entrySet());
    }


}
