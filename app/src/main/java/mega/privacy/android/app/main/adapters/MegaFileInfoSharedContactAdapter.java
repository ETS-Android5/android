package mega.privacy.android.app.main.adapters;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.util.ArrayList;

import mega.privacy.android.app.R;
import mega.privacy.android.app.main.FileInfoActivity;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaShare;

import static mega.privacy.android.app.utils.LogUtil.*;

public class MegaFileInfoSharedContactAdapter extends MegaSharedFolderAdapter {

    public MegaFileInfoSharedContactAdapter(Context _context, MegaNode node, ArrayList<MegaShare> _shareList, RecyclerView _lv) {
        super(_context,node,_shareList,_lv);
    }
    
    @Override
    public void onClick(View v) {
        logDebug("onClick");
        
        ViewHolderShareList holder = (ViewHolderShareList) v.getTag();
        int currentPosition = holder.currentPosition;
        final MegaShare s = (MegaShare) getItem(currentPosition);
        
        switch (v.getId()){
            case R.id.shared_folder_three_dots_layout:{
                if(multipleSelect){
                    ((FileInfoActivity) context).itemClick(currentPosition);
                }
                else{
                    ((FileInfoActivity) context).showOptionsPanel(s);
                }
                
                break;
            }
            case R.id.shared_folder_item_layout:{
                ((FileInfoActivity) context).itemClick(currentPosition);
                break;
            }
        }
    }
    
    public void toggleSelection(int pos) {
        logDebug("Position: " + pos);
        if (selectedItems.get(pos, false)) {
            logDebug("Delete pos: " + pos);
            selectedItems.delete(pos);
        }
        else {
            logDebug("PUT pos: " + pos);
            selectedItems.put(pos, true);
        }
        notifyItemChanged(pos);
        
        MegaSharedFolderAdapter.ViewHolderShareList view = (MegaSharedFolderAdapter.ViewHolderShareList) listFragment.findViewHolderForLayoutPosition(pos);
        if(view!=null){
            logDebug("Start animation: " + pos);
            Animation flipAnimation = AnimationUtils.loadAnimation(context, R.anim.multiselect_flip);
            flipAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    
                }
                
                @Override
                public void onAnimationEnd(Animation animation) {
                    if (selectedItems.size() <= 0){
                        ((FileInfoActivity) context).hideMultipleSelect();
                    }
                }
                
                @Override
                public void onAnimationRepeat(Animation animation) {
                    
                }
            });
            view.imageView.startAnimation(flipAnimation);
        }
    }
    
    public void toggleAllSelection(int pos) {
        logDebug("Position: " + pos);
        final int positionToflip = pos;
        
        if (selectedItems.get(pos, false)) {
            logDebug("Delete pos: " + pos);
            selectedItems.delete(pos);
        }
        else {
            logDebug("PUT pos: " + pos);
            selectedItems.put(pos, true);
        }

        logDebug("Adapter type is LIST");
        MegaSharedFolderAdapter.ViewHolderShareList view = (MegaSharedFolderAdapter.ViewHolderShareList) listFragment.findViewHolderForLayoutPosition(pos);
        if(view!=null){
            logDebug("Start animation: " + pos);
            Animation flipAnimation = AnimationUtils.loadAnimation(context, R.anim.multiselect_flip);
            flipAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    
                }
                
                @Override
                public void onAnimationEnd(Animation animation) {
                    if (selectedItems.size() <= 0){
                        ((FileInfoActivity) context).hideMultipleSelect();
                    }
                    notifyItemChanged(positionToflip);
                }
                
                @Override
                public void onAnimationRepeat(Animation animation) {
                    
                }
            });
            view.imageView.startAnimation(flipAnimation);
        }
        else{
            logError("NULL view pos: " + positionToflip);
            notifyItemChanged(pos);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        ViewHolderShareList holder = (ViewHolderShareList) v.getTag();
        int currentPosition = holder.currentPosition;

        ((FileInfoActivity) context).activateActionMode();
        ((FileInfoActivity) context).itemClick(currentPosition);

        return true;
    }
}
