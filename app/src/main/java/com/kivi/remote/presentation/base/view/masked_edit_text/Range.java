package com.kivi.remote.presentation.base.view.masked_edit_text;

public class Range {
    private int start;
    private int end;

    Range() {
        start = -1;
        end = -1;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }


}