/* $Id$ */
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
package games.stendhal.server.maps.semos.city;

import java.awt.Rectangle;
import java.awt.Shape;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.GameEvent;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.CollisionAction;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.mapstuff.office.RentedSign;
import games.stendhal.server.entity.mapstuff.office.RentedSignList;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.RemoveStorableEntityAction;
import games.stendhal.server.entity.npc.condition.AdminCondition;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.LevelLessThanCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasStorableEntityCondition;
import games.stendhal.server.entity.npc.condition.TextHasParameterCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.StringUtils;

/**
 * A merchant (original name: Gordon) who rents signs to players.
 *
 * The player has to have at least level 5 to prevent abuse by newly created characters.
 */
public class SignLessorNPC implements ZoneConfigurator {
	protected String text;

	// 1.5 minutes
	private static final int CHAT_TIMEOUT = 90;
	private static final int MONEY = 100;
	protected RentedSignList rentedSignList;

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		final Shape shape = new Rectangle(21, 48, 17, 1);
		rentedSignList = new RentedSignList(zone, shape);
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Gordon") {

			@Override
			public void createDialog() {
				addGreeting("Hi, 我出租 #rent 标识，同时也移除 #remove 旧标识.");
				addJob("一次出租 #rent 标识一天.");
				addHelp("如果你想阻用 #rent 一个标识, 只用告诉我需要写的标识名字.");
				setPlayerChatTimeout(CHAT_TIMEOUT);

				add(ConversationStates.ATTENDING, "",
					new AndCondition(getRentMatchCond(), new LevelLessThanCondition(6)),
					ConversationStates.ATTENDING,
					"Oh 抱歉，我不给经验像你一样少的人提供标识出租服务。",
					null);

				add(ConversationStates.ATTENDING, "",
					new AndCondition(getRentMatchCond(), new LevelGreaterThanCondition(5), new NotCondition(new TextHasParameterCondition())),
					ConversationStates.ATTENDING,
					"只要告诉我你想租用 #rent ，接下来再告诉我需要写的标识名字。",
					null);

				add(ConversationStates.ATTENDING, "",
					new AndCondition(getRentMatchCond(), new LevelGreaterThanCondition(5), new TextHasParameterCondition()),
					ConversationStates.BUY_PRICE_OFFERED,
					null,
					new ChatAction() {
						@Override
						public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
							text = sentence.getOriginalText().substring(5).trim();

							String reply = "标上标识24小时要花费 " + MONEY + " 钱，你想租吗？";

							if (rentedSignList.getByName(player.getName()) != null) {
								reply = reply + " 请写下标识名称，我会替换掉你的旧标识.";
							}

							npc.say(reply);
						}

						@Override
						public String toString() {
							return "remember text";
						}
				});

				add(ConversationStates.BUY_PRICE_OFFERED,
					ConversationPhrases.YES_MESSAGES,
					new NotCondition(new PlayerHasItemWithHimCondition("money", MONEY)),
					ConversationStates.ATTENDING,
					"抱歉，你没有足够的钱", null);

				add(ConversationStates.BUY_PRICE_OFFERED,
					ConversationPhrases.YES_MESSAGES,
					new PlayerHasItemWithHimCondition("money", MONEY),
					ConversationStates.IDLE, null,
					new RentSignChatAction());

				add(ConversationStates.BUY_PRICE_OFFERED,
					ConversationPhrases.NO_MESSAGES, null,
					ConversationStates.ATTENDING,
					"如果你改变主意，再告诉我就行。", null);

				add(ConversationStates.ATTENDING, "remove",
					new PlayerHasStorableEntityCondition(rentedSignList),
					ConversationStates.ATTENDING,
					"Ok, 我马上移除你的标识.",
					new RemoveStorableEntityAction(rentedSignList));

				add(ConversationStates.ATTENDING, "remove",
					new NotCondition(new PlayerHasStorableEntityCondition(rentedSignList)),
					ConversationStates.ATTENDING,
					"你没有任何的标识，所以我不能移除.", null);

				// admins may remove signs (even low level admins)
				add(ConversationStates.ATTENDING, "delete",
					new AdminCondition(100),
					ConversationStates.ATTENDING, null,
					new ChatAction() {
						@Override
						public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
							if (sentence.getExpressions().size() < 2) {
								npc.say("Syntax: delete <nameofplayer>");
								return;
							}
							final String playerName = sentence.getOriginalText().substring("delete ".length()).trim();
							if (rentedSignList.removeByName(playerName)) {
								final String message = player.getName() + " 从 " + playerName + " 删除标识";
								SingletonRepository.getRuleProcessor().sendMessageToSupporters("SignLessorNPC", message);
								new GameEvent(player.getName(), "sign", "deleted", playerName).raise();
							} else {
								player.sendPrivateText("找不到 " + playerName +" 的标识");
							}
						}

						@Override
						public String toString() {
							return "admin delete sign";
						}
				});

				addGoodbye();
			}

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(20,50));
				nodes.add(new Node(38, 50));
				nodes.add(new Node(38, 51));
				nodes.add(new Node(20, 51));
				setPath(new FixedPath(nodes, true));
			}

		};
		npc.setPosition(20, 50);
		npc.setCollisionAction(CollisionAction.STOP);
		npc.setEntityClass("signguynpc");
		zone.add(npc);
		npc.setDescription("你见到了 Gordon. 他一直盯着经过他身边人们的标识看");
	}

	private static ChatCondition getRentMatchCond() {
		return new ChatCondition() {
			@Override
			public boolean fire(Player player, Sentence sentence, Entity npc) {
				String txt = sentence.getOriginalText();

            	//TODO replaced by using sentence matching "[you] rent"
				if (txt.startsWith("rent") || txt.startsWith("you rent")) {
					return true;
				} else {
					return false;
				}
			}
		};
	}

	class RentSignChatAction implements ChatAction {

		private final Logger logger = Logger.getLogger(RentSignChatAction.class);

		@Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
			if (text.length() > 1000) {
				text = text.substring(0, 1000) + "...";
			}

			// do not accept all upper case
			if (StringUtils.countLowerCase(text) < StringUtils.countUpperCase(text) * 2) {
				text = text.toLowerCase();
			}

			// put the sign up
			rentedSignList.removeByName(player.getName());
			final RentedSign sign = new RentedSign(player, text);
			final boolean success = rentedSignList.add(sign);

			// confirm, log, tell postman
			if (success) {
				player.drop("money", MONEY);
				npc.say("OK, 让我把你的标识放上.");

				// inform IRC using postman
				final Player postman = SingletonRepository.getRuleProcessor().getPlayer("postman");
				String message = player.getName() + " 租用一个标识说 \"" + text + "\"";
				if (postman != null) {
					postman.sendPrivateText(message);
				}
				logger.log(Level.toLevel(System.getProperty("stendhal.support.loglevel"), Level.DEBUG), message);
				new GameEvent(player.getName(), "sign", "rent", text).raise();
			} else {
				npc.say("抱歉，现在没有更多的标识位了，我没有空间能写下了.");
			}
		}

		@Override
		public String toString() {
			return "put up sign";
		}
	}
}
