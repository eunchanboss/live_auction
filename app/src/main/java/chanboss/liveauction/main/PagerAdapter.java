package chanboss.liveauction.main;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import chanboss.liveauction.chatting.chatroom.ChatroomListFragment;
import chanboss.liveauction.notice.PostListFragment;
import chanboss.liveauction.streaming.StreamRoomFragment;


public class PagerAdapter extends FragmentPagerAdapter {

    private static int PAGE_NUMBER=3;

    public PagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return PostListFragment.newInstance();
            case 1:
                return StreamRoomFragment.newInstance();
            case 2:
                return ChatroomListFragment.newInstance();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return PAGE_NUMBER;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "장터";
            case 1:
                return "실시간";
            case 2:
                return "채팅목록";
            default:
                return null;
        }
    }
}
