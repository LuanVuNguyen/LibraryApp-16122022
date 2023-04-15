package com.example.libraryapp.Thread;

/**
 * HTTP response interface
 *
 * @author cong-pv
 * @since 2019-03-07
 */

public interface HttpRfidResponse {

    void progressRfidFinish(String output, int typeRequestApi, String fileName);
}
