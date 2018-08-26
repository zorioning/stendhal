[![Stendhal Tower](https://arianne-project.org/screens/stendhal/THM_MagicTower98small.jpeg)](https://arianne-project.org/screens/stendhal/MagicTower98.jpg)
[![Achievements](https://arianne-project.org/screens/stendhal/THM_Achievement_Stendhal98.jpg)](https://arianne-project.org/screens/stendhal/Achievement_Stendhal98.jpg)
[![Party](https://arianne-project.org/screens/stendhal/THM_raid20110105.jpg)](https://arianne-project.org/screens/stendhal/raid20110105.jpg)&nbsp;&nbsp;&nbsp;[更多图片 >](https://stendhalgame.org/media/screenshots.html)

大兄弟，你是在寻求刺激不？ 是想一夜暴富不？<br>
想不想狂砍十条街，当大哥使唤小弟？<br>
有没有兴趣来这里一起建设新大陆？

Stendhal是一款开发多年的RPG网游。这款游戏完全开源，客户端和服务端都开源。
这款游戏的世界非常庞大，你可以探索城镇、建筑、平原、洞穴和地牢，并且会遇到NPC并获得任务。

你的角色慢慢成长，游戏也会越来越刺激。用你赚到的钱，去购买物品、改进武器和装备。我知道你的欲望已经饥渴难耐了，赶紧通过周游世界来实现你的血腥杀戮！

赶紧...来啊，快活啊~

## 如何参与汉化

1、直接Pull request<br/>
2、加入到qq群603830308或tg群linuxgamedotcn，参与汉化<br/>
汉化内容：<br/>
1、data/languages/zh_Cn.txt<br/>
2、src/games/stendhal/server/maps/ 文件夹下，里面文件夹是每个城镇名字，再里面是每个npc的对话<br/>
查看汉化进度：https://github.com/Gamuxorg/stendhal-cn/projects/1

## 如何做汉化
一、需要汉化的地方主要有以下几处： 客户端，服务端。
    （一）客户端主要是UI界面的汉化，需汉化的词句都分散在代码中，这些工作基本完工。
    （二）服务端需要汉化的地方：
        1、data/config 目录，里面是xml文件，都是些精灵、物品和地图的属性描述，主要修改两个地方，<name>标签、 <creature name>标签，也就是名称和描述。 另外还有个需要修改的地方是怪物掉落物品的名称，需要配合date/langulge/zh_CN.txt 做全局替换
        2、src/games/stendhal/server/下的 maps、entity、action目录，里面是NPC的对话、任务记录和菜单命令，汉化这些需要一个一个手工打开查找，一些对话是字符串的拼接，所以汉化这些内容需要有一定编程基础。
        
二、注意事项

    （一）关键字替换。我用vscode做全局替换，搜索关键字时，要注意被替换的内容，别把不该替换的替换掉了。经验总结有以下几点：
    
    一般物品名称需要替换的地方有三处：xml中的物品name和打怪掉落物品item的name，npc对话和任务的关键物品，物品买卖的关键字。出现其他需要替换的地方就要小心，vscode可以去不需要替换的地方。核对清楚后再替换
    
    怪物名称需要替换的地方有下面几处：xml中的怪物名称，npc对话中的一些相关名词，一些任务中的打怪需求，
    
        1，查找替换时加 "" （英文半角双引号），比如 "wolf"替换为"狼"， 因为这样可以最大限度的少替换不该被替换的内容。虽会漏掉一些，但程序不会出错，如果不小心替换了程序中的变量什么的，造成程序无法正常运行就惨了。 搜完加引号的，然后再搜一遍不带引号的，检查一下有没有漏掉的地方。
        
        2、就算加了双引号，还是有些不该替换的被替换，要格外小心。比如一些 类型属性名称与主名称相同，比如axe主名称是斧子，但类型class名称也是axe, 要注意替换时只替换主名稳，物品类型等其它属性名称不替换
        
        3、先替换长字符串，再替换短的。比如 "sword"、"long sword"、"gold sword" 这些名称，如果先找sword, 肯定会找到 long sword 和 gole sword, 如果不小心替换了，后面再汉化long sword查找时会出现很多问题。 
        

## How to play

[![Play now](https://arianne-project.org/images/playbutton.png)](https://stendhalgame.org/account/mycharacters.html) &nbsp; &nbsp; &nbsp; &nbsp;
[![Download](https://stendhalgame.org/images/downloadbutton.png)](https://arianne-project.org/download/stendhal.zip)

You need Java which you can download from [https://www.java.com](https://www.java.com/en/download) on Microsoft Windows. Java is included in most Linux distributions. Both Oracle Java and OpenJDK are supported.

Please download Stendhal from [https://stendhalgame.org](https://stendhalgame.org)<br>
You can double click on stendhal-starter.jar and the client will run.<br>
If you prefer to run from command line, then just execute this command
in the stendhal folder:

`java -jar stendhal-starter.jar`

There is a  [manual](https://stendhalgame.org/wiki/Stendhal_Manual), and there is Help from the client menu icons in the upper right hand side of the game screen.


## Development environment

* [Hosting a Stendhal Server](https://stendhalgame.org/wiki/Host_a_Stendhal_Server)
* [Stendhal in Eclipse](https://stendhalgame.org/wiki/Stendhal_on_Eclipse)
* [Building Stendhal](https://stendhalgame.org/wiki/HowToBuildStendhal)


## Legal
The server and java client is free software; you can redistribute it and/or modify it under the terms of the **GNU General Public License** 2 or later as published by the Free Software Foundation.

The web client is free software; you can redistribute it and/or modify it under the terms of the **GNU Affero General Public License** 3 or later as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the [LICENSE.txt](LICENSE.txt) file for more details.

Stendhal(c) is copyright of Miguel Angel Blanch Lardin, 2005-2008, arianne_rpg at users dot sourceforge dot net and others<br>
Stendhal(c) is copyright of the Arianne Project, 2006-2017, arianne-general at lists dot sourceforge dot net
