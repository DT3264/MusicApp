package com.ggg.songplayer;

import io.reactivex.Observable;

public class RxMessage {
    private String key;
    private Object[] val;

    public RxMessage(String _key, Object[] _val){
        this.key = _key;
        this.val = _val;
    }

    public String getKey(){
        return key;
    }

    public Object[] getVal(){
        return this.val;
    }

    public static Observable<RxMessage> transmitMessage(RxMessage rxMessage) {
        return Observable.just(rxMessage);
    }
}
