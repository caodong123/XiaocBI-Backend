package com.yupi.springbootinit.manager;

import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AiManager {

    @Autowired
    private YuCongMingClient yuCongMingClient;

    public String doChat(Long modelId,String msg){
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(modelId);
        devChatRequest.setMessage(msg);

        BaseResponse<DevChatResponse> response = yuCongMingClient.doChat(devChatRequest);
        String content = response.getData().getContent();
        System.out.println(content);
        return content;
    }

}
