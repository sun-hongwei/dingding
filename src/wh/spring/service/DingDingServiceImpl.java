package wh.spring.service;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiRobotSendResponse;
import com.taobao.api.ApiException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import wh.spring.interfaces.DingDingService;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * @author lubin
 * <p>
 * 钉钉发送消息
 * <p>
 * 2019-12-04
 */
@Service
public class DingDingServiceImpl implements DingDingService {

    private OapiRobotSendRequest request;
    private OapiRobotSendRequest.Markdown markdown;
    private OapiRobotSendRequest.Text text;
    private OapiRobotSendRequest.Link link;
    private OapiRobotSendRequest.Feedcard feedcard;
    private OapiRobotSendRequest.At at;
    private OapiRobotSendRequest.Actioncard actioncard;
    private DingTalkClient client;
    private OapiRobotSendResponse response;

    private String signature;

    /**
     * 钉钉文档地址
     * https://ding-doc.dingtalk.com/doc#/serverapi2/qf2nxq
     * <p>
     * 发送钉钉群消息  Markdown  类型
     *
     * @param secret         密钥，机器人安全设置页面，加签一栏下面显示的SEC开头的字符串  (必填项)
     * @param webhook        webhook  (必填项)
     * @param contactPersons 推送人List,不传就是@所有人   (非必填项)
     * @param messageTitle   消息标题  (必填项)
     * @param message        消息内容    (必填项)
     * @param pageUrl        跳转的页面   (非必填项)
     * @param picUrl         显示图片的外网地址   (非必填项)
     * @return
     */
    @Override
    public OapiRobotSendResponse sendDingMarkdownMessage(String secret, String webhook, List<String> contactPersons, String messageTitle, String message, String pageUrl, String picUrl) throws ApiException {
        this.response = new OapiRobotSendResponse();
        if (StringUtils.isEmpty(secret)) {
            this.response.setErrcode(400L);
            this.response.setErrmsg("secret参数为空");
            return this.response;
        }
        if (StringUtils.isEmpty(webhook)) {
            this.response.setErrcode(400L);
            this.response.setErrmsg("webhook参数为空");
            return this.response;
        }
        if (StringUtils.isEmpty(messageTitle)) {
            this.response.setErrcode(400L);
            this.response.setErrmsg("messageTitle参数为空");
            return this.response;
        }
        if (StringUtils.isEmpty(message)) {
            this.response.setErrcode(400L);
            this.response.setErrmsg("message参数为空");
            return this.response;
        }
        this.signature = getSignature(secret);
        if (this.signature != null) {
            this.client = new DefaultDingTalkClient(webhook + this.signature);
            this.request = new OapiRobotSendRequest();
            this.at = new OapiRobotSendRequest.At();
            String persons = "";
            if (CollectionUtils.isEmpty(contactPersons)) {
                this.at.setIsAtAll("true");
            } else {
                for (String person : contactPersons) {
                    persons += "@" + person;
                }
                this.at.setAtMobiles(contactPersons);
            }
            this.request.setAt(this.at);
            this.markdown = new OapiRobotSendRequest.Markdown();
            this.request.setMsgtype("markdown");
            this.markdown.setTitle(messageTitle);

            String page = "";
            if (!StringUtils.isEmpty(pageUrl)) {
                page = "[页面](" + pageUrl + ")";
            }
            String pic = "";
            if (!StringUtils.isEmpty(picUrl)) {
                pic = "![screenshot](" + picUrl + ")";
            }

            this.markdown.setText(message + persons + pic + page);
            this.request.setMarkdown(this.markdown);
            this.client.execute(this.request);
            this.response.setErrcode(200L);
            this.response.setErrmsg("发送成功");
            return this.response;
        } else {
            this.response.setErrcode(500L);
            this.response.setErrmsg("真实密钥获取失败");
            return this.response;
        }
    }

