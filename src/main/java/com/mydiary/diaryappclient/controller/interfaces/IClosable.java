package com.mydiary.diaryappclient.controller.interfaces;

public interface IClosable {
    /**
     * Gán hành động sẽ được thực thi khi controller này muốn đóng.
     * @param action Hành động (thường là một lambda expression).
     */
    void setOnClose(Runnable action);
}
