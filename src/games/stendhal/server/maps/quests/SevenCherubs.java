package games.stendhal.server.maps.quests;

import games.stendhal.common.Rand;
import games.stendhal.server.core.engine.StendhalRPWorld;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.parser.Sentence;
import games.stendhal.server.entity.player.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * QUEST: Find the seven cherubs that are all around the world. PARTICIPANTS: -
 * Cherubiel - Gabriel - Ophaniel - Raphael - Uriel - Zophiel - Azazel STEPS: -
 * Find them and they will reward you.
 *
 * REWARD: -
 *
 * REPETITIONS: - Just once.
 */
public class SevenCherubs extends AbstractQuest {
	private static final String QUEST_SLOT = "seven_cherubs";

	@Override
	public void init(String name) {
		super.init(name, QUEST_SLOT);
	}

	@Override
	public boolean isCompleted(Player player) {
		if (!player.hasQuest(QUEST_SLOT)) {
			return false;
		}
		String npcDoneText = player.getQuest(QUEST_SLOT);
		String[] done = npcDoneText.split(";");
		int left = 7 - done.length;
		return left < 0;
	}

	@Override
	public List<String> getHistory(Player player) {
		List<String> res = new ArrayList<String>();
		if (player.hasQuest(QUEST_SLOT)) {
			String npcDoneText = player.getQuest(QUEST_SLOT);
			String[] done = npcDoneText.split(";");
			boolean first = true;
			for (String cherub : done) {
				if (!cherub.trim().equals("")) {
					res.add(cherub.toUpperCase());
					if (first) {
						first = false;
						res.add("QUEST_ACCEPTED");
					}
				}
			}
			if (isCompleted(player)) {
				res.add("DONE");
			}
		}
		return res;
	}

	static class CherubNPC extends SpeakerNPC {
		public CherubNPC(String name, int x, int y) {
			super(name);

			setEntityClass("angelnpc");
			setPosition(x, y);
			initHP(100);

			List<Node> nodes = new LinkedList<Node>();
			nodes.add(new Node(x, y));
			nodes.add(new Node(x - 2, y));
			nodes.add(new Node(x - 2, y - 2));
			nodes.add(new Node(x, y - 2));
			setPath(new FixedPath(nodes, true));
		}

		@Override
		protected void createPath() {
			// do nothing
		}

		@Override
		protected void createDialog() {
			add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				null, ConversationStates.IDLE, null,
				new SpeakerNPC.ChatAction() {
					@Override
					public void fire(Player player, Sentence sentence, SpeakerNPC engine) {
						if (!player.hasQuest(QUEST_SLOT)) {
							player.setQuest(QUEST_SLOT, "");
						}

						// Visited cherubs are store in the quest-name
						// QUEST_SLOT.
						// Please note that there is an additional empty
						// entry in the beginning.
						String npcDoneText = player.getQuest(QUEST_SLOT);
						String[] done = npcDoneText.split(";");
						List<String> list = Arrays.asList(done);
						int left = 7 - list.size();

						if (!list.contains(engine.getName())) {
							player.setQuest(QUEST_SLOT, npcDoneText + ";"
									+ engine.getName());

							player.heal();
							player.healPoison();

							if (left > 0) {
								engine.say("Well done! You only need to find "
												+ (7 - list.size())
												+ " more. Fare thee well!");
								player.addXP((7 - left + 1) * 200);
							} else {
								engine.say("Thou hast proven thyself brave enough to bear this mighty relic!");

								/*
								 * Proposal by Daniel Herding (mort): once
								 * we have enough quests, we shouldn't have
								 * this randomization anymore. There should
								 * be one hard quest for each of the golden
								 * items.
								 *
								 * I commented out the golden shield here
								 * because you already get that from the
								 * CloaksForBario quest.
								 *
								 * Golden legs was disabled because it can
								 * be won in DiceGambling.
								 *
								 * Fire sword was disabled because it can be
								 * earned by fighting, and because the
								 * stronger ice sword is available through
								 * other quest and through fighting.
								 *
								 * Once enough quests exist, this quest
								 * should always give you golden boots
								 * (because you have to walk much to fulfil
								 * it).
								 *
								 */
								String[] items = { "golden boots", "golden armor", "golden helmet" };
								Item item = StendhalRPWorld
									.get().getRuleManager()
									.getEntityManager()
									.getItem(items[Rand.rand(items.length)]);
								item.setBoundTo(player.getName());
								player.equip(item, true);
								player.addXP(2000);
							}
						} else {
							if (left > -1) {
								engine.say("Seek out the other cherubim to get thy reward!");
							} else {
								engine.say("Thou hast sought and found each of the seven cherubim! Now, mighty art thou with the rewards so earn'd.");
							}
						}
						player.notifyWorldAboutChanges();
					}
				});
			addGoodbye();
		}
	}

	@Override
	public void addToWorld() {
		StendhalRPWorld world = StendhalRPWorld.get();
		super.addToWorld();

		StendhalRPZone zone;
		SpeakerNPC npc;

		zone = world.getZone("0_semos_village_w");
		npc = new CherubNPC("Cherubiel", 48, 60);
		zone.add(npc);

		zone = world.getZone("0_nalwor_city");
		npc = new CherubNPC("Gabriel", 105, 17);
		zone.add(npc);

		zone = world.getZone("0_orril_river_s");
		npc = new CherubNPC("Ophaniel", 105, 79);
		zone.add(npc);

		zone = world.getZone("0_orril_river_s_w2");
		npc = new CherubNPC("Raphael", 95, 30);
		zone.add(npc);

		zone = world.getZone("0_orril_mountain_w2");
		npc = new CherubNPC("Uriel", 47, 27);
		zone.add(npc);

		zone = world.getZone("0_semos_mountain_n2_w2");
		npc = new CherubNPC("Zophiel", 16, 3);
		zone.add(npc);

		zone = world.getZone("0_ados_rock");
		npc = new CherubNPC("Azazel", 67, 24);
		zone.add(npc);
	}
}