    /**
     * 发送钉钉群消息  Text  类型
     *
     * @param secret         密钥，机器人安全设置页面，加签一栏下面显示的SEC开头的字符串  (必填项)
     * @param webhook        webhook  (必填项)
     * @param contactPersons 推送人List,不传就是@所有人   (非必填项)
     * @param message        消息内容  (必填项)
     * @return
     */
    @Override
    public OapiRobotSendResponse sendDingTextMessage(String secret, String webhook, List<String> contactPersons, String message) throws ApiException {
        this.response = new OapiRobotSendResponse();
        if (StringUtils.isEmpty(secret)) {
            this.response.setErrcode(400L);
            this.response.setErrmsg("secret参数为空");
            return this.response;
        }
        if (StringUtils.isEmpty(webhook)) {
            this.response.setErrcode(400L);
            this.response.setErrmsg("webhook参数为空");
            return this.response;
        }
        if (StringUtils.isEmpty(message)) {
            this.response.setErrcode(400L);
            this.response.setErrmsg("message参数为空");
            return this.response;
        }
        this.signature = getSignature(secret);
        if (this.signature != null) {
            this.client = new DefaultDingTalkClient(webhook + this.signature);
            this.request = new OapiRobotSendRequest();
            this.at = new OapiRobotSendRequest.At();
            String persons = "";
            if (CollectionUtils.isEmpty(contactPersons)) {
                this.at.setIsAtAll("true");
            } else {
                for (String person : contactPersons) {
                    persons += "@" + person;
                }
            }
            this.at.setAtMobiles(contactPersons);
            this.request.setAt(this.at);

            this.request.setMsgtype("text");
            this.text = new OapiRobotSendRequest.Text();
            this.text.setContent(message);
            this.request.setText(this.text);
            this.client.execute(this.request);
            this.response.setErrcode(200L);
            this.response.setErrmsg("发送成功");
            return this.response;
        } else {
            this.response.setErrcode(500L);
            this.response.setErrmsg("真实密钥获取失败");
            return this.response;
        }
    }

    /**
     * 发送钉钉群消息  Link  类型
     *
     * @param secret     密钥，机器人安全设置页面，加签一栏下面显示的SEC开头的字符串  (必填项)
     * @param webhook    webhook  (必填项)
     * @param title      文章标题   (必填项)
     * @param text       文章描述   (必填项)
     * @param messageUrl 文章地址   (必填项)
     * @param picUrl     图片路径   (非必填项)
     * @return
     */
    @Override
    public OapiRobotSendResponse sendDingLinkMessage(String secret, String webhook, String title, String text, String messageUrl, String picUrl) throws ApiException {
        this.response = new OapiRobotSendResponse();
        if (StringUtils.isEmpty(secret)) {
            this.response.setErrcode(400L);
            this.response.setErrmsg("secret参数为空");
            return this.response;
        }
        if (StringUtils.isEmpty(webhook)) {
            this.response.setErrcode(400L);
            this.response.setErrmsg("webhook参数为空");
            return this.response;
        }
        if (StringUtils.isEmpty(title)) {
            this.response.setErrcode(400L);
            this.response.setErrmsg("title参数为空");
            return this.response;
        }
        if (StringUtils.isEmpty(text)) {
            this.response.setErrcode(400L);
            this.response.setErrmsg("message参数为空");
            return this.response;
        }
        if (StringUtils.isEmpty(messageUrl)) {
            this.response.setErrcode(400L);
            this.response.setErrmsg("messageUrl参数为空");
            return this.response;
        }
        this.signature = getSignature(secret);
        if (this.signature != null) {
            this.client = new DefaultDingTalkClient(webhook + this.signature);
            this.request = new OapiRobotSendRequest();
            this.request.setMsgtype("link");
            this.link = new OapiRobotSendRequest.Link();
            this.link.setTitle(title);
            this.link.setText(text);
            this.link.setPicUrl(picUrl);
            this.link.setMessageUrl(messageUrl);
            this.request.setLink(this.link);
            this.client.execute(this.request);
            this.response.setErrcode(200L);
            this.response.setErrmsg("发送成功");
            return this.response;
        } else {
            this.response.setErrcode(200L);
            this.response.setErrmsg("真实密钥获取失败");
            return this.response;
        }
    }

