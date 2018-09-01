/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
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

import games.stendhal.common.Level;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.rp.StendhalRPAction;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.creature.Sheep;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.maps.semos.city.SheepBuyerNPC.SheepBuyerSpeakerNPC;

/**
 * QUEST: Sheep Growing for 尼世亚
 *
 * PARTICIPANTS:
 * <ul>
 * <li>尼世亚 (the sheep seller in 塞门镇 village)</li>
 * <li>赛特 (the sheep buyer in 塞门镇)</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>尼世亚 asks you to grow a sheep.</li>
 * <li>Sheep grows to weight 100.</li>
 * <li>Sheep is handed over to 赛特.</li>
 * <li>尼世亚 thanks you.</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>Maximum of (XP to level 2) or (30XP)</li>
 * <li>Karma: 10</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>None</li>
 * </ul>
 */
public class SheepGrowing extends AbstractQuest {

    private static final String QUEST_SLOT = "sheep_growing";
    private static final String TITLE = "尼世亚的养羊计划";
    private static final int MIN_XP_GAIN = 30;

    @Override
    public void addToWorld() {
        fillQuestInfo(
                TITLE,
                "尼世亚, 一个卖羊人，答应给 赛特 一只羊。" +
                    "由于他很忙，需要人为他把一只羊养大后，" +
                    "交给 赛特.",
                true);
        generalInformationDialogs();
        preparePlayerGetsSheepStep();
        preparePlayerHandsOverSheepStep();
        preparePlayerReturnsStep();
    }

    @Override
    public String getSlotName() {
        return QUEST_SLOT;
    }

    @Override
    public List<String> getHistory(final Player player) {
        final List<String> res = new LinkedList<String>();
        if (!player.hasQuest(QUEST_SLOT)) {
            return res;
        }
        res.add("尼世亚问我是否可以帮他带大一只羊");

        final String questState = player.getQuest(QUEST_SLOT);
        if (questState.equals("rejected")) {
            res.add("我告诉尼世亚还有其他的事要做，等我以后有空再帮他.");
        }
        if (player.isQuestInState(QUEST_SLOT, "start", "handed_over", "done")) {
            res.add("我答应帮他养大一只羊。");
        }
        if (player.isQuestInState(QUEST_SLOT, "handed_over", "done")) {
            res.add("我把羊养大并交给 赛特。现在要向 尼世亚 复命了");
        }
        if(questState.equals("done")) {
            res.add("我把消息带给尼世亚。他很开心我能帮忙.");
        }
        return res;
    }

    @Override
    public String getName() {
        return TITLE;
    }

