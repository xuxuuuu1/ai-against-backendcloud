package com.kob.botrunningsystem.service.impl.utils;

import com.kob.botrunningsystem.utils.BotInterface;
import org.joor.Reflect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class Consumer extends Thread {
    private Bot bot;

    private static RestTemplate restTemplate;

    private final static String receiveBotMoveUrl = "http://43.143.193.15:3000/pk/receive/bot/move/";
    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        Consumer.restTemplate = restTemplate;
    }

    public void startTimeout(long timeout, Bot bot) {
        this.bot = bot;
        this.start();

        try {
            // 最多等待多少秒,且join可以提前结束，而sleep必须等待指定时间
            this.join(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 如果超出等待时间则中断当前线程
            this.interrupt();
        }
    }

    private String addUid(String code, String uid) {
        int k = code.indexOf(" implements com.kob.botrunningsystem.utils.BotInterface");
        return code.substring(0, k) + uid + code.substring(k);
    }

    // create()方法等同于调用该Class对象的构造方法，即实例化一个对象，有有参和无参两种形式
    // get()用于获取最终的对象
    @Override
    public void run() {
        // 相同类名只会编译一次,加uuid是为了使类名不一样
        UUID uuid = UUID.randomUUID();
        String uid = uuid.toString().substring(0, 8);
        BotInterface botInterface = Reflect.compile(
                "com.kob.botrunningsystem.utils.Bot" + uid,
                addUid(bot.getBotCode(), uid)
        ).create().get();

        Integer direction = botInterface.nextMove(bot.getInput());

        System.out.println("user + move direction " + bot.getUserId() + " " + direction);

        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", bot.getUserId().toString());
        data.add("direction", direction.toString());
        // 服务器内部通信
        restTemplate.postForObject(receiveBotMoveUrl, data, String.class);
    }
}