    /**
     * 整体跳转ActionCard类型
     *
     * @param secret      密钥，机器人安全设置页面，加签一栏下面显示的SEC开头的字符串  (必填项)
     * @param webhook     webhook  (必填项)
     * @param title       首屏会话透出的展示内容  (必填项)
     * @param text        markdown格式的消息    (必填项)
     * @param singleTitle 单个按钮的方案。(设置此项和singleURL后btns无效),按钮标题   (必填项)
     * @param singleURL   点击singleTitle按钮触发的URL   (必填项)
     * @return
     * @throws ApiException
     */
    @Override
    public OapiRobotSendResponse sendDingOverallActionCardMessage(String secret, String webhook, String title, String text, String singleTitle, String singleURL) throws ApiException {
        this.response = new OapiRobotSendResponse();
        if (StringUtils.isEmpty(secret)) {
            this.response.setErrcode(400L);
            this.response.setErrmsg("secret参数为空");
            return this.response;
        }
        if (StringUtils.isEmpty(webhook)) {
            this.response.setErrcode(400L);
            this.response.setErrmsg("webhook参数为空");
            return this.response;
        }
        if (StringUtils.isEmpty(title)) {
            this.response.setErrcode(400L);
            this.response.setErrmsg("title参数为空");
            return this.response;
        }
        if (StringUtils.isEmpty(text)) {
            this.response.setErrcode(400L);
            this.response.setErrmsg("text参数为空");
            return this.response;
        }
        if (StringUtils.isEmpty(singleTitle)) {
            this.response.setErrcode(400L);
            this.response.setErrmsg("singleTitle参数为空");
            return this.response;
        }
        if (StringUtils.isEmpty(singleURL)) {
            this.response.setErrcode(400L);
            this.response.setErrmsg("singleURL参数为空");
            return this.response;
        }

        this.signature = getSignature(secret);
        if (this.signature != null) {
            this.client = new DefaultDingTalkClient(webhook + this.signature);
            this.request = new OapiRobotSendRequest();
            this.request.setMsgtype("actionCard");
            this.actioncard = new OapiRobotSendRequest.Actioncard();
            this.actioncard.setTitle(title);
            this.actioncard.setText(text);
            this.actioncard.setSingleTitle(singleTitle);
            this.actioncard.setSingleURL(singleURL);
            this.request.setActionCard(this.actioncard);
            this.client.execute(this.request);
            this.response.setErrcode(200L);
            this.response.setErrmsg("发送成功");
            return this.response;
        } else {
            this.response.setErrcode(200L);
            this.response.setErrmsg("真实密钥获取失败");
            return this.response;
        }
    }

    /**
     * 独立跳转ActionCard类型
     *
     * @param secret         密钥，机器人安全设置页面，加签一栏下面显示的SEC开头的字符串  (必填项)
     * @param webhook        webhook  (必填项)
     * @param title          首屏会话透出的展示内容   (必填项)
     * @param text           markdown格式的消息     (必填项)
     * @param btns           按钮List   (必填项)
     *                       List<OapiRobotSendRequest.Btns>
     *                       {
     *                       title:按钮名称,
     *                       actionURL:点击按钮触发的URL
     *                       }
     * @param btnOrientation 0-按钮竖直排列，1-按钮横向排列   (必填项)
     * @return
     * @throws ApiException
     */
    @Override
    public OapiRobotSendResponse sendDingIndependentActionCardMessage(String secret, String webhook, String title, String text, List<OapiRobotSendRequest.Btns> btns, String btnOrientation) throws ApiException {

        this.response = new OapiRobotSendResponse();
        if (StringUtils.isEmpty(secret)) {
            this.response.setErrcode(400L);
            this.response.setErrmsg("secret参数为空");
            return this.response;
        }
        if (StringUtils.isEmpty(webhook)) {
            this.response.setErrcode(400L);
            this.response.setErrmsg("webhook参数为空");
            return this.response;
        }
        if (StringUtils.isEmpty(title)) {
            this.response.setErrcode(400L);
            this.response.setErrmsg("title参数为空");
            return this.response;
        }
        if (StringUtils.isEmpty(text)) {
            this.response.setErrcode(400L);
            this.response.setErrmsg("text参数为空");
            return this.response;
        }
        if (CollectionUtils.isEmpty(btns)) {
            this.response.setErrcode(400L);
            this.response.setErrmsg("btns参数为空");
            return this.response;
        }
        if (StringUtils.isEmpty(btnOrientation)) {
            btnOrientation = "0";
        }

        this.signature = getSignature(secret);
        if (this.signature != null) {
            this.client = new DefaultDingTalkClient(webhook + this.signature);
            this.request = new OapiRobotSendRequest();
            this.request.setMsgtype("actionCard");
            this.actioncard = new OapiRobotSendRequest.Actioncard();
            this.actioncard.setTitle(title);
            this.actioncard.setText(text);
            this.actioncard.setBtns(btns);
            this.actioncard.setBtnOrientation(btnOrientation);
            this.request.setActionCard(this.actioncard);
            this.client.execute(this.request);
            this.response.setErrcode(200L);
            this.response.setErrmsg("发送成功");
            return this.response;
        } else {
            this.response.setErrcode(200L);
            this.response.setErrmsg("真实密钥获取失败");
            return this.response;
        }
    }

