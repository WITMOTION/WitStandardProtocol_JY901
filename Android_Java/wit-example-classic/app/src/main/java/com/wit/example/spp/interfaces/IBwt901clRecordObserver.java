package com.wit.example.spp.interfaces;

import com.wit.example.spp.Bwt901cl;

/**
 * 记录数据通知
 *
 * @author huangyajun
 * @date 2022/4/26 11:27
 */
public interface IBwt901clRecordObserver {

    /**
     * 接收通知
     *
     * @author huangyajun
     * @date 2022/4/26 13:42
     */
    void onRecord(Bwt901cl bwt901cl);

}
