package com.taobao.rpc.remoting.command;

import com.taobao.gecko.core.command.ResponseStatus;


public class ResponseStatusCode {
    public static short getValue(final ResponseStatus status) {
        switch (status) {
        case NO_ERROR:
            return 0x0000;
        case ERROR:
            return 0x0001;
        case EXCEPTION:
            return 0x0002;
        case UNKNOWN:
            return 0x0003;
        case THREADPOOL_BUSY:
            return 0x0004;
        case ERROR_COMM:
            return 0x0005;
        case NO_PROCESSOR:
            return 0x0006;
        case TIMEOUT:
            return 0x0007;

        }
        throw new IllegalArgumentException("Unknown status," + status);
    }


    public static ResponseStatus valueOf(final short value) {
        switch (value) {
        case 0x0000:
            return ResponseStatus.NO_ERROR;
        case 0x0001:
            return ResponseStatus.ERROR;
        case 0x0002:
            return ResponseStatus.EXCEPTION;
        case 0x0003:
            return ResponseStatus.UNKNOWN;
        case 0x0004:
            return ResponseStatus.THREADPOOL_BUSY;
        case 0x0005:
            return ResponseStatus.ERROR_COMM;
        case 0x0006:
            return ResponseStatus.NO_PROCESSOR;
        case 0x0007:
            return ResponseStatus.TIMEOUT;
        }
        throw new IllegalArgumentException("Unknown status value ," + value);
    }
}
