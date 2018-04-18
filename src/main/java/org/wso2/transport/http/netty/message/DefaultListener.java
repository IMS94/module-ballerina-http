/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.message;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Default implementation of the message Listener.
 */
public class DefaultListener implements Listener {

    private static final int MAXIMUM_BYTE_SIZE = 2097152; //Maximum threshold of reading bytes(2MB)
    private AtomicInteger cumulativeByteQuantity = new AtomicInteger(0);
    private final ChannelHandlerContext ctx;
    private boolean readCompleted = false;

    public DefaultListener(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void onAdd(HttpContent httpContent) {
        int count = this.cumulativeByteQuantity.addAndGet(httpContent.content().readableBytes());
        System.out.println("onAdd count:" + count);
        if (count < MAXIMUM_BYTE_SIZE) {
            if (Util.isLastHttpContent(httpContent)) {
                readCompleted = true;
            } else {
                this.ctx.channel().read();
            }
        }
    }

    @Override
    public void onRemove(HttpContent httpContent) {
        int count = this.cumulativeByteQuantity.addAndGet(-(httpContent.content().readableBytes()));
        System.out.println("onRemove count:" + count);
        if (count < MAXIMUM_BYTE_SIZE && !readCompleted) {
            this.ctx.channel().read();
        }
    }
}
