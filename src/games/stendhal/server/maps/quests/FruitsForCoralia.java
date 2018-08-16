package games.stendhal.server.maps.quests;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.CollectRequestedItemsAction;
import games.stendhal.server.entity.npc.action.EquipRandomAmountOfItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayRequiredItemsFromCollectionAction;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.util.ItemCollection;

/**
 * QUEST: Fruits for Coralia
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Coralia (Bar-maid of Ado tavern)</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>Coralia introduces herself and asks for a variety of fresh fruits for her hat.</li>
 * <li>You collect the items.</li>
 * <li>Coralia sees your items, asks for them then thanks you.</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>XP: 300</li>
 * <li><1-5> Crepes Suzettes</li>
 * <li><2-8> Minor Potions</li>
 * <li>Karma: 5</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>After 1 week, fit with the withering of the fruits</li>
 * </ul>
 *
 * @author pinchanzee
 */
public class FruitsForCoralia extends AbstractQuest {



	/**
	 * NOTE: Reward has not been set, nor has the XP.
	 * left them default here, but in the JUnit test
	 * called reward item "REWARD" temporarily
	 */

    public static final String QUEST_SLOT = "fruits_coralia";

    /**
     * The delay between repeating quests.
     * 1 week
     */
	private static final int REQUIRED_MINUTES = 1440;

    /**
	 * Required items for the quest.
	 */
	protected static final String NEEDED_ITEMS = "apple=4;banana=5;cherry=9;grapes=2;pear=4;watermelon=1;pomegranate=2";

    @Override
    public void addToWorld() {
        fillQuestInfo("Fruits for Coralia",
				"Ados 的旅店女待 Coralia, 要找一些新鲜的水果给她收养的老鼠",
				true);
        prepareQuestStep();
        prepareBringingStep();
    }

    @Override
    public String getSlotName() {
        return QUEST_SLOT;
    }

    @Override
    public String getName() {
        return "FruitsForCoralia";
    }

 	@Override
 	public int getMinLevel() {
 		return 0;
 	}