    /**
     * General information for the player related to the quest.
     */
    private void generalInformationDialogs() {
        final SpeakerNPC npc = npcs.get("尼世亚");

        npc.add(ConversationStates.ATTENDING, "赛特", null, ConversationStates.ATTENDING, "赛特是 塞门镇 的收羊人。" +
                "顺着这条路往东，你会找到他。", null);
        npc.add(ConversationStates.QUEST_OFFERED, "赛特", null, ConversationStates.QUEST_OFFERED, "赛特是 塞门镇 的收羊人。" +
                "顺着这条路往东，你会找到他。", null);

        List<String> berryStrings = new ArrayList<String>();
        berryStrings.add("红树莓");
        berryStrings.add("树莓");
        berryStrings.add("羊食");
        npc.addReply(berryStrings, "羊喜欢吃长在矮树丛中的 红树莓");

        npc.addReply("sheep", "本人销售毛茸茸的羊，这是我的 #工作 .");
    }
    /**
     * The step where the player speaks with 尼世亚 about quests and gets the sheep.
     */
    private void preparePlayerGetsSheepStep() {
        final SpeakerNPC npc = npcs.get("尼世亚");

        // If quest is not done or started yet ask player for help (if he does not have a sheep already)
        ChatCondition playerHasNoSheep = new ChatCondition() {
            @Override
			public boolean fire(Player player, Sentence sentence, Entity npc) {
                return !player.hasSheep();
            }
        };
        npc.add(
                ConversationStates.ATTENDING,
                ConversationPhrases.QUEST_MESSAGES,
                new AndCondition(playerHasNoSheep,
                        new QuestNotInStateCondition(QUEST_SLOT, "start"),
                        new QuestNotInStateCondition(QUEST_SLOT, "handed_over"),
                        new QuestNotInStateCondition(QUEST_SLOT, "done")),
                ConversationStates.QUEST_OFFERED,
                "最近因为我的羊的事情一直非常忙. " +
                "你想带走一只羊并照料它，然后再转手给 #赛特 吗? " +
                "你只需带着羊走到红色树莓附近，直到羊的体重达到 " + Sheep.MAX_WEIGHT + " . " +
                "你做的到吗?",
                new SetQuestAction(QUEST_SLOT, "asked"));

        // If quest is offered and player says no reject the quest
        npc.add(
                ConversationStates.QUEST_OFFERED,
                ConversationPhrases.NO_MESSAGES,
                new AndCondition(playerHasNoSheep,
                        new QuestInStateCondition(QUEST_SLOT, "asked")),
                ConversationStates.IDLE,
                "好吧... 这些天我不得不加倍努力...",
                new SetQuestAction(QUEST_SLOT, "rejected"));

        // If quest is still active but not handed over do not give an other sheep to the player
        npc.add(
                ConversationStates.ATTENDING,
                ConversationPhrases.QUEST_MESSAGES,
                new AndCondition(
                    new QuestActiveCondition(QUEST_SLOT),
                    new NotCondition(new QuestInStateCondition(QUEST_SLOT, "asked")),
                    new NotCondition(new QuestInStateCondition(QUEST_SLOT, "handed_over"))),
                ConversationStates.ATTENDING,
                "我已经给了你一只羊. " +
                "如果你把它丢弃了，我可以卖你一只新的. 只用对我说 #buy #sheep.",
                null);

        // If quest is offered and player says yes, give a sheep to him.
        List<ChatAction> sheepActions = new LinkedList<ChatAction>();
        sheepActions.add(new SetQuestAction(QUEST_SLOT, "start"));
        sheepActions.add(new ChatAction() {
            @Override
			public void fire(Player player, Sentence sentence, EventRaiser npc) {
                final Sheep sheep = new Sheep(player);
                StendhalRPAction.placeat(npc.getZone(), sheep, npc.getX(), npc.getY() + 1);
            }
        });
        npc.add(
                ConversationStates.QUEST_OFFERED,
                ConversationPhrases.YES_MESSAGES,
                new AndCondition(playerHasNoSheep,
                        new QuestInStateCondition(QUEST_SLOT, "asked")),
                ConversationStates.IDLE,
                "谢谢! *smiles* 这是你的毛绒绒的养女，要好好待她. " +
                "如果她死了，或者你丢弃了她，还想再弄一只的话，只能向我重新购买. " +
                "Oh... 不出意外的话，羊长大后可以卖给 赛特. 只要向他说话就行了.",
                new MultipleActions(sheepActions));
    }
    /**
     * The step where the player goes to 赛特 to give him the grown up sheep.
     */
    private void preparePlayerHandsOverSheepStep() {
        // Remove action
        final List<ChatAction> removeSheepAction = new LinkedList<ChatAction>();
        removeSheepAction.add(new ChatAction() {
            @Override
			public void fire(Player player, Sentence sentence, EventRaiser npc) {
                // remove sheep
                final Sheep sheep = player.getSheep();
                if(sheep != null) {
                    player.removeSheep(sheep);
                    player.notifyWorldAboutChanges();
                    if(npc.getEntity() instanceof SheepBuyerSpeakerNPC) {
                        ((SheepBuyerSpeakerNPC)npc.getEntity()).moveSheep(sheep);
                    } else {
                        // only to prevent that an error occurs and the sheep does not disappear
                        sheep.getZone().remove(sheep);
                    }
                } else {
                    // should not happen
                    npc.say("什么? 什么羊? 你丢了什么东西吗?");
                    npc.setCurrentState(ConversationStates.IDLE);
                    return;
                }
            }
        });
        removeSheepAction.add(new SetQuestAction(QUEST_SLOT, "handed_over"));

        // Hand-Over condition
        ChatCondition playerHasFullWeightSheep = new ChatCondition() {
            @Override
			public boolean fire(Player player, Sentence sentence, Entity npc) {
                return player.hasSheep()
                    && player.getSheep().getWeight() >= Sheep.MAX_WEIGHT;
            }
        };

        // 赛特 asks for sheep
        final SpeakerNPC npc = npcs.get("赛特");
        npc.add(
                ConversationStates.IDLE,
                ConversationPhrases.GREETING_MESSAGES,
                new AndCondition(
                        new QuestInStateCondition(QUEST_SLOT,"start"),
                        playerHasFullWeightSheep),
                ConversationStates.QUEST_ITEM_BROUGHT,
                "Hello. 你后面的羊很好很健康的样子! 这是给我的吗?",
                null);

        npc.add(
                ConversationStates.IDLE,
                ConversationPhrases.GREETING_MESSAGES,
                new AndCondition(
                        new QuestInStateCondition(QUEST_SLOT,"start"),
                        new NotCondition(playerHasFullWeightSheep)),
                ConversationStates.IDLE,
                "Hello. 你应该从 尼世亚 处弄一只羊给我, 他欠我一个！但我想要一只足量的。你弄到一只时再回来。再见!",
                null);

        // Player answers yes - Sheep is given to 赛特
        npc.add(
                ConversationStates.QUEST_ITEM_BROUGHT,
                ConversationPhrases.YES_MESSAGES,
                new AndCondition(
                        new QuestInStateCondition(QUEST_SLOT,"start"),
                        playerHasFullWeightSheep),
                ConversationStates.IDLE,
                "我知道它是 尼世亚 的，对吧? 我就等它了. " +
                "这是给朋友的朋友的礼物，如果我没有送生日礼物，我会不好意思. " +
                "向 尼世亚 说声谢谢.",
                new MultipleActions(removeSheepAction));

        // Player answers no - Sheep stays at player
        npc.add(
                ConversationStates.QUEST_ITEM_BROUGHT,
                ConversationPhrases.NO_MESSAGES,
                new AndCondition(
                        new QuestInStateCondition(QUEST_SLOT,"start"),
                        playerHasFullWeightSheep),
                ConversationStates.IDLE,
                "刚才他想送给我一个...",
                null);


		npc.add(
				ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
						new QuestInStateCondition(QUEST_SLOT, "handed_over"),
				ConversationStates.ATTENDING,
				"谢谢你给我带来了 Nishiyas 的羊! 我的朋友真的很喜欢它.", null);
    }

