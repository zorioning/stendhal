package games.stendhal.server.entity.npc.action;

import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.annotations.Dev;
import games.stendhal.server.core.config.annotations.Dev.Category;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.behaviour.impl.RepairerBehaviour;
import games.stendhal.server.entity.player.Player;

/**
 * Behaviour action for repairing npcs
 *
 * @author madmetzger
 */
@Dev(category=Category.IGNORE)
public class RepairingBehaviourAction extends AbstractBehaviourAction<RepairerBehaviour>{

	public RepairingBehaviourAction(RepairerBehaviour repairerBehaviour) {
		super(repairerBehaviour, "repair", "repair");
	}

	@Override
	public void fireRequestOK(ItemParserResult res, Player player,
			Sentence sentence, EventRaiser npc) {
		String chosen = res.getChosenItemName();
		if(behaviour.canDealWith(chosen)) {
			npc.say("修好要花些钱");
		} else {
			npc.say("抱歉, 我不能修好你的 " + res.getChosenItemName() + ".");
		}
	}

	@Override
	public void fireRequestError(ItemParserResult res, Player player,
			Sentence sentence, EventRaiser npc) {
		npc.say("抱歉, 你说的我不太明白.");
	}

}