 	@Override
 	public boolean isRepeatable(final Player player) {
 		return new AndCondition(
 					new QuestStateStartsWithCondition(QUEST_SLOT, "done;"),
 					new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)).fire(player, null, null);
 	}

 	@Override
 	public String getRegion() {
 		return Region.ADOS_CITY;
 	}

 	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Coralia 向我要了一些新鲜水果给她的老鼠吃");
		final String questState = player.getQuest(QUEST_SLOT);

		if ("rejected".equals(questState)) {
			// quest rejected
			res.add("我决定不帮她找，我还有别的事要做.");
		} else if (!player.isQuestCompleted(QUEST_SLOT)) {
			// not yet finished
			final ItemCollection missingItems = new ItemCollection();
			missingItems.addFromQuestStateString(questState);
			res.add("我仍需要带给 Coralia " + missingItems.toStringList() + ".");
		} else if (isRepeatable(player)) {
			// may be repeated now
			res.add("为了Coralia 的老鼠，我找到的水果已经有一段时间，我想知道水果是不是已经干了。");
        } else {
        	// not (currently) repeatable
        	res.add("我把水果带给了 Coralia ，她把水果恢复到刚长成的样子。");
		}
		return res;
	}

    public void prepareQuestStep() {
    	SpeakerNPC npc = npcs.get("Coralia");

    	// various quest introductions

    	// offer quest first time
    	npc.add(ConversationStates.ATTENDING,
    		ConversationPhrases.combine(ConversationPhrases.QUEST_MESSAGES, "fruit"),
    		new AndCondition(
    			new QuestNotStartedCondition(QUEST_SLOT),
    			new QuestNotInStateCondition(QUEST_SLOT, "rejected")),
    		ConversationStates.QUEST_OFFERED,
    		"你这么好心为了我的老鼠寻找水果？我很感谢!",
    		null);

    	// ask for quest again after rejected
    	npc.add(ConversationStates.ATTENDING,
    		ConversationPhrases.combine(ConversationPhrases.QUEST_MESSAGES, "hat"),
    		new QuestInStateCondition(QUEST_SLOT, "rejected"),
    		ConversationStates.QUEST_OFFERED,
    		"你仍在为我的老鼠找新鲜的水果吗？",
    		null);

    	// repeat quest
    	npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.combine(ConversationPhrases.QUEST_MESSAGES, "hat"),
            new AndCondition(
            	new QuestCompletedCondition(QUEST_SLOT),
            	new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)),
            ConversationStates.QUEST_OFFERED,
            "我对此歉意，你给我的老鼠找的水果不新鲜了, " +
            "你能再次为我找一些吗?",
            null);

    	// quest inactive
    	npc.add(ConversationStates.ATTENDING,
        	ConversationPhrases.combine(ConversationPhrases.QUEST_MESSAGES, "hat"),
        	new AndCondition(
        		new QuestCompletedCondition(QUEST_SLOT),
        		new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES))),
        	ConversationStates.ATTENDING,
        	"我的老鼠看起来不新鲜？我不再需要新鲜水果了。但还是谢谢你的关心!",
        	null);

    	// end of quest introductions


    	// introduction chat
    	npc.add(ConversationStates.ATTENDING,
        	"hat",
        	new AndCondition(
        		new QuestNotStartedCondition(QUEST_SLOT),
        		new QuestNotInStateCondition(QUEST_SLOT, "rejected")),
        	ConversationStates.ATTENDING,
        	"有点歉意，你看这些水果都像这个一样干了，最好还是新鲜的 #fruits...",
        	null);

    	// accept quest response
    	npc.add(ConversationStates.QUEST_OFFERED,
    		ConversationPhrases.YES_MESSAGES,
    		null,
    		ConversationStates.QUESTION_1,
    		null,
			new MultipleActions(
				new SetQuestAction(QUEST_SLOT, NEEDED_ITEMS),
				new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "太好了，我喜欢这些新鲜水果: [items].")));

    	// reject quest response
    	npc.add(ConversationStates.QUEST_OFFERED,
        	ConversationPhrases.NO_MESSAGES,
        	null,
        	ConversationStates.ATTENDING,
        	"这些外来的老鼠不给管好自已，你明白的...",
        	new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

    	// meet again during quest
    	npc.add(ConversationStates.IDLE,
    		ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(
				new QuestActiveCondition(QUEST_SLOT),
				new GreetingMatchesNameCondition(npc.getName())),
			ConversationStates.ATTENDING,
			"欢迎回来，如果你给我的老鼠 #hat 带来了新鲜水果，我会乐意接受。!",
			null);



    	// specific fruit info
    	npc.add(ConversationStates.QUESTION_1,
        	"apple",
        	new QuestActiveCondition(QUEST_SLOT),
        	ConversationStates.QUESTION_1,
        	"好红, 好亮的苹果! 这是我刚从Semos东边拿到的.",
        	null);

    	npc.add(ConversationStates.QUESTION_1,
            "banana",
            new QuestActiveCondition(QUEST_SLOT),
            ConversationStates.QUESTION_1,
            "来自小岛的格外异域风情香蕉。一直往西走，穿过。。还是说香蕉的事吧，它肉多且够新鲜.",
            null);

    	npc.add(ConversationStates.QUESTION_1,
        	"cherry",
        	new QuestActiveCondition(QUEST_SLOT),
        	ConversationStates.QUESTION_1,
        	"Fado 的一位老妇销售一些漂亮的樱桃 cherries.",
        	null);

    	npc.add(ConversationStates.QUESTION_1,
            "grapes",
            new QuestActiveCondition(QUEST_SLOT),
            ConversationStates.QUESTION_1,
            "There's a beautiful little temple in the mountains north of Semos北面的山上有一个漂亮的神庙，上面爬满了葡萄藤 grapes!  还听说 Or'ril山上有些旧房子.",
            null);

    	npc.add(ConversationStates.QUESTION_1,
        	"pear",
        	new QuestActiveCondition(QUEST_SLOT),
        	ConversationStates.QUESTION_1,
        	"在北方山上有个漂亮的瀑布，我看到过那里有梨树 pear .",
        	null);

    	npc.add(ConversationStates.QUESTION_1,
            "watermelon",
            new QuestActiveCondition(QUEST_SLOT),
            ConversationStates.QUESTION_1,
            "Kalavan 花园里有一个大西瓜 watermelons ，可以给我的老鼠做成漂亮的装饰",
            null);

    	npc.add(ConversationStates.QUESTION_1,
            "pomegranate",
            new QuestActiveCondition(QUEST_SLOT),
            ConversationStates.QUESTION_1,
            "我没见过长在野外的石榴树pomegranate ，但我听说一个住在南方大河的人在自家花园里种它们.",
            null);
    }


    private void prepareBringingStep() {
		final SpeakerNPC npc = npcs.get("Coralia");

		// ask for required items
    	npc.add(ConversationStates.ATTENDING,
    		ConversationPhrases.combine(ConversationPhrases.QUEST_MESSAGES, "hat"),
    		new QuestActiveCondition(QUEST_SLOT),
    		ConversationStates.QUESTION_2,
    		null,
    		new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "我还喜欢 [items]. 你带的有吗?"));

    	// player says he didn't bring any items
		npc.add(ConversationStates.QUESTION_2,
			ConversationPhrases.NO_MESSAGES,
			new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.QUESTION_1,
			null,
			new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Oh, that's a shame, do tell me when you find some. I'd still like [items]."));

    	// player says he has a required item with him
		npc.add(ConversationStates.QUESTION_2,
			ConversationPhrases.YES_MESSAGES,
			new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.QUESTION_2,
			"Wonderful, what fresh delights have you brought?",
			null);

		// set up next step
    	ChatAction completeAction = new  MultipleActions(
			new SetQuestAction(QUEST_SLOT, "done"),
			new SayTextAction("我的老鼠从未如此可爱! 非常感谢! 请收下这些回报"),
			new IncreaseXPAction(300),
			new IncreaseKarmaAction(5),
			new EquipRandomAmountOfItemAction("crepes suzette", 1, 5),
			new EquipRandomAmountOfItemAction("minor potion", 2, 8),
			new SetQuestToTimeStampAction(QUEST_SLOT, 1)
		);

    	// add triggers for the item names
    	final ItemCollection items = new ItemCollection();
    	items.addFromQuestStateString(NEEDED_ITEMS);
    	for (final Map.Entry<String, Integer> item : items.entrySet()) {
    		npc.add(ConversationStates.QUESTION_2,
    			item.getKey(),
    			new QuestActiveCondition(QUEST_SLOT),
    			ConversationStates.QUESTION_2,
    			null,
    			new CollectRequestedItemsAction(item.getKey(),
    				QUEST_SLOT,
    				"太棒了! 你又带了什么好东西吗?", "这些我已经够多了.",
    				completeAction,
    				ConversationStates.ATTENDING));
    	}
    }

	@Override
	public String getNPCName() {
		return "Coralia";
	}
}
