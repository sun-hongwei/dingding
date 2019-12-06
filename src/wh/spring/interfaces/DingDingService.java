package wh.spring.interfaces;

import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiRobotSendResponse;
import com.taobao.api.ApiException;

import java.util.List;
import java.util.Map;

/**
 * @author lubin
 *
 * 钉钉发送消息
 *
 * 2019-12-04
 */
public interface DingDingService {

    OapiRobotSendResponse sendDingMarkdownMessage(String secret, String webhook, List<String> contactPersons, String messageTitle, String message, String pageUrl, String picUrl) throws ApiException;
    OapiRobotSendResponse sendDingTextMessage(String secret, String webhook, List<String> contactPersons,String message) throws ApiException;
    OapiRobotSendResponse sendDingLinkMessage(String secret, String webhook, String title, String text,String messageUrl,String picUrl) throws ApiException;
    OapiRobotSendResponse sendDingOverallActionCardMessage(String secret, String webhook, String title, String text, String singleTitle, String singleURL) throws ApiException;
    OapiRobotSendResponse sendDingIndependentActionCardMessage(String secret, String webhook, String title, String text, List<OapiRobotSendRequest.Btns> btns, String btnOrientation) throws ApiException;
    OapiRobotSendResponse sendDingFeedCardMessage(String secret, String webhook, List<OapiRobotSendRequest.Links> linkList) throws ApiException;

}
