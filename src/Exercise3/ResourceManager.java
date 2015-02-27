package Exercise3;

import Exercise3.containers.Item;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class ResourceManager {
    private static final int amountResources = 400;

    public static ArrayList<HashMap<Item,Integer>> getDistributions(int nrOfAgents){
        ArrayList<ArrayList<Double>> ownedResourcesFractions = generateOwnedResources(nrOfAgents);
        ArrayList<ArrayList<Double>> wantedResourcesFractions = generateWantedResources(nrOfAgents);
        return resourceDistribution(ownedResourcesFractions, wantedResourcesFractions, nrOfAgents);
    }


    private static ArrayList<HashMap<Item, Integer>> resourceDistribution(ArrayList<ArrayList<Double>> ownedResourcesFractions, ArrayList<ArrayList<Double>> wantedResourcesFractions, int nrOfAgents){
        ArrayList<HashMap<Item, Integer>> distributions = new ArrayList<HashMap<Item, Integer>>();
        for(int i = 0; i< nrOfAgents; i++){
            HashMap<Item, Integer> resourceDeficit = new HashMap<Item, Integer>();
            for(int j = 0; j < ownedResourcesFractions.size(); j++){
                resourceDeficit.put(Item.values()[j],(int)(nrOfAgents*amountResources*(ownedResourcesFractions.get(j).get(i) - wantedResourcesFractions.get(j).get(i))));
            }
            distributions.add(resourceDeficit);
        }
        return distributions;
    }


    private static ArrayList<ArrayList<Double>> generateOwnedResources(int nrOfAgents){
        ArrayList<ArrayList<Double>> resourceFractions = new ArrayList<ArrayList<Double>>();
        for(int i = 0; i < Item.values().length; i++){
            ArrayList<Double> itemFraction = new ArrayList<Double>();
            double sum = 0;
            for(int j = 0; j < nrOfAgents; j++){
                double randomFloat = Math.random();
                sum += randomFloat;
                itemFraction.add(randomFloat);
            }
            for(int k = 0; k<itemFraction.size(); k++){
                itemFraction.set(k, itemFraction.get(k)/sum);
            }
            resourceFractions.add(itemFraction);
        }
        return resourceFractions;
    }

    private static ArrayList<ArrayList<Double>> generateWantedResources(int nrOfAgents){
        ArrayList<ArrayList<Double>> resourceFractions = new ArrayList<ArrayList<Double>>();
        for(int i = 0; i < Item.values().length; i++){
            ArrayList<Double> itemFraction = new ArrayList<Double>();
            double sum = 0;
            for(int j = 0; j < nrOfAgents; j++){
                double randomFloat = Math.abs(new Random().nextGaussian());
                sum += randomFloat;
                itemFraction.add(randomFloat);
            }
            for(int k = 0; k<itemFraction.size(); k++){
                itemFraction.set(k, itemFraction.get(k)/sum);
            }
            resourceFractions.add(itemFraction);
        }
        return resourceFractions;
    }
}
