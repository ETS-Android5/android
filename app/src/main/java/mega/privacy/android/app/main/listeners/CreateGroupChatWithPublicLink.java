package mega.privacy.android.app.main.listeners;


import android.content.Context;
import android.content.Intent;
import android.util.Pair;
import com.jeremyliao.liveeventbus.LiveEventBus;

import mega.privacy.android.app.main.FileExplorerActivity;
import mega.privacy.android.app.main.ManagerActivity;
import mega.privacy.android.app.main.megachat.ChatActivity;
import mega.privacy.android.app.main.megachat.ChatExplorerActivity;
import mega.privacy.android.app.main.megachat.GroupChatInfoActivity;
import nz.mega.sdk.MegaChatApiJava;
import nz.mega.sdk.MegaChatError;
import nz.mega.sdk.MegaChatRequest;
import nz.mega.sdk.MegaChatRequestListenerInterface;

import static mega.privacy.android.app.constants.EventConstants.EVENT_LINK_RECOVERED;
import static mega.privacy.android.app.constants.EventConstants.EVENT_MEETING_CREATED;
import static mega.privacy.android.app.utils.Constants.*;
import static mega.privacy.android.app.utils.LogUtil.*;

public class CreateGroupChatWithPublicLink implements MegaChatRequestListenerInterface {

    Context context;
    String title;

    public CreateGroupChatWithPublicLink(Context context, String title) {
        this.context = context;
        this.title = title;
    }

    public CreateGroupChatWithPublicLink() { }

    @Override
    public void onRequestStart(MegaChatApiJava api, MegaChatRequest request) {

    }

    @Override
    public void onRequestUpdate(MegaChatApiJava api, MegaChatRequest request) {

    }

    @Override
    public void onRequestFinish(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {
        logDebug("onRequestFinish: " + e.getErrorCode() + "_" + request.getRequestString());

        if(request.getType() == MegaChatRequest.TYPE_CREATE_CHATROOM) {
            if (e.getErrorCode() == MegaChatError.ERROR_OK) {
                if(request.getNumber() == 1){
                    logDebug("Meeting created");
                    LiveEventBus.get(EVENT_MEETING_CREATED, Long.class).post(request.getChatHandle());
                }else{
                    logDebug("Chat created - get link");
                    api.createChatLink(request.getChatHandle(), this);
                }
            }
            else{
                if(context instanceof ManagerActivity){
                    ((ManagerActivity) context).onRequestFinishCreateChat(e.getErrorCode(), request.getChatHandle());
                }
                else if(context instanceof FileExplorerActivity){
                    ((FileExplorerActivity) context).onRequestFinishCreateChat(e.getErrorCode(), request.getChatHandle(), false);
                }
                else if(context instanceof ChatExplorerActivity){
                    ((ChatExplorerActivity) context).onRequestFinishCreateChat(e.getErrorCode(), request.getChatHandle(), false);
                }
                else if(context instanceof GroupChatInfoActivity){
                    ((GroupChatInfoActivity) context).onRequestFinishCreateChat(e.getErrorCode(), request.getChatHandle(), false);
                }
            }
        }
        else if (request.getType() == MegaChatRequest.TYPE_CHAT_LINK_HANDLE) {
            Pair<Long, String> chatAndLink = Pair.create(request.getChatHandle(), request.getText());
            LiveEventBus.get(EVENT_LINK_RECOVERED, Pair.class).post(chatAndLink);

            if (request.getFlag() == false) {
              if (request.getNumRetry() == 1) {
                   logDebug("Chat link exported");

                   if(context instanceof ManagerActivity){
                       Intent intent = new Intent(context, ChatActivity.class);
                       intent.setAction(ACTION_CHAT_SHOW_MESSAGES);
                       intent.putExtra(CHAT_ID, request.getChatHandle());
                       intent.putExtra("PUBLIC_LINK", e.getErrorCode());
                       intent.putExtra(CHAT_LINK_EXTRA, request.getText());
                       context.startActivity(intent);
                   }
                   else if(context instanceof FileExplorerActivity){
                       ((FileExplorerActivity) context).onRequestFinishCreateChat(e.getErrorCode(), request.getChatHandle(), true);
                   }
                   else if(context instanceof ChatExplorerActivity){
                       ((ChatExplorerActivity) context).onRequestFinishCreateChat(e.getErrorCode(), request.getChatHandle(), true);
                   }
                   else if(context instanceof GroupChatInfoActivity){
                       ((GroupChatInfoActivity) context).onRequestFinishCreateChat(e.getErrorCode(), request.getChatHandle(), true);
                   }
                }
            }
        }
    }

    @Override
    public void onRequestTemporaryError(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {

    }
}