    /**
     * FeedCard类型
     *
     * @param secret   密钥，机器人安全设置页面，加签一栏下面显示的SEC开头的字符串  (必填项)
     * @param webhook  webhook  (必填项)
     * @param linkList OapiRobotSendRequest.Links类型List   (必填项)
     *                 {
     *                 title:单条信息文本
     *                 messageURL:点击单条信息到跳转链接
     *                 picURL:单条信息后面图片的URL
     *                 }
     * @return
     * @throws ApiException
     */
    @Override
    public OapiRobotSendResponse sendDingFeedCardMessage(String secret, String webhook, List<OapiRobotSendRequest.Links> linkList) throws ApiException {

        this.response = new OapiRobotSendResponse();
        if (StringUtils.isEmpty(secret)) {
            this.response.setErrcode(400L);
            this.response.setErrmsg("secret参数为空");
            return this.response;
        }
        if (StringUtils.isEmpty(webhook)) {
            this.response.setErrcode(400L);
            this.response.setErrmsg("webhook参数为空");
            return this.response;
        }
        if (CollectionUtils.isEmpty(linkList)) {
            this.response.setErrcode(400L);
            this.response.setErrmsg("linkList参数为空");
            return this.response;
        }
        this.signature = getSignature(secret);
        if (this.signature != null) {
            this.client = new DefaultDingTalkClient(webhook + this.signature);
            this.request = new OapiRobotSendRequest();
            this.request.setMsgtype("feedCard");
            this.feedcard = new OapiRobotSendRequest.Feedcard();
            this.feedcard.setLinks(linkList);
            this.request.setFeedCard(feedcard);
            this.client.execute(this.request);
            this.response.setErrcode(200L);
            this.response.setErrmsg("发送成功");
            return this.response;
        } else {
            this.response.setErrcode(200L);
            this.response.setErrmsg("真实密钥获取失败");
            return this.response;
        }
    }


    /**
     * 获取真实秘钥
     * <p>
     * 算法
     * 获取  timestamp  当前时间戳，单位是毫秒，与请求调用时间误差不能超过1小时
     * sign       把 timestamp+"\n"+密钥 当做签名字符串，使用HmacSHA256算法计算签名，
     * 然后进行Base64 encode，最后再把签名参数再进行urlEncode，
     * 得到最终的签名（需要使用UTF-8字符集）。
     *
     * @param secret 密钥，机器人安全设置页面，加签一栏下面显示的SEC开头的字符串
     * @return
     */
    private String getSignature(String secret) {
        Long timestamp = System.currentTimeMillis();
        String stringToSign = timestamp + "\n" + secret;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
            String signature = URLEncoder.encode(new String(Base64.encodeBase64(signData)), "UTF-8");
            /**
             * 把 timestamp和第一步得到的签名值拼接到webhook中。
             * https://oapi.dingtalk.com/robot/send?access_token=XXXXXX&timestamp=XXX&sign=XXX
             */
            return "&timestamp=" + timestamp + "&sign=" + signature;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }


