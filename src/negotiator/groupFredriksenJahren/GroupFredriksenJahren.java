package negotiator.groupFredriksenJahren;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import agents.bayesianopponentmodel.BayesianOpponentModelScalable;
import negotiator.Bid;
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
    private Bid lastBid;
    private Bid myLastBid;
    private double targetUtility = 1.0f;
    private HashMap<Object, BayesianOpponentModelScalable> opponentModels = new HashMap<Object, BayesianOpponentModelScalable>();

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
        if (!validActions.contains(Accept.class) || !shouldAccept()) {
            return new Offer(generateBid());
        } else {
            return new Accept();
        }
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
			lastBid = ((Offer) action).getBid();
			updateOpponentModel(sender, lastBid);
		}
	}


	/**
	 * Updates opponent model after receiving a bid
	 * @param agent the sender of the bid
	 * @param bid the bid
	 */
	private void updateOpponentModel(Object agent, Bid bid) {
		BayesianOpponentModelScalable model = opponentModels.get(agent);
		if(model == null){
			model = new BayesianOpponentModelScalable(this.utilitySpace);
			opponentModels.put(agent, model);
		}

		try {
			model.updateBeliefs(bid);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * Calculates average expected utility between the opponents based on their model
	 * @param bid the bid to evaluate
	 * @return average expected utility
	 */
	private double averageOpponentUtility(Bid bid){
		double sum = 0;
		for(BayesianOpponentModelScalable model : opponentModels.values()){
			try {
				sum += model.getExpectedUtility(bid);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return sum / (double) opponentModels.size();
	}


    private boolean shouldAccept(){
        calculateTargetUtility();
        boolean incomingBidBetter = false;
        if(myLastBid != null){
            incomingBidBetter = getUtility(lastBid) >= getUtility(myLastBid);
        }
        boolean higherThanReservationValue = getUtility(lastBid) >= targetUtility;
        return incomingBidBetter || higherThanReservationValue;
    }


    private void calculateTargetUtility(){
        if(this.timeline.getCurrentTime() <= (int)Math.ceil(((float)28/(float)36)*this.timeline.getTotalTime())) {
            float a = (float)((0.875 - 1)/(int)Math.ceil(((float)28/(float)36)*this.timeline.getTotalTime()));
            targetUtility = a*this.timeline.getCurrentTime() + 1;

        } else if(this.timeline.getCurrentTime() > (int)Math.ceil(((float)28/(float)36)*this.timeline.getTotalTime()) && this.timeline.getCurrentTime() < (int)(((float)35/(float)36)*this.timeline.getTotalTime())){
            float a = (float)((0.625 - 0.875)/(int)Math.ceil(((float)7/(float)36)*this.timeline.getTotalTime()));
            targetUtility = a*(this.timeline.getCurrentTime()-(int)(((float)28/(float)36)*this.timeline.getTotalTime())) + 0.875;

        } else {
            float a = ((0.0f - 0.625f)/(int)Math.ceil(((float)1/(float)36)*this.timeline.getTotalTime()));
            targetUtility = a*(this.timeline.getCurrentTime()-(int)(((float)35/(float)36)*this.timeline.getTotalTime())) + 0.625;
        }
    }


    private Bid generateBid(){
        Bid bid = null;
        if(myLastBid == null){
            try {
                bid = this.utilitySpace.getMaxUtilityBid();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else{
            double previousUtility = getUtility(myLastBid);
            for(int i = 0; i < 5 && bid == null; i++){
                bid = getBidBetterThan(previousUtility - (0.05*i));
            }
        }
        myLastBid = bid;
        return bid;
    }

    private Bid getBidBetterThan(double previousUtility){
        Bid bid;
        int counter = 0;
        while(true){
            bid = generateRandomBid();
            if(getUtility(bid) >= previousUtility && getUtility(bid) >= averageOpponentUtility(bid)){
                break;
            }
            counter++;
            if(counter > 1000){
                bid = null;
                break;
            }
        }
        return bid;
    }
}
