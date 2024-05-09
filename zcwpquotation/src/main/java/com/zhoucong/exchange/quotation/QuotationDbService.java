package com.zhoucong.exchange.quotation;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.zhoucong.exchange.model.quotation.DayBarEntity;
import com.zhoucong.exchange.model.quotation.HourBarEntity;
import com.zhoucong.exchange.model.quotation.MinBarEntity;
import com.zhoucong.exchange.model.quotation.SecBarEntity;
import com.zhoucong.exchange.model.quotation.TickEntity;
import com.zhoucong.exchange.support.AbstractDbService;

@Component
@Transactional
public class QuotationDbService extends AbstractDbService {

	public void saveBars(SecBarEntity sec, MinBarEntity min, HourBarEntity hour, DayBarEntity day) {
        if (sec != null) {
            this.db.insertIgnore(sec);
        }
        if (min != null) {
            this.db.insertIgnore(min);
        }
        if (hour != null) {
            this.db.insertIgnore(hour);
        }
        if (day != null) {
            this.db.insertIgnore(day);
        }
    }

    public void saveTicks(List<TickEntity> ticks) {
        this.db.insertIgnore(ticks);
    }
}
