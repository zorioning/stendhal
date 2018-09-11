/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DecreaseKarmaAction;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.InflictStatusOnNPCAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * Quest to buy ice cream for a little girl.
 * You have to get approval from her mother before giving it to her
 *
 * @author kymara
 *
 *
 * QUEST: Ice Cream for Annie
 *
 * PARTICIPANTS:
 * <ul>
 * <li> 安妮琼斯 (a little girl playing in Kalavan City Gardens) </li>
 * <li> Mrs Jones (Annie's mum) </li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li> Annie asks you for an 冰淇淋. </li>
 * <li> You buy 冰淇淋 from Sam who is nearby. </li>
 * <li> Speak to Mrs Jones, Annie's mum. </li>
 * <li> Now give the 冰淇淋 to Annie. </li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>a <item>present</item></li>
 * <li>500 XP</li>
 * <li>12 karma total (2 + 10)</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>Every 60 minutes</li>
 * </ul>
 */
public class IcecreamForAnnie extends AbstractQuest {

	// constants
	private static final String QUEST_SLOT = "icecream_for_annie";

	/** The delay between repeating quests. */
	private static final int REQUIRED_MINUTES = 60;

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	private void icecreamStep() {
		final SpeakerNPC npc = npcs.get("安妮琼斯");

		// first conversation with annie. be like [strike]every good child[/strike] kymara was when she was little and advertise name and age.
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestNotStartedCondition(QUEST_SLOT),
						new QuestNotInStateCondition(QUEST_SLOT, "rejected")),
				ConversationStates.ATTENDING,
				"你好, 我是安妮. 我5岁了",
				null);

		// player is supposed to speak to mummy now
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "start"),
						new PlayerHasItemWithHimCondition("冰淇淋")),
				ConversationStates.IDLE,
				"妈妈说我一定不要和你说话. 你是陌生人.",
				null);

		// player didn't get ice cream, meanie
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "start"),
						new NotCondition(new PlayerHasItemWithHimCondition("冰淇淋"))),
				ConversationStates.ATTENDING,
				"你好. 我饿了.",
				null);

		// player got ice cream and spoke to mummy
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "mummy"),
						new PlayerHasItemWithHimCondition("冰淇淋")),
				ConversationStates.QUESTION_1,
				"好吃! 那个冰淇淋是给我的吗?",
				null);

		// player spoke to mummy and hasn't got ice cream
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "mummy"),
						new NotCondition(new PlayerHasItemWithHimCondition("冰淇淋"))),
				ConversationStates.ATTENDING,
				"你好. 我饿了.",
				null);

		// player is in another state like eating
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStartedCondition(QUEST_SLOT),
						new QuestNotInStateCondition(QUEST_SLOT, "start"),
						new QuestNotInStateCondition(QUEST_SLOT, "mummy")),
				ConversationStates.ATTENDING,
				"你好.",
				null);

		// player rejected quest
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "rejected")),
				ConversationStates.ATTENDING,
				"你好.",
				null);

		// player asks about quest for first time (or rejected)
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"我饿了! 我喜欢冰淇淋, 是香草味, 带巧克力片的那种. 你能给我一个吗?",
				null);

		// shouldn't happen
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"我吃饱了，谢谢你!",
				null);

		// player can repeat quest
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "eating;"), new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)),
				ConversationStates.QUEST_OFFERED,
				"希望下个冰淇淋不要太腻. 你能再拿一个吗?",
				null);

		// player can't repeat quest
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "eating;"), new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES))),
				ConversationStates.ATTENDING,
				"吃得太多了. 我觉得要病了.",
				null);

		// player should be bringing ice cream not asking about the quest
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT), new NotCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "eating;"))),
				ConversationStates.ATTENDING,
				"哇～～! 我的冰淇淋在哪....",
				null);

		// Player agrees to get the ice cream
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"谢谢你!",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 2.0));

		// Player says no, they've lost karma
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.IDLE,
				"好吧, 我问妈妈要.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		// Player has got ice cream and spoken to mummy
		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("冰淇淋"));
		reward.add(new EquipItemAction("present"));
		reward.add(new IncreaseXPAction(500));
		reward.add(new SetQuestAction(QUEST_SLOT, "eating;"));
		reward.add(new SetQuestToTimeStampAction(QUEST_SLOT,1));
		reward.add(new IncreaseKarmaAction(10.0));
		reward.add(new InflictStatusOnNPCAction("冰淇淋"));

		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new PlayerHasItemWithHimCondition("冰淇淋"),
				ConversationStates.ATTENDING,
				"太感谢了! 你真是个好人. 给, 这是送你的礼物.",
				new MultipleActions(reward));

		// player did have ice cream but put it on ground after question?
		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new NotCondition(new PlayerHasItemWithHimCondition("冰淇淋")),
				ConversationStates.ATTENDING,
				"嗨, 我的冰淇淋在哪?!",
				null);

		// Player says no, they've lost karma
		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.IDLE,
				"哇啊! 你是个大骗子.",
				new DecreaseKarmaAction(5.0));
	}

	private void meetMummyStep() {
		final SpeakerNPC mummyNPC = npcs.get("Mrs Jones");

		// player speaks to mummy before annie
		mummyNPC.add(ConversationStates.IDLE,
					ConversationPhrases.GREETING_MESSAGES,
					new AndCondition(new GreetingMatchesNameCondition(mummyNPC.getName()),
							new QuestNotStartedCondition(QUEST_SLOT)),
					ConversationStates.ATTENDING, "你好, 很高兴见到你.",
					null);

		// player is supposed to begetting ice cream
		mummyNPC.add(ConversationStates.IDLE,
					ConversationPhrases.GREETING_MESSAGES,
					new AndCondition(new GreetingMatchesNameCondition(mummyNPC.getName()),
							new QuestInStateCondition(QUEST_SLOT, "start")),
					ConversationStates.ATTENDING,
					"你好, 我明白你已见到我女儿安妮了. 我希望她不要太过份. 看起来你是个好人.",
					new SetQuestAction(QUEST_SLOT, "mummy"));

		// any other state
		mummyNPC.add(ConversationStates.IDLE,
					ConversationPhrases.GREETING_MESSAGES,
					new GreetingMatchesNameCondition(mummyNPC.getName()), true,
					ConversationStates.ATTENDING, "又见面了.", null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Annie的冰淇淋",
				"在炎炎烈日下的广场, 对于像安妮这样的女孩, 最能她惊喜的事莫过于给她一个清凉的冰淇淋了.",
				true);
		icecreamStep();
		meetMummyStep();
	}


	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("安妮琼斯 是个甜美的小女孩, 她在 Kalavan 城市花园中玩耍.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("我不喜欢卖萌的小女孩.");
		}
		if (player.isQuestInState(QUEST_SLOT, "start","mummy") || isCompleted(player)) {
			res.add("小安妮想要冰淇淋.");
		}
		if (player.isQuestInState(QUEST_SLOT, "start","mummy") && player.isEquipped("冰淇淋") || isCompleted(player)) {
			res.add("我找到了 安妮 喜欢吃的冰淇淋.");
		}
        if ("mummy".equals(questState) || isCompleted(player)) {
            res.add("我和 Jones 女士谈了谈, 她同意我把冰淇淋给她女儿吃.");
        }
        if (isCompleted(player)) {
            if (isRepeatable(player)) {
                res.add("我把冰淇淋给了 安妮, 她给了我一个礼物. 可能她现在喜欢其它东西了.");
            } else {
                res.add("安妮吃着我给的冰淇淋, 她给我一个礼物作为回报.");
            }
		}
		return res;
	}

	@Override
	public String getName() {
		return "IcecreamForAnnie";
	}

	// Getting to Kalavan is not too feasible till this level
	@Override
	public int getMinLevel() {
		return 10;
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"eating;"),
				 new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)).fire(player,null, null);
	}

	@Override
	public boolean isCompleted(final Player player) {
		return new QuestStateStartsWithCondition(QUEST_SLOT,"eating;").fire(player, null, null);
	}

	@Override
	public String getRegion() {
		return Region.KALAVAN;
	}
	@Override
	public String getNPCName() {
		return "安妮琼斯";
	}
}
