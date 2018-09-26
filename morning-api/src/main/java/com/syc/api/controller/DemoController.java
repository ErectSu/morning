package com.syc.api.controller;

import com.jfinal.kit.StrKit;
import com.syc.api.kit.EmailKit;
import com.syc.api.service.common.RedisService;
import com.jfinal.kit.Ret;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author choxsu
 * @CrossOrigin 这注解是开启跨域访问
 */
@RestController
@RequestMapping(value = "/demo")
@Slf4j
@CrossOrigin
public class DemoController {

    @Autowired
    private RedisService redisService;

    @Autowired
    EmailKit emailKit;

    final String MS = "MS";
    //20个线程池并发数
    static ExecutorService executor = Executors.newFixedThreadPool(20);

    /**
     * @api {get} /demo/test #1Redis数据保存
     * @apiName #1Redis数据保存
     * @apiGroup Redis
     * @apiVersion 1.0.0
     * @apiDescription Redis数据保存Demo接口，key和value都是必须传递的参数
     * @apiParam {String} key 必须的Redis插入key
     * @apiParam {String} value 必须的Redis插入value
     * @apiSuccess {String} state  ok 成功 fail 失败
     * @apiSuccess {String} msg  提示信息
     * @apiSuccess {Object} data  返回的数据
     */
    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public Map demoTest(@RequestParam String key, @RequestParam String value) {
        if (StrKit.isBlank(key)) {
            return Ret.fail().set("msg", "key不能为空串");
        }
        //模拟20万并发
        int bfNum = 200000;
        //500人能秒杀到
        int num = 500;
        //验证程序执行时数据量是否正确
        Object n1 = redisService.get("n");
        int n = n1 == null ? 0 : (int) n1;
        String uid = "";
        while (bfNum-- >= 0) {
            uid = String.valueOf((new Random().nextInt(100000) + 990000));
            if (n < num) {
                redisService.listPush(MS, uid + "|" + n);
                System.out.println(uid + "秒杀成功" + n);
            } else {
                System.out.println(uid + "秒杀已结束");
                break;
            }
            n++;
        }
        System.out.println(n);
        redisService.set("n", n);
        List<Object> ms = redisService.listRange(MS, 0, 1000);
        System.out.println(ms.size());
        boolean isSu = redisService.set(key, value);
        if (isSu) {
            return Ret.ok("msg", "插入成功");
        }
        return Ret.fail("msg", "系统错误");
    }

    /**
     * @api {get} /demo/getTest #2Redis数据获取
     * @apiName #2Redis数据获取
     * @apiGroup Redis
     * @apiVersion 1.0.0
     * @apiDescription Redis数据查询接口，key是必须传递的参数
     * @apiParam {String} key 必须的Redis获取的key
     * @apiSuccess {String} state  ok 成功 fail 失败
     * @apiSuccess {String} msg  提示信息
     * @apiSuccess {Object} data  返回的数据
     * @apiSuccess {String} data.value redis value
     */
    @RequestMapping(value = "/getTest", method = RequestMethod.GET)
    public Map getTest(@RequestParam String key) {
        if (StrKit.isBlank(key)) {
            return Ret.fail().set("msg", "key不能为空串");
        }
        Object o = redisService.get(key);
        Map map = new HashMap();
        map.put("value", o);
        return Ret.ok("data", map);
    }

    /**
     * @api {get} /demo/sendMail #1邮件发送【text版本】
     * @apiName #1邮件发送【text版本】
     * @apiGroup Mail
     * @apiVersion 1.0.0
     * @apiDescription 邮件发送测试接口，这个只能发送文字，email是必须传递的参数
     * @apiParam {String} email 目标邮箱，我们发送到的邮箱
     * @apiSuccess {String} state  ok 成功 fail 失败
     * @apiSuccess {String} msg  提示信息
     * @apiSuccess {Object} data  返回的数据
     */
    @RequestMapping(value = "/sendMail", method = RequestMethod.GET)
    public Map sendMail(@RequestParam("email") String email) {
        boolean b = emailKit.sendEmail(
                email,
                "标题 123 ",
                "内容 <div style='color:red;'>123</div> ");
        if (!b) {
            return Ret.fail().set("msg", "邮件发送失败");
        }
        return Ret.ok().set("msg", "发送到邮件：" + email + " 成功！");
    }

    /**
     * @api {get} /demo/sendHtmlMail #2邮件发送【HTML版本】
     * @apiName #2邮件发送【HTML版本】
     * @apiGroup Mail
     * @apiVersion 1.0.0
     * @apiDescription 邮件发送测试接口，HTML版本，email是必须传递的参数
     * @apiParam {String} email 目标邮箱，我们发送到的邮箱
     * @apiSuccess {String} state  ok 成功 fail 失败
     * @apiSuccess {String} msg  提示信息
     * @apiSuccess {Object} data  返回的数据
     */
    @RequestMapping(value = "/sendHtmlMail", method = RequestMethod.GET)
    public Map sendHtmlMail(@RequestParam("email") String email) {
        boolean b = emailKit.sendHtmlEmail(
                email,
                "标题 123 ",
                "内容 <div style='color:red;'>123</div>");
        if (!b) {
            return Ret.fail().set("msg", "邮件发送失败");
        }
        return Ret.ok().set("msg", "发送到邮件：" + email + " 成功！");
    }

}
