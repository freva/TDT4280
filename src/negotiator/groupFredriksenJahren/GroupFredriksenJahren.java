package negotiator.groupFredriksenJahren;

import java.util.*;

import agents.bayesianopponentmodel.BayesianOpponentModelScalable;
import negotiator.Bid;
import negotiator.BidIterator;
import negotiator.DeadlineType;
import negotiator.Timeline;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.utility.UtilitySpace;

/**
 * This is your negotiation party.
 */
public class GroupFredriksenJahren extends AbstractNegotiationParty {
	/**
	 * Keeps own BayesianOpponentModel for each agent
	 */
    private HashMap<Object, BayesianOpponentModelScalable> opponentModels = new HashMap<>();

	/**
	 * Keeps a history concessions, each bit represents a point in time. 1 means conceded.
	 */
	private HashMap<Object, Long> concessions = new HashMap<>();

	/**
	 * Stores last bid received from every agent, used to detect non-concession behaviour
	 */
	private HashMap<Object, Bid> lastBids = new HashMap<>();

	/**
	 * Stores the last bid received so that it can be evaluated again when choose action is called
	 */
    private Map.Entry<Object, Bid> lastBid;

	/**
	 * Lowest utility bid we are willing to accept
	 */
    private static final double reservationValue = 0.6;

	/**
	 * Beta is defined in such a way that 8/9 way into the negotiation, the target value should be 0.875
	 */
    private static final double beta = Math.log(1-(0.875-reservationValue)/(1-reservationValue)) / Math.log(8d/9);

	/**
	 * Number of rounds that should be part of history
	 */
	private static int historySize;



    /**
     * Please keep this constructor. This is called by genius.
     *
     * @param utilitySpace Your utility space.
     * @param deadlines The deadlines set for this negotiation.
     * @param timeline Value counting from 0 (start) to 1 (end).
     * @param randomSeed If you use any randomization, use this seed for it.
     */
    public GroupFredriksenJahren(UtilitySpace utilitySpace, Map<DeadlineType, Object> deadlines, Timeline timeline, long randomSeed) {
        super(utilitySpace, deadlines, timeline, randomSeed);
		historySize = (int) Math.ceil(timeline.getTotalTime() * 0.05);
    }


    /**
     * Each round this method gets called and ask you to accept or offer. The first party in
     * the first round is a bit different, it can only propose an offer.
     *
     * @param validActions Either a list containing both accept and offer or only offer.
     * @return The chosen action.
     */
    @Override
    public Action chooseAction(List<Class> validActions) {
        if (!validActions.contains(Accept.class) || !shouldAccept(lastBid)) {
			lastBid = new AbstractMap.SimpleEntry<Object, Bid>(this, generateBid());
			return new Offer(lastBid.getValue());
        } else {
            return new Accept();
        }
    }


    /**
     * Checks if you should accept following bid.
     * @param bid bid to check
     * @return true if the bid is acceptable, false otherwise
     */
    private boolean shouldAccept(Map.Entry<Object, Bid> bid){
        double endTime = timeline.getTotalTime() - (timeline.getType().name().equals("Rounds") ? 1 : 0.2);
        if(timeline.getCurrentTime() >= endTime) return true;
		double concessionRate = (1-getTargetUtility())/historySize;
		return getUtility(bid.getValue()) >= 1 - concessionRate*Long.bitCount(concessions.get(bid.getKey()));
    }


	/**
	 * All offers proposed by the other parties will be received as a message.
	 * Messages of Offer or Accept are passed to updateOpponentModel().
	 *
	 * @param sender The party that did the action.
	 * @param action The action that party did.
	 */
	@Override
	public void receiveMessage(Object sender, Action action) {
		super.receiveMessage(sender, action);
		if(action instanceof Offer) {
			lastBid = new AbstractMap.SimpleEntry<>(sender, ((Offer) action).getBid());
			updateOpponentModel(sender, lastBid, false);
		} else if(action instanceof Accept) {
			updateOpponentModel(sender, lastBid, true);
		}
	}


	/**
	 * Updates opponent model after receiving a bid. All messages of type Accept are considered to be concessions.
	 * An Offer is considered a concession only if it differs from the last Offer by this agent.
	 * @param agent the sender of the bid
	 * @param bid the bid
	 */
	private void updateOpponentModel(Object agent, Map.Entry<Object, Bid> bid, boolean isAccept) {
		BayesianOpponentModelScalable model = opponentModels.get(agent);
		if(model == null){
			model = new BayesianOpponentModelScalable(utilitySpace);
			opponentModels.put(agent, model);
			concessions.put(agent, 0L);
			lastBids.put(agent, bid.getValue());
		}

		try {
			model.updateBeliefs(bid.getValue());

			if(! concessions.containsKey(bid.getKey())) return;
			//Next level bit hacks
			long out = (concessions.get(bid.getKey())<<1) + (lastBids.get(agent).equals(bid.getValue()) && !isAccept ? 0 : 1);
			concessions.put(agent, out & ((1<<historySize) - 1)); //Keep history of only last historySize rounds.
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * Calculates average expected utility between the opponents based on their model
	 * @param bid the bid to evaluate
	 * @return average expected utility
	 */
	private double getAverageOpponentUtility(Bid bid){
		double sum = 0;
		for(BayesianOpponentModelScalable model : opponentModels.values()){
			try {
				sum += model.getExpectedUtility(bid);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return sum / opponentModels.size();
	}


    /**
     * Calculates target value for bid as function of current time
     * @return double in [reservationValue, 1]
     */
	private double getTargetUtility() {
        double endTime = timeline.getTotalTime() - (timeline.getType().name().equals("Rounds") ? 1 : 0.2);
        return reservationValue + (1-reservationValue)*(1 - Math.pow(timeline.getCurrentTime()/endTime, beta));
	}


	/**
	 * Generates max utility bid if this is the first round, otherwise generates best acceptable bid
	 * @return the generated bid
	 */
	private Bid generateBid() {
		double bestAvgUtil = 0, bestOwnUtil = 0, minAcceptable = getTargetUtility();
		ArrayList<Bid> acceptableBids = new ArrayList<>();
		Bid bestBid = null, maxBid = null;

		BidIterator bidIterator = new BidIterator(utilitySpace.getDomain());
		while(bidIterator.hasNext()) {
			Bid thisBid = bidIterator.next();
			double thisOwnUtil = getUtility(thisBid);

			if(getUtility(maxBid) < getUtility(thisBid)) maxBid = thisBid;
			if(thisOwnUtil >= minAcceptable) {
				acceptableBids.add(thisBid);
				double thisAvgUtil = getAverageOpponentUtility(thisBid);
				if (thisAvgUtil * thisOwnUtil > bestAvgUtil * bestOwnUtil) {
					bestAvgUtil = thisAvgUtil;
					bestOwnUtil = thisOwnUtil;
					bestBid = thisBid;
				}
			}
		}

		if(bestBid == null) return maxBid;
		else if(1 - Math.pow(timeline.getCurrentTime()/timeline.getTotalTime(), 2) < Math.random()) return bestBid;
		else return acceptableBids.get((int) (Math.random() * acceptableBids.size()));
    }
}
