/***************************************************************************
 *                     (C) Copyright 2015 - Stendhal                       *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.ados.market;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Builds a npc in Ados (name: Aerianna) who is a half elf
 *
 * @author storyteller
 *
 */
public class HalfElfNPC implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Aerianna") {

			@Override
			protected void createPath() {
                final List<Node> nodes = new LinkedList<Node>();
                nodes.add(new Node(55, 47));
                nodes.add(new Node(55, 35));
                nodes.add(new Node(73, 35));
                nodes.add(new Node(73, 23));
                nodes.add(new Node(76, 23));
                nodes.add(new Node(76, 11));
                nodes.add(new Node(71, 11));
                nodes.add(new Node(71, 7));
                nodes.add(new Node(57, 7));
                nodes.add(new Node(57, 23));
                nodes.add(new Node(28, 23));
                nodes.add(new Node(28, 28));
                nodes.add(new Node(21, 28));
                nodes.add(new Node(21, 44));
                nodes.add(new Node(31, 44));
                nodes.add(new Node(31, 47));
                setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("欢迎, #朋友. 很高兴见到你.");
				addHelp("需要我帮忙吗？我可以给你一些真正有用的建议, 让你增加些 #经验 和 #知识.");
				addJob("这是一个半精灵人, 我 #父亲 是一个 #人类 冒险家, 我 #母亲 是一个来自 #Nalwor 的 #精灵. 因为我想弄清楚这两种文化, 我走遍了 #Faiumoni 的各处, 尽可能多的学会了当地的 #文化 和习惯");
				addOffer("我不能给你什么, 除了花 #时间 告诉你的我的经历. ");
				addGoodbye("感谢你能与我谈话, 朋友!");

		        addReply("quest", "不, #朋友, 我没有任务交给你, 现在我只是看看 Ados 城内的市场. ");
		        addReply("朋友", "我喜欢和来自不同 #文化 的人交朋友, 我们可以相互学到很多东西！");
		        addReply("father", "我的父亲是很常勇敢的 #人类 冒险家, 一天他勇敢地闯进了 Nalwor 森林, 他很聪明并设法到达了 #Nalwor 城, 在那里他见到我 #母亲 ,随之立即坠入了爱河...");
		        addReply("母亲", "我妈妈是一个 #精灵 , 住在 #Nalwor 城. #多年前, 她在我父亲进入了 #Nalwor 后相遇并相爱, 所以我生来就是个半精灵. ");
		        addReply("时间", "时间很珍贵, 尤其对于我来说. 作为一个半精灵, 我能比 #人类 活的更长, 但长不过一个真正的 #精灵.");
		        addReply("Faiumoni", "Faiumoni 是一块很大的陆地! 我曾进入南方的森林, 爬上北方的山, 还花了一些时间去了东北的沙漠. 那里真的非常值得去旅行!");
		        addReply("experience", "当做某件事的时候, 你就增加一些经验, 要留意周围的每件人和事, 并从中学习. ");
		        addReply("知识", "知识对于弄清事物的原理很重要, 我喜欢去学习这个世界的每样东西. 尽管我知道我不可能弄清这里的每一处. ");
		        addReply("Nalwor", "你没听说过 Nalwor 城? 好吧, 可能没有, 它在森林中藏的很深, 但离我不太远... #多年前 , 我出生在那...");
		        addReply("多年前", "你应该知道我比看上云老...精灵能活得很长, 因为我 #母亲 是一个 #精灵 我也长得不那么快. 但我对我的年龄保密. 嘻嘻 *hihi*");
		        addReply("文化", "在 #Faiumoni 存在着各种文明, 不同的地区和种族有着 #不同 的文化, 但也有很多 #相似 的地方.");
		        addReply("不同", "精灵族文化与人族存在着些许不同, 但完全不同与矮人族文明, #矮人 #dwarf 从不会住在森林里, 而会选择住进山间或地下深处. ");
		        addReply("相似", "你知道一些 #兽族 部落也会举行类似人类的结婚的庆祝活动吗？但兽人的婚姻不会持续很长, 通常情况下不会超过后代的成长期, 并且会互相猎杀. 另一个与之相似的例子是人类中游牧民族四海为家, 有点像白化精灵. 有时文明如此相近, 只需靠近他们一些!");
		        addReply("人类", "好吧, 因为我在 #Nalwor 出生并成长, 所以你了解的比我多, 尽管我有一个人类的父亲. 但我听到些人类的大致情况, 人类非常善于发明创造和解决问题. 尽管他们时常争斗, 一部分人对于另一些人类和生物真的产生同情.");
		        addReply("精灵", "精灵们对于自身的漂亮, 智慧和魔力感到自豪, 我也不说因为我是半精灵！嘻嘻 *hihi* 他们住在远离其他种族的森林中或一些有强大魔力的地方. 精灵们都活得很久, 你甚至不能从外观看出他们的年龄. 如果你可以和一些精灵做朋友, 那就可以从他们身上学到很多东西!");
		        addReply("矮人", "矮人都很小, 但很通勇敢！他们住在地下或山上, 因为他们喜欢挖一些珍贵的原料. 对于矮人的传统有些旧传闻, 比如氏族化. 之前我拜过一个矮人氏族, 一周的时间就从不信任变成非常铁的朋友!");
		        addReply("兽族", "兽族住的房子都很简陋, 每天的大多的事情都是狩猎和争斗. 我很幸运求了一个兽人首长, 他想独自杀了绿龙, 但他快成功时太倒霉没能成功. 所以他邀请我拜访他的部落.  在那段时间里我学到很多兽人的习性. 他们并不坏, 虽然很多人这样认为. ");
			}
		};

		npc.setDescription("你遇见了 Aerianna, 一个长着精灵耳朵的漂亮的年轻少女");
		npc.setEntityClass("halfelfnpc");
		npc.setPosition(55, 47);
		npc.setDirection(Direction.DOWN);
		npc.initHP(100);
		zone.add(npc);
	}

}