    /*@Test
    public void test() throws ApiException {
        String secret = "SEC377c02264ab0b2783fc72f0d9373cf8fb4d3c28028e0a8fa5b670c5565b3e839";
        String webhook =
                "https://oapi.dingtalk.com/robot/send?access_token=f070bc9edad1abf250132a1790c2b760405698bcbfc5ba6ee96e860894735db1";
        List<String> contactPersons = Arrays.asList("18443126375", "15590025520");
        String messageTitle = "请假审批";
        String message = "请假审批已通过";
        String pageUrl = "http://172.16.0.22:8123/web-socket/";


        //new DingDingServiceImpl().sendDingMarkdownMessage(secret, webhook, contactPersons, messageTitle, message, pageUrl, picUrl);
        //new DingDingServiceImpl().sendDingTextMessage(secret, webhook, contactPersons,message);

        String singleUrl = "https://gw.alicdn.com/tfs/TB1ut3xxbsrBKNjSZFpXXcXhFXa-846-786.png";
        //OapiRobotSendResponse o=new DingDingServiceImpl().sendDingLinkMessage(secret, webhook, "时代的火车向前开","这个即将发布的新版本，创始人xx称它为“红树林","https://www.dingtalk.com/s?__biz=MzA4NjMwMTA2Ng==&mid=2650316842&idx=1&sn=60da3ea2b29f1dcc43a7c8e4a7c97a16&scene=2&srcid=09189AnRJEdIiWVaKltFzNTw&from=timeline&isappinstalled=0&key=&ascene=2&uin=&devicetype=android-23&version=26031933&nettype=WIFI","");
        //OapiRobotSendResponse o=new DingDingServiceImpl().sendDingOverallActionCardMessage(secret, webhook,"乔布斯 20 年前想打造一间苹果咖啡厅，而它正是 Apple Store 的前身",text,"阅读全文","https://www.dingtalk.com/");
        String title = "乔布斯 20 年前想打造一间苹果咖啡厅，而它正是 Apple Store 的前身";
        String text = "![screenshot](@lADOpwk3K80C0M0FoA)" +
                "### 乔布斯 20 年前想打造的苹果咖啡厅" +
                "Apple Store 的设计正从原来满满的科技感走向生活化，而其生活化的走向其实可以追溯到 20 年前苹果一个建立咖啡馆的计划";
        OapiRobotSendRequest.Btns btns = new OapiRobotSendRequest.Btns();
        btns.setActionURL("https://www.dingtalk.com/");
        btns.setTitle("内容不错");
        OapiRobotSendRequest.Btns btns1 = new OapiRobotSendRequest.Btns();
        btns1.setActionURL("https://www.dingtalk.com/");
        btns1.setTitle("不感兴趣");
        //OapiRobotSendResponse o=new DingDingServiceImpl().sendDingIndependentActionCardMessage(secret, webhook,title,text,Arrays.asList(btns,btns1),"0");

        OapiRobotSendRequest.Links link = new OapiRobotSendRequest.Links();
        link.setTitle("时代的火车向前开");
        link.setMessageURL("https://www.dingtalk.com/s?__biz=MzA4NjMwMTA2Ng==&mid=2650316842&idx=1&sn=60da3ea2b29f1dcc43a7c8e4a7c97a16&scene=2&srcid=09189AnRJEdIiWVaKltFzNTw&from=timeline&isappinstalled=0&key=&ascene=2&uin=&devicetype=android-23&version=26031933&nettype=WIFI");
        link.setPicURL("https://www.dingtalk.com/");
        OapiRobotSendRequest.Links link1 = new OapiRobotSendRequest.Links();
        link1.setTitle("时代的火车向前开1");
        link1.setMessageURL("https://www.dingtalk.com/s?__biz=MzA4NjMwMTA2Ng==&mid=2650316842&idx=1&sn=60da3ea2b29f1dcc43a7c8e4a7c97a16&scene=2&srcid=09189AnRJEdIiWVaKltFzNTw&from=timeline&isappinstalled=0&key=&ascene=2&uin=&devicetype=android-23&version=26031933&nettype=WIFI");
        link1.setPicURL("https://www.dingtalk.com/");
        OapiRobotSendResponse o = new DingDingServiceImpl().sendDingFeedCardMessage(secret, webhook, Arrays.asList(link, link1));
        System.out.println(o.getErrcode() + o.getErrmsg());
    }*/
}