    /**
     * The step where the player returns to 尼世亚 to get his reward.
     */
    private void preparePlayerReturnsStep() {
        final List<ChatAction> reward = new LinkedList<ChatAction>();
        reward.add(new ChatAction() {
            @Override
			public void fire(Player player, Sentence sentence, EventRaiser npc) {
                // give XP to level 2
                int reward = Level.getXP( 2 ) - player.getXP();
                if(reward > MIN_XP_GAIN) {
                    player.addXP(reward);
                } else {
                    player.addXP(MIN_XP_GAIN);
                }
                player.notifyWorldAboutChanges();
            }
        });
        reward.add(new SetQuestAction(QUEST_SLOT, "done"));
        reward.add(new IncreaseKarmaAction( 10 ));

        final SpeakerNPC npc = npcs.get("尼世亚");
        // Asks player if he handed over the sheep
        npc.add(
                ConversationStates.IDLE,
                ConversationPhrases.GREETING_MESSAGES,
                new QuestInStateCondition(QUEST_SLOT, "handed_over"),
                ConversationStates.QUEST_ITEM_QUESTION,
                "你已经把羊给 赛特 了吗?",
                null);
        // Player answers yes - give reward
        npc.add(
                ConversationStates.QUEST_ITEM_QUESTION,
                ConversationPhrases.YES_MESSAGES,
                new QuestInStateCondition(QUEST_SLOT, "handed_over"),
                ConversationStates.IDLE,
                "谢谢你！你不知道这几天我有多少事要做. " +
                "你真的帮了我的大忙.",
                new MultipleActions(reward));
        // Player answers no -
        npc.add(
                ConversationStates.QUEST_ITEM_QUESTION,
                ConversationPhrases.NO_MESSAGES,
                new QuestInStateCondition(QUEST_SLOT, "handed_over"),
                ConversationStates.IDLE,
                "好吧...ok，但不要忘了。. 赛特 需要马上弄到羊.",
                null);

        // Player asks for quest after solving the quest
        npc.add(ConversationStates.ATTENDING,
                ConversationPhrases.QUEST_MESSAGES,
                new QuestCompletedCondition(QUEST_SLOT),
                ConversationStates.ATTENDING,
                "抱歉，现在我什么都为你做不了，不过还是谢谢你的帮助。",
                null);
    }

    @Override
    public String getRegion() {
        return Region.SEMOS_CITY;
    }

	@Override
	public String getNPCName() {
		return "尼世亚";
	}
}
