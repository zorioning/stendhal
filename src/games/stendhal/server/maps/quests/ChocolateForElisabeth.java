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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import games.stendhal.common.Rand;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DecreaseKarmaAction;
import games.stendhal.server.entity.npc.action.DropItemAction;
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
 * Quest to buy chocolate for a little girl called 伊丽莎白.
 * Ask her mother Carey for a quest and she will ask you to get some chocolate for her daughter.
 * Get some chocolate and bring it to 伊丽莎白.
 *
 * @author Vanessa Julius idea by miasma
 *
 *
 * QUEST: Chocolate for 伊丽莎白
 *
 * PARTICIPANTS:
 * <ul>
 * <li>伊丽莎白 (a young girl who loves chocolate)</li>
 * <li>Carey (伊丽莎白's mother)</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>伊丽莎白 asks you to bring her a 巧克力棒.</li>
 * <li>Get some chocolate .</li>
 * <li>Ask Carey if she allows you to give the chocolate to her daughter.</li>
 * <li>Make 伊丽莎白 happy and get a lovely reward.</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>a random flower</li>
 * <li>500 XP</li>
 * <li>12 karma total (2 + 10)</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>Every 60 minutes</li>
 * </ul>
 */
public class ChocolateForElisabeth extends AbstractQuest {

	// constants
	private static final String QUEST_SLOT = "chocolate_for_elisabeth";

	/** The delay between repeating quests. */
	private static final int REQUIRED_MINUTES = 60;
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	private void chocolateStep() {
		final SpeakerNPC npc = npcs.get("伊丽莎白");

		// first conversation with 伊丽莎白.
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestNotStartedCondition(QUEST_SLOT), new QuestNotInStateCondition(QUEST_SLOT, "rejected")),
				ConversationStates.ATTENDING,
				"我已经忘记了上次那个好吃的 #巧克力 是什么味道了...",
				null);

		npc.addReply("巧克力", "妈妈说, 刺客学校里有巧克力, 那里很 #危险 ,她还说 Ados 城里也有人卖...");

		npc.addReply("危险", "一些强盗埋伏在上学的路上, 还有刺客在那里守卫, 所以妈妈和我只能呆在 Kirdneh ,可能这里安全点...");

		// player is supposed to speak to mummy now
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "start"), new PlayerHasItemWithHimCondition("巧克力棒")),
				ConversationStates.IDLE,
				"妈妈想知道我向谁要的巧克力 :(",
				null);

		// player didn't get chocolate, meanie
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "start"), new NotCondition(new PlayerHasItemWithHimCondition("巧克力棒"))),
				ConversationStates.ATTENDING,
				"我希望有人能马上给我巧克力吃...:(",
				null);

		// player got chocolate and spoke to mummy
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "mummy"), new PlayerHasItemWithHimCondition("巧克力棒")),
				ConversationStates.QUESTION_1,
				"厉害! 这是给我的巧克力?",
				null);

		// player spoke to mummy and hasn't got chocolate
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "mummy"), new NotCondition(new PlayerHasItemWithHimCondition("巧克力棒"))),
				ConversationStates.ATTENDING,
				"我希望有人能马上给我巧克力吃...:(",
				null);

		// player is in another state like eating
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStartedCondition(QUEST_SLOT), new QuestNotInStateCondition(QUEST_SLOT, "start"), new QuestNotInStateCondition(QUEST_SLOT, "mummy")),
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
				"我真的想吃巧克力. 我喜欢成块的深棕色那种, 或者是带白花的, 带花纹的更好. 你能带给我吗?",
				null);

		// shouldn't happen
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"上次你带的 巧克力棒 我很喜欢,谢谢!",
				null);

		// player can repeat quest
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "eating;"), new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)),
				ConversationStates.QUEST_OFFERED,
				"期待你下次带的 巧克力棒 更好吃, 你还能带个不同口味的来吗?",
				null);

		// player can't repeat quest
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "eating;"), new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES))),
				ConversationStates.ATTENDING,
				"吃了太多巧克力. 我觉得要吃出病了.",
				null);

		// player should be bringing chocolate not asking about the quest
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT), new NotCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "eating;"))),
				ConversationStates.ATTENDING,
				"哇啊啊! 我的巧克力在哪 ...",
				null);

		// Player agrees to get the chocolate
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
				"Ok, 我会等着妈妈找其他的好心人帮忙...",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		// Player has got 巧克力棒 and spoken to mummy
		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("巧克力棒"));
		reward.add(new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				// pick a random flower
				String rewardClass = Rand.rand(Arrays.asList("雏菊","马蹄莲","三色堇"));

				final StackableItem item = (StackableItem) SingletonRepository.getEntityManager().getItem(rewardClass);
				item.setQuantity(1);
				player.equipOrPutOnGround(item);
				player.notifyWorldAboutChanges();
			}
		});
		reward.add(new IncreaseXPAction(500));
		reward.add(new SetQuestAction(QUEST_SLOT, "eating;"));
		reward.add(new SetQuestToTimeStampAction(QUEST_SLOT,1));
		reward.add(new IncreaseKarmaAction(10.0));
		reward.add(new InflictStatusOnNPCAction("巧克力棒"));

		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new PlayerHasItemWithHimCondition("巧克力棒"),
				ConversationStates.ATTENDING,
				"太谢谢你了! 你真是好人. 给, 这些鲜花送给你.",
				new MultipleActions(reward));


		// player did have chocolate but put it on ground after question?
		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new NotCondition(new PlayerHasItemWithHimCondition("巧克力棒")),
				ConversationStates.ATTENDING,
				"喂, 我的巧克力没了?!",
				null);

		// Player says no, they've lost karma
		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.IDLE,
				"Waaaaaa! 你这个大骗子.",
				new DecreaseKarmaAction(5.0));
	}

	private void meetMummyStep() {
		final SpeakerNPC mummyNPC = npcs.get("Carey");

		// player speaks to mummy before 伊丽莎白
		mummyNPC.add(ConversationStates.IDLE,
					ConversationPhrases.GREETING_MESSAGES,
					new AndCondition(new GreetingMatchesNameCondition(mummyNPC.getName()),
							new QuestNotStartedCondition(QUEST_SLOT)),
					ConversationStates.ATTENDING, "Hello, 很高兴认识你.",
					null);

		// player is supposed to begetting chocolate
		mummyNPC.add(ConversationStates.IDLE,
					ConversationPhrases.GREETING_MESSAGES,
					new AndCondition(new GreetingMatchesNameCondition(mummyNPC.getName()),
							new QuestInStateCondition(QUEST_SLOT, "start")),
					ConversationStates.ATTENDING,
					"你见过我女儿 伊丽莎白 了吧. 你看着很好,一定是个好心人 , 由于我不够 #强大, 希望你能给她带一块 巧克力棒.",
					new SetQuestAction(QUEST_SLOT, "mummy"));

		mummyNPC.addReply("强大", "我想尽快给 伊丽莎白 找点巧克力吃, 但我没办法穿过 #那里 的小偷和强盗的围堵.");

		mummyNPC.addReply("那里", "他们在 Ados 城堡附近活动. 滤主那里! 不过我还听到 #某些人 销售巧克力棒.");

		mummyNPC.addReply("某些人", "我也没见过那家伙, 因为他看着真的...好吧, 他在 Ados 的某处工作, 但我不想去.");

		// any other state
		mummyNPC.add(ConversationStates.IDLE,
					ConversationPhrases.GREETING_MESSAGES, new GreetingMatchesNameCondition(mummyNPC.getName()), true,
					ConversationStates.ATTENDING, "欢迎光临.", null);
	}
	@Override
	public void addToWorld() {
		fillQuestInfo(
				"伊丽莎白的巧克力",
				"甜甜的巧克力! 没它怎么活! 伊丽莎白 爱死它了...",
				true);
		chocolateStep();
		meetMummyStep();
	}


	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("伊丽莎白 是个甜美的小女孩, 和家人一起生活在 Kirdneh.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("我不喜欢卖萌的女孩");
		}
		if (player.isQuestInState(QUEST_SLOT, "start","mummy") || isCompleted(player)) {
			res.add("小 伊丽莎白 想吃 巧克力棒.");
		}
		if (player.isQuestInState(QUEST_SLOT, "start","mummy") && player.isEquipped("巧克力棒") || isCompleted(player)) {
			res.add("我找到了 伊丽莎白 喜欢吃的 巧克力棒.");
		}
        if ("mummy".equals(questState) || isCompleted(player)) {
            res.add("我和 Carey, 伊丽莎白 的妈妈谈谈, 她同意我帮她女儿找 巧克力棒.");
        }
        if (isCompleted(player)) {
            if (isRepeatable(player)) {
                res.add("我给 伊丽莎白 带了些巧克力,她送给我一些花作为回报,可能现在她更喜欢巧克力一些.");
            } else {
                res.add("伊丽莎白 吃着我带来的巧克力棒, 并送给我一些鲜花");
            }
		}
		return res;
	}
	@Override
	public String getName() {
		return "ChocolateForElisabeth";
	}

	// Getting to Kirdneh is not too feasible till this level
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
		return Region.KIRDNEH;
	}
	@Override
	public String getNPCName() {
		return "伊丽莎白";
	}
}
