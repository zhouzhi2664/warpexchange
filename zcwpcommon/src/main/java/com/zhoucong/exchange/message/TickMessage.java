package com.zhoucong.exchange.message;

import java.util.List;

import com.zhoucong.exchange.model.quotation.TickEntity;

public class TickMessage extends AbstractMessage {
	
	public long sequenceId;

    public List<TickEntity> ticks;

}
