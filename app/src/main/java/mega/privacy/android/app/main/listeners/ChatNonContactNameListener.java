package mega.privacy.android.app.main.listeners;


import android.app.Activity;
import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.main.megachat.chatAdapters.MegaChatAdapter;
import mega.privacy.android.app.main.megachat.chatAdapters.MegaListChatAdapter;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaChatApiJava;
import nz.mega.sdk.MegaChatError;
import nz.mega.sdk.MegaChatRequest;
import nz.mega.sdk.MegaChatRequestListenerInterface;
import nz.mega.sdk.MegaError;

import static mega.privacy.android.app.utils.LogUtil.*;
import static mega.privacy.android.app.utils.TextUtil.*;

public class ChatNonContactNameListener implements MegaChatRequestListenerInterface {

    Context context;
    RecyclerView.ViewHolder holder;
    RecyclerView.Adapter adapter;
    boolean isUserHandle;
    DatabaseHandler dbH;
    String firstName;
    String lastName;
    String mail;
    long userHandle;
    boolean receivedFirstName = false;
    boolean receivedLastName = false;
    boolean receivedEmail = false;
    MegaApiAndroid megaApi;
    boolean isPreview = false;
    int pos;

    public ChatNonContactNameListener(Context context, RecyclerView.ViewHolder holder, RecyclerView.Adapter adapter, long userHandle, boolean isPreview) {
        this.context = context;
        this.holder = holder;
        this.adapter = adapter;
        this.isUserHandle = true;
        this.userHandle = userHandle;
        this.isPreview = isPreview;

        dbH = DatabaseHandler.getDbHandler(context);

        if (megaApi == null){
            megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
        }
    }

    public ChatNonContactNameListener(Context context, RecyclerView.ViewHolder holder, RecyclerView.Adapter adapter, long userHandle, boolean isPreview, int pos) {
        this.context = context;
        this.holder = holder;
        this.adapter = adapter;
        this.isUserHandle = true;
        this.userHandle = userHandle;
        this.isPreview = isPreview;
        this.pos = pos;

        dbH = DatabaseHandler.getDbHandler(context);

        if (megaApi == null){
            megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
        }
    }

    public ChatNonContactNameListener(Context context) {
        this.context = context;
        dbH = DatabaseHandler.getDbHandler(context);

        if (megaApi == null){
            megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
        }
    }

    @Override
    public void onRequestStart(MegaChatApiJava api, MegaChatRequest request) {

    }

    @Override
    public void onRequestUpdate(MegaChatApiJava api, MegaChatRequest request) {

    }

    @Override
    public void onRequestFinish(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {
        logDebug("onRequestFinish()");

        if (e.getErrorCode() == MegaError.API_OK){
            if (adapter == null) {
                return;
            }

            if(adapter instanceof MegaChatAdapter && holder == null){
                logWarning("holder is NULL");
                holder = ((MegaChatAdapter)adapter).queryIfHolderNull(pos);
                if (holder == null) {
                    logWarning("holder is NULL");
                    return;
                }
            }
            else {
                logWarning("Other adapter holder is NULL");
            }

            if(request.getType()==MegaChatRequest.TYPE_GET_FIRSTNAME){
                logDebug("First name received");
                firstName = request.getText();
                receivedFirstName = true;
                if(!isTextEmpty(firstName)){
                    dbH.setNonContactFirstName(firstName, request.getUserHandle()+"");
                    updateAdapter();
                }
            }
            else if(request.getType()==MegaChatRequest.TYPE_GET_LASTNAME){
                logDebug("Last name received");
                lastName = request.getText();
                receivedLastName = true;
                if(!isTextEmpty(lastName)){
                    dbH.setNonContactLastName(lastName, request.getUserHandle()+"");
                    updateAdapter();
                }
            }
            else if(request.getType()==MegaChatRequest.TYPE_GET_EMAIL){
                logDebug("Email received");
                mail = request.getText();
                receivedEmail = true;
                if(!isTextEmpty(mail)){
                    dbH.setNonContactEmail(mail, request.getUserHandle()+"");
                    updateAdapter();
                }
            }
        }
        else{
            logError("ERROR: requesting: " + request.getRequestString());
        }
    }

    private void updateAdapter() {
        if (receivedFirstName || receivedLastName || receivedEmail) {
            logDebug("updateAdapter");
            if (adapter instanceof MegaChatAdapter) {
                adapter.notifyItemChanged(holder.getAdapterPosition());
            } else if (adapter instanceof MegaListChatAdapter) {
                ((MegaListChatAdapter) adapter).updateNonContactName(holder.getAdapterPosition(), this.userHandle);
            }

            receivedFirstName = false;
            receivedLastName = false;
            receivedEmail = false;
        } else {
            logWarning("NOT updateAdapter: " + receivedFirstName + ":" + receivedLastName + ":" + receivedEmail);
        }
    }

    @Override
    public void onRequestTemporaryError(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {

    }
}