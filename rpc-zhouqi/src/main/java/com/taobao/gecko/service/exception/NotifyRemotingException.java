/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.taobao.gecko.service.exception;

/**
 * 
 * 
 * Notify remoting��check�쳣��ǿ��Ҫ��׽
 * 
 * @author boyan
 * 
 * @since 1.0, 2009-12-18 ����02:07:48
 */

public class NotifyRemotingException extends Exception {

    static final long serialVersionUID = 8923187437857838L;


    public NotifyRemotingException() {
        super();
    }


    public NotifyRemotingException(String message, Throwable cause) {
        super(message, cause);
    }


    public NotifyRemotingException(String message) {
        super(message);
    }


    public NotifyRemotingException(Throwable cause) {
        super(cause);
    }

}