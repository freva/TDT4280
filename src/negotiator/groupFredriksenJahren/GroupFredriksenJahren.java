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
    private HashMap<Object, BayesianOpponentModelScalable> opponentModels = new HashMap<Object, BayesianOpponentModelScalable>();
	private HashMap<Object, Integer> concessions = new HashMap<Object, Integer>();
	private HashMap<Object, Bid> lastBids = new HashMap<Object, Bid>();
    private Map.Entry<Object, Bid> lastBid;
    private static final double reservationValue = 0.6;
    private static final double beta = Math.log(1-(0.875-reservationValue)/(1-reservationValue)) / Math.log(8d/9);

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
     * Checks if you should accept following bid
     * @param bid bid to check
     * @return true if the bid is acceptable, false otherwise
     */
    private boolean shouldAccept(Map.Entry<Object, Bid> bid){
        return getUtility(bid.getValue()) >= getTargetUtility(concessions.get(bid.getKey()));
    }


	/**
	 * All offers proposed by the other parties will be received as a message.
	 * You can use this information to your advantage, for example to predict their utility.
	 *
	 * @param sender The party that did the action.
	 * @param action The action that party did.
	 */
	@Override
	public void receiveMessage(Object sender, Action action) {
		super.receiveMessage(sender, action);
		if(action instanceof Offer) {
			lastBid = new AbstractMap.SimpleEntry<Object, Bid>(sender, ((Offer) action).getBid());
			updateOpponentModel(sender, lastBid);
		} else if(action instanceof Accept) {
			updateOpponentModel(sender, lastBid);
		}
	}


	/**
	 * Updates opponent model after receiving a bid
	 * @param agent the sender of the bid
	 * @param bid the bid
	 */
	private void updateOpponentModel(Object agent, Map.Entry<Object, Bid> bid) {
		BayesianOpponentModelScalable model = opponentModels.get(agent);
		if(model == null){
			model = new BayesianOpponentModelScalable(utilitySpace);
			opponentModels.put(agent, model);
			concessions.put(agent, 0);
			lastBids.put(agent, bid.getValue());
		}

		try {
			model.updateBeliefs(bid.getValue());

			if(! lastBids.get(agent).equals(bid.getValue()))
				concessions.put(agent, concessions.get(agent)+1);
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
        return getTargetUtility(timeline.getCurrentTime());
    }


	private double getTargetUtility(double numTimes) {
		return reservationValue + (1-reservationValue)*(1 - Math.pow(numTimes/timeline.getTotalTime(), beta));
	}


	/**
	 * Generates max utility bid if this is the first round, otherwise generates best acceptable bid
	 * @return the generated bid
	 */
	private Bid generateBid() {
		if(timeline.getCurrentTime() < 5) {
			try {
				return utilitySpace.getMaxUtilityBid();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}


		double maxAvgUtil = 0, maxOwnUtil = 0, minAcceptable = getTargetUtility();
		ArrayList<Bid> acceptableBids = new ArrayList<Bid>();
		Bid maxBid = null;

		BidIterator bidIterator = new BidIterator(utilitySpace.getDomain());
		if(!bidIterator.hasNext()) return null;
		else {
			while(bidIterator.hasNext()) {
				Bid thisBid = bidIterator.next();
				double thisOwnUtil = Math.pow(getUtility(thisBid), 2);

				if(thisOwnUtil > minAcceptable) {
					acceptableBids.add(thisBid);
					double thisAvgUtil = getAverageOpponentUtility(thisBid);
					if (thisAvgUtil * thisOwnUtil > maxAvgUtil * maxOwnUtil) {
						maxAvgUtil = thisAvgUtil;
						maxOwnUtil = thisOwnUtil;
						maxBid = thisBid;
					}
				}
			}

			if(acceptableBids.size() == 0 || 1 - Math.pow(timeline.getCurrentTime()/timeline.getTotalTime(), 2) < Math.random()) return maxBid;
			else return acceptableBids.get((int) (Math.random() * acceptableBids.size()));
		}
    }
}
