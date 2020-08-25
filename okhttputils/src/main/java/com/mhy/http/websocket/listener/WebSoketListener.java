package com.mhy.http.websocket.listener;

import okhttp3.Response;
import okio.ByteString;

/**
 *  可用于监听ws连接状态并进一步拓展
 */
public abstract class WebSoketListener {

  public void onOpen(Response response) {
  }

  public void onMessage(String text) {
  }

  /**
   * 图片 文件
   * @param bytes 包装
   */
  public void onMessage(ByteString bytes) {
  }

  public void onReconnect() {

  }

  public void onClosing(int code, String reason) {
  }


  public void onClosed(int code, String reason) {
  }

  public void onFailure(Throwable t, Response response) {
  }
}
